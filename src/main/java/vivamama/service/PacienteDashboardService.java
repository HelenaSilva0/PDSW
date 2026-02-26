package vivamama.service;

import org.springframework.stereotype.Service;
import vivamama.dto.PacienteDashboardResponse;
import vivamama.model.Chat;
import vivamama.model.ChatMessage;
import vivamama.model.Medico;
import vivamama.model.Paciente;
import vivamama.repository.*;

import java.util.*;

@Service
public class PacienteDashboardService {

    private final PacienteRepository pacienteRepo;
    private final MedicoRepository medicoRepo;
    private final ChatRepository chatRepo;
    private final ChatMessageRepository msgRepo;
    private final ExameRepository exameRepo;
    private final HistoricoFamiliarRepository historicoRepo;

    public PacienteDashboardService(
            PacienteRepository pacienteRepo,
            MedicoRepository medicoRepo,
            ChatRepository chatRepo,
            ChatMessageRepository msgRepo,
            ExameRepository exameRepo,
            HistoricoFamiliarRepository historicoRepo
    ) {
        this.pacienteRepo = pacienteRepo;
        this.medicoRepo = medicoRepo;
        this.chatRepo = chatRepo;
        this.msgRepo = msgRepo;
        this.exameRepo = exameRepo;
        this.historicoRepo = historicoRepo;
    }

    public List<PacienteDashboardResponse> listarDashboard(Integer medicoUserId) {
        Medico medico = medicoRepo.findByUser_Id(medicoUserId)
                .orElseThrow(() -> new IllegalArgumentException("Perfil de médico não encontrado."));

        List<Paciente> pacientes = pacienteRepo.findAll();
        List<Integer> ids = pacientes.stream()
                .map(Paciente::getIdPaciente)
                .toList();


        Map<Integer, ExameRepository.ExameResumo> examesByPaciente = new HashMap<>();
        if (!ids.isEmpty()) {
            for (var r : exameRepo.resumoPorPacientes(ids)) {
                examesByPaciente.put(r.getPacienteId(), r);
            }
        }

        Map<Integer, HistoricoFamiliarRepository.HistoricoResumo> histByPaciente = new HashMap<>();
        if (!ids.isEmpty()) {
            for (var r : historicoRepo.resumoPorPacientes(ids)) {
                histByPaciente.put(r.getPacienteId(), r);
            }
        }

        List<Chat> chats = chatRepo.findByMedico_IdMedicoOrderByCriadoEmDesc(medico.getIdMedico());
        Map<Integer, Chat> chatByPaciente = new HashMap<>();
        Map<Long, ChatMessage> lastMsgByChatId = new HashMap<>();
        for (Chat c : chats) {
            chatByPaciente.put(c.getPaciente().getIdPaciente(), c);
            msgRepo.findTopByChat_IdOrderByEnviadoEmDesc(c.getId())
                    .ifPresent(m -> lastMsgByChatId.put(c.getId(), m));
        }

        List<PacienteDashboardResponse> out = new ArrayList<>();
        for (Paciente p : pacientes) {
            PacienteDashboardResponse r = new PacienteDashboardResponse();
            r.idPaciente = p.getIdPaciente();
            r.nome = p.getNome();
            r.genero = p.getGenero() == '\u0000' ? null : p.getGenero();
            r.dataNascimento = p.getDataNascimento();

            Chat c = chatByPaciente.get(p.getIdPaciente());
            r.temChat = c != null;
            if (c != null) {
                r.chatId = c.getId();
                r.chatCriadoEm = c.getCriadoEm();
                ChatMessage last = lastMsgByChatId.get(c.getId());
                if (last != null) {
                    r.ultimaMensagemEm = last.getEnviadoEm();
                    r.ultimaMensagemSender = last.getSender().name();
                    r.ultimaMensagemTexto = snippet(last.getTexto());
                }
            }

            var ex = examesByPaciente.get(p.getIdPaciente());
            r.exameCount = ex == null ? 0 : Math.toIntExact(ex.getTotal());
            r.ultimoExameEm = ex == null ? null : ex.getUltimoEnviadoEm();

            var hi = histByPaciente.get(p.getIdPaciente());
            r.historicoCount = hi == null ? 0 : Math.toIntExact(hi.getTotal());
            r.ultimoHistoricoEm = hi == null ? null : hi.getUltimoCriadoEm();

            out.add(r);
        }

        out.sort(Comparator.comparing(a -> (a.nome == null ? "" : a.nome.toLowerCase())));
        return out;
    }

    private static String snippet(String s) {
        if (s == null) return null;
        String t = s.trim();
        if (t.isEmpty()) return null;
        if (t.length() <= 80) return t;
        return t.substring(0, 80) + "…";
    }
}
