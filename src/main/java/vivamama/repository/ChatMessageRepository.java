package vivamama.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import vivamama.model.ChatMessage;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

	List<ChatMessage> findByChat_IdOrderByEnviadoEmAsc(Long chatId);
    Optional<ChatMessage> findTopByChat_IdOrderByEnviadoEmDesc(Long chatId);
    void deleteByChat_Paciente_IdPaciente(int pacienteId);

}
