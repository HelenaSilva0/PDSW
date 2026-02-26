package vivamama.dto;

import java.time.Instant;

public class ChatSummaryResponse {
	    private Long chatId;
	    private Integer pacienteId;
	    private String pacienteNome;
	    private String medicoLabel;
	    private Instant criadoEm;

	    private Instant ultimaMensagemEm;
	    private String ultimaMensagemSender;
	    private String ultimaMensagemTexto;

	    public ChatSummaryResponse(Long chatId, Integer pacienteId, String pacienteNome, String medicoLabel, Instant criadoEm) {
	        this(chatId, pacienteId, pacienteNome, medicoLabel, criadoEm, null, null, null);
	    }

	    public ChatSummaryResponse(
	            Long chatId,
	            Integer pacienteId,
	            String pacienteNome,
	            String medicoLabel,
	            Instant criadoEm,
	            Instant ultimaMensagemEm,
	            String ultimaMensagemSender,
	            String ultimaMensagemTexto
	    ) {
	        this.chatId = chatId;
	        this.pacienteId = pacienteId;
	        this.pacienteNome = pacienteNome;
	        this.medicoLabel = medicoLabel;
	        this.criadoEm = criadoEm;
	        this.ultimaMensagemEm = ultimaMensagemEm;
	        this.ultimaMensagemSender = ultimaMensagemSender;
	        this.ultimaMensagemTexto = ultimaMensagemTexto;
	    }

	    public Long getChatId() { return chatId; }
	    public Integer getPacienteId() { return pacienteId; }
	    public String getPacienteNome() { return pacienteNome; }
	    public String getMedicoLabel() { return medicoLabel; }
	    public Instant getCriadoEm() { return criadoEm; }

	    public Instant getUltimaMensagemEm() { return ultimaMensagemEm; }
	    public String getUltimaMensagemSender() { return ultimaMensagemSender; }
	    public String getUltimaMensagemTexto() { return ultimaMensagemTexto; }
	}
