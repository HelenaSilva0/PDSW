package vivamama.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import vivamama.model.ChatMessage;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

	List<ChatMessage> findByChat_IdOrderByEnviadoEmAsc(Long chatId);
}