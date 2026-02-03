package vivamama.dto;

import java.time.Instant;

public class ChatSummaryResponse {
	private Long chatId;
	private Integer pacienteId;
	private String pacienteNome;
	private String medicoLabel;
	private Instant criadoEm;

	public ChatSummaryResponse(Long chatId, Integer pacienteId, String pacienteNome, String medicoLabel,
			Instant criadoEm) {
		this.chatId = chatId;
		this.pacienteId = pacienteId;
		this.pacienteNome = pacienteNome;
		this.medicoLabel = medicoLabel;
		this.criadoEm = criadoEm;
	}

	public Long getChatId() {
		return chatId;
	}

	public Integer getPacienteId() {
		return pacienteId;
	}

	public String getPacienteNome() {
		return pacienteNome;
	}

	public String getMedicoLabel() {
		return medicoLabel;
	}

	public Instant getCriadoEm() {
		return criadoEm;
	}
}
