package vivamama.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vivamama.model.Exame;
import vivamama.model.HistoricoFamiliarAnexo;
import vivamama.model.Paciente;
import vivamama.repository.ChatMessageRepository;
import vivamama.repository.ChatRepository;
import vivamama.repository.ExameRepository;
import vivamama.repository.HistoricoFamiliarAnexoRepository;
import vivamama.repository.HistoricoFamiliarRepository;
import vivamama.repository.PacienteRepository;
import vivamama.repository.UserRepository;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

@Service
public class PacienteService {

    private final PacienteRepository pacienteRepository;
    private final UserRepository userRepository;
    private final ExameRepository exameRepository;
    private final HistoricoFamiliarRepository historicoRepository;
    private final HistoricoFamiliarAnexoRepository anexoRepository;
    private final ChatRepository chatRepository;
    private final ChatMessageRepository chatMessageRepository;

    @Value("${app.upload-dir}")
    private String uploadDir;

    public PacienteService(PacienteRepository pacienteRepository, UserRepository userRepository,
                           ExameRepository exameRepository, HistoricoFamiliarRepository historicoRepository,
                           HistoricoFamiliarAnexoRepository anexoRepository, ChatRepository chatRepository,
                           ChatMessageRepository chatMessageRepository) {
        this.pacienteRepository = pacienteRepository;
        this.userRepository = userRepository;
        this.exameRepository = exameRepository;
        this.historicoRepository = historicoRepository;
        this.anexoRepository = anexoRepository;
        this.chatRepository = chatRepository;
        this.chatMessageRepository = chatMessageRepository;
    }

    public Paciente salvarPaciente(@NonNull Paciente paciente) {
        return pacienteRepository.save(paciente);
    }

    public List<Paciente> listarPacientes() {
        return pacienteRepository.findAll();
    }

    public Optional<Paciente> buscarPacientePorId(@NonNull Integer id) {
        return pacienteRepository.findById(id);
    }

    public Optional<Paciente> buscarPacientePorUserId(@NonNull Integer userId) {
        return pacienteRepository.findByUser_Id(userId);
    }

    public Optional<Paciente> atualizarPaciente(@NonNull Integer id, Paciente atualizado) {
        return pacienteRepository.findById(id).map(p -> {
            aplicarAtualizacao(p, atualizado);
            return pacienteRepository.save(p);
        });
    }

    public Optional<Paciente> atualizarPacientePorUserId(@NonNull Integer userId, Paciente atualizado) {
        return pacienteRepository.findByUser_Id(userId).map(p -> {
            aplicarAtualizacao(p, atualizado);
            return pacienteRepository.save(p);
        });
    }

    public boolean excluirPaciente(@NonNull Integer id) {
        return pacienteRepository.findById(id)
                .map(p -> { pacienteRepository.delete(p); return true; })
                .orElse(false);
    }

    @Transactional
    public boolean excluirContaPorUserId(@NonNull Integer userId) {
        return pacienteRepository.findByUser_Id(userId).map(p -> {
            for (Exame exame : exameRepository.findByPaciente_IdPacienteOrderByIdDesc(p.getIdPaciente())) {
                apagarArquivo(exame.getNomeArmazenado());
            }
            for (HistoricoFamiliarAnexo anexo : anexoRepository.findByHistorico_Paciente_IdPacienteOrderByIdDesc(p.getIdPaciente())) {
                apagarArquivo(anexo.getNomeArmazenado());
            }

            chatMessageRepository.deleteByChat_Paciente_IdPaciente(p.getIdPaciente());
            chatRepository.deleteByPaciente_IdPaciente(p.getIdPaciente());
            anexoRepository.deleteByHistorico_Paciente_IdPaciente(p.getIdPaciente());
            historicoRepository.deleteByPaciente_IdPaciente(p.getIdPaciente());
            exameRepository.deleteByPaciente_IdPaciente(p.getIdPaciente());
            pacienteRepository.delete(p);
            userRepository.deleteById(userId);
            return true;
        }).orElse(false);
    }

    private void apagarArquivo(String nome) {
        if (nome == null || nome.isBlank()) return;
        try {
            Path root = Paths.get(uploadDir).toAbsolutePath().normalize();
            Path path = root.resolve(nome).normalize();
            if (path.startsWith(root)) Files.deleteIfExists(path);
        } catch (Exception ignored) {
            // A limpeza física pode ser refeita por job sem desfazer a remoção lógica.
        }
    }

    private void aplicarAtualizacao(Paciente paciente, Paciente atualizado) {
        if (atualizado.getNome() != null) paciente.setNome(atualizado.getNome());
        if (atualizado.getDataNascimento() != null) paciente.setDataNascimento(atualizado.getDataNascimento());
        if (atualizado.getHistoricoFamiliar() != null) paciente.setHistoricoFamiliar(atualizado.getHistoricoFamiliar());
        if (atualizado.getObservacoes() != null) paciente.setObservacoes(atualizado.getObservacoes());
        if (atualizado.getGenero() != '\u0000') paciente.setGenero(atualizado.getGenero());
    }
}
