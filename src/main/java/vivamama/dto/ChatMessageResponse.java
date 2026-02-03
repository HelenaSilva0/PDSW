package vivamama.dto;

import java.time.Instant;

public class ChatMessageResponse {
	private Long id;
	private String sender;
	private String texto;
	private Instant enviadoEm;

	public ChatMessageResponse(Long id, String sender, String texto, Instant enviadoEm) {
		this.id = id;
		this.sender = sender;
		this.texto = texto;
		this.enviadoEm = enviadoEm;
	}

	public Long getId() {
		return id;
	}

	public String getSender() {
		return sender;
	}

	public String getTexto() {
		return texto;
	}

	public Instant getEnviadoEm() {
		return enviadoEm;
	}
}
