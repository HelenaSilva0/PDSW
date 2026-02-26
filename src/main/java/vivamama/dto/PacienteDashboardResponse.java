package vivamama.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

/*Dados agregados para o painel do médico */
public class PacienteDashboardResponse {
public Integer idPaciente;
public String nome;
public Character genero;
public LocalDate dataNascimento;

public Boolean temChat;
public Long chatId;
public Instant chatCriadoEm;
public Instant ultimaMensagemEm;
public String ultimaMensagemSender;
public String ultimaMensagemTexto;

public Integer exameCount;
public LocalDateTime ultimoExameEm;
public Integer historicoCount;
public LocalDateTime ultimoHistoricoEm;
}
