package vivamama.model;

import java.time.Instant;

import jakarta.persistence.*;

@Entity
public class ChatMessage {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "chat_id")
	private Chat chat;

	@Enumerated(EnumType.STRING)
	private ChatSender sender;

	@Column(length = 2000, nullable = false)
	private String texto;

	private Instant enviadoEm = Instant.now();

	public Long getId() {
		return id;
	}

	public Chat getChat() {
		return chat;
	}

	public void setChat(Chat chat) {
		this.chat = chat;
	}

	public ChatSender getSender() {
		return sender;
	}

	public void setSender(ChatSender sender) {
		this.sender = sender;
	}

	public String getTexto() {
		return texto;
	}

	public void setTexto(String texto) {
		this.texto = texto;
	}

	public Instant getEnviadoEm() {
		return enviadoEm;
	}
}
