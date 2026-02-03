package vivamama.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vivamama.dto.ChatMessageResponse;
import vivamama.dto.ChatSummaryResponse;
import vivamama.model.*;
import vivamama.repository.*;

@Service
public class ChatService {

	private final ChatRepository chatRepo;
	private final ChatMessageRepository msgRepo;
	private final MedicoRepository medicoRepo;
	private final PacienteRepository pacienteRepo;
	private final UserRepository userRepo;

	public ChatService(ChatRepository chatRepo, ChatMessageRepository msgRepo, MedicoRepository medicoRepo,
			PacienteRepository pacienteRepo, UserRepository userRepo) {
		this.chatRepo = chatRepo;
		this.msgRepo = msgRepo;
		this.medicoRepo = medicoRepo;
		this.pacienteRepo = pacienteRepo;
		this.userRepo = userRepo;
	}

	private User requireUser(Integer userId) {
		return userRepo.findById(userId).orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
	}

	private Medico requireMedicoByUserId(Integer userId) {
		return medicoRepo.findByUser_Id(userId)
				.orElseThrow(() -> new IllegalArgumentException("Perfil de médico não encontrado."));
	}

	private Paciente requirePacienteByUserId(Integer userId) {
		return pacienteRepo.findByUser_Id(userId)
				.orElseThrow(() -> new IllegalArgumentException("Perfil de paciente não encontrado."));
	}

	@Transactional
	public Chat startChat(Integer medicoUserId, Integer pacienteId) {
		Medico medico = requireMedicoByUserId(medicoUserId);
		Paciente paciente = pacienteRepo.findById(pacienteId)
				.orElseThrow(() -> new IllegalArgumentException("Paciente não encontrado."));

		return chatRepo.findByMedico_IdMedicoAndPaciente_IdPaciente(medico.getIdMedico(), paciente.getIdPaciente())
				.orElseGet(() -> {
					Chat c = new Chat();
					c.setMedico(medico);
					c.setPaciente(paciente);
					return chatRepo.save(c);
				});
	}

	public List<ChatSummaryResponse> listChats(Integer userId) {
		User u = requireUser(userId);

		if (u.getRole() == UserType.MEDICO) {
			Medico m = requireMedicoByUserId(userId);
			return chatRepo.findByMedico_IdMedicoOrderByCriadoEmDesc(m.getIdMedico()).stream()
					.map(c -> new ChatSummaryResponse(c.getId(), c.getPaciente().getIdPaciente(),
							c.getPaciente().getNome(), null, c.getCriadoEm()))
					.collect(Collectors.toList());
		}

		if (u.getRole() == UserType.PACIENTE) {
			Paciente p = requirePacienteByUserId(userId);
			return chatRepo.findByPaciente_IdPacienteOrderByCriadoEmDesc(p.getIdPaciente()).stream()
					.map(c -> new ChatSummaryResponse(c.getId(), null, null, "Médico(a)", c.getCriadoEm()))
					.collect(Collectors.toList());
		}

		throw new IllegalArgumentException("Role não suportada.");
	}

	public List<ChatMessageResponse> listMessages(Long chatId) {
		return msgRepo.findByChat_IdOrderByEnviadoEmAsc(chatId).stream()
				.map(m -> new ChatMessageResponse(m.getId(), m.getSender().name(), m.getTexto(), m.getEnviadoEm()))
				.collect(Collectors.toList());
	}

	@Transactional
	public ChatMessageResponse sendMessage(Long chatId, Integer userId, String texto) {
		if (texto == null || texto.trim().isEmpty())
			throw new IllegalArgumentException("texto é obrigatório");

		User u = requireUser(userId);
		Chat chat = chatRepo.findById(chatId).orElseThrow(() -> new IllegalArgumentException("Chat não encontrado."));

		if (u.getRole() == UserType.MEDICO) {
			Medico m = requireMedicoByUserId(userId);
			if (chat.getMedico().getIdMedico() != m.getIdMedico())
				throw new IllegalArgumentException("Sem acesso.");
		} else if (u.getRole() == UserType.PACIENTE) {
			Paciente p = requirePacienteByUserId(userId);
			if (chat.getPaciente().getIdPaciente() != p.getIdPaciente())
				throw new IllegalArgumentException("Sem acesso.");
		} else {
			throw new IllegalArgumentException("Role não suportada.");
		}

		ChatMessage msg = new ChatMessage();
		msg.setChat(chat);
		msg.setSender(u.getRole() == UserType.MEDICO ? ChatSender.MEDICO : ChatSender.PACIENTE);
		msg.setTexto(texto.trim());
		msg = msgRepo.save(msg);

		return new ChatMessageResponse(msg.getId(), msg.getSender().name(), msg.getTexto(), msg.getEnviadoEm());
	}
}