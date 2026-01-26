package vivamama.dto;

import java.time.LocalDateTime;

public class ExameResponse {
    public Long id;
    public Integer pacienteId;
    public Integer versao;
    public String descricao;
    public String nomeOriginal;
    public String contentType;
    public Long tamanho;
    public LocalDateTime enviadoEm;
    public Integer enviadoPorUserId;
}
