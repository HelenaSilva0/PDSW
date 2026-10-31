package vivamama.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vivamama.model.Paciente;
import vivamama.model.User;
import vivamama.repository.ChatMessageRepository;
import vivamama.repository.ChatRepository;
import vivamama.repository.ExameRepository;
import vivamama.repository.HistoricoFamiliarAnexoRepository;
import vivamama.repository.HistoricoFamiliarRepository;
import vivamama.repository.PacienteRepository;
import vivamama.repository.UserRepository;

@Service
public class AccountService {
    private final UserRepository userRepository;
    private final PacienteRepository pacienteRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRepository chatRepository;
    private final ExameRepository exameRepository;
    private final HistoricoFamiliarRepository historicoRepository;
    private final HistoricoFamiliarAnexoRepository anexoRepository;
    private final String uploadDir;

    public AccountService(UserRepository userRepository, PacienteRepository pacienteRepository,
            ChatMessageRepository chatMessageRepository, ChatRepository chatRepository,
            ExameRepository exameRepository, HistoricoFamiliarRepository historicoRepository,
            HistoricoFamiliarAnexoRepository anexoRepository, @Value("${app.upload-dir}") String uploadDir) {
        this.userRepository = userRepository;
        this.pacienteRepository = pacienteRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.chatRepository = chatRepository;
        this.exameRepository = exameRepository;
        this.historicoRepository = historicoRepository;
        this.anexoRepository = anexoRepository;
        this.uploadDir = uploadDir;
    }

    @Transactional
    public void deletePatientAccount(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
        Paciente paciente = pacienteRepository.findByUser_Id(userId)
                .orElseThrow(() -> new IllegalArgumentException("Perfil de paciente não encontrado."));

        Path uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
        exameRepository.findByPaciente_IdPacienteOrderByIdDesc(paciente.getIdPaciente())
                .forEach(exame -> deleteFile(uploadRoot, exame.getNomeArmazenado()));
        anexoRepository.findByHistorico_Paciente_IdPacienteOrderByIdDesc(paciente.getIdPaciente())
                .forEach(anexo -> deleteFile(uploadRoot, anexo.getNomeArmazenado()));

        chatMessageRepository.deleteByChat_Paciente_IdPaciente(paciente.getIdPaciente());
        anexoRepository.deleteByHistorico_Paciente_IdPaciente(paciente.getIdPaciente());
        historicoRepository.deleteByPaciente_IdPaciente(paciente.getIdPaciente());
        exameRepository.deleteByPaciente_IdPaciente(paciente.getIdPaciente());
        chatRepository.deleteByPaciente_IdPaciente(paciente.getIdPaciente());
        pacienteRepository.delete(paciente);
        userRepository.delete(user);
    }

    private void deleteFile(Path root, String storedName) {
        if (storedName == null || storedName.isBlank()) return;
        try {
            Path path = root.resolve(storedName).normalize();
            if (path.startsWith(root)) Files.deleteIfExists(path);
        } catch (Exception ignored) {
            // A remoção de arquivo órfão não deve impedir a exclusão dos dados da conta.
        }
    }
}
