package vivamama.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import vivamama.dto.*;
import vivamama.security.AuthzService;
import vivamama.service.ChatService;

@RestController
@RequestMapping("/chats")
public class ChatController {

	private final ChatService chatService;
	private final AuthzService authz;

	public ChatController(ChatService chatService, AuthzService authz) {
		this.chatService = chatService;
		this.authz = authz;
	}

	// médico inicia chat
	@PostMapping("/start")
	@PreAuthorize("hasRole('MEDICO') or hasAuthority('MEDICO')")
	public ResponseEntity<?> start(@RequestBody StartChatRequest req) {
		Integer me = authz.currentUserId();
		var chat = chatService.startChat(me, req.getPacienteId());
		return ResponseEntity.ok(chat.getId());
	}

	// lista chats do usuário logado
	@GetMapping
	public ResponseEntity<List<ChatSummaryResponse>> list() {
		Integer me = authz.currentUserId();
		return ResponseEntity.ok(chatService.listChats(me));
	}

	@GetMapping("/{chatId}/messages")
	@PreAuthorize("@authz.canAccessChat(#chatId)")
	public ResponseEntity<List<ChatMessageResponse>> messages(@PathVariable Long chatId) {
		return ResponseEntity.ok(chatService.listMessages(chatId));
	}

	@PostMapping("/{chatId}/messages")
	@PreAuthorize("@authz.canAccessChat(#chatId)")
	public ResponseEntity<ChatMessageResponse> send(@PathVariable Long chatId,
			@RequestBody SendChatMessageRequest req) {
		Integer me = authz.currentUserId();
		return ResponseEntity.ok(chatService.sendMessage(chatId, me, req.getTexto()));
	}
}