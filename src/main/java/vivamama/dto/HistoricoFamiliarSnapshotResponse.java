package vivamama.dto;

import java.time.LocalDateTime;
import java.util.List;

public class HistoricoFamiliarSnapshotResponse {
    public Long id;
    public Integer pacienteId;
    public Integer versao;
    public LocalDateTime criadoEm;
    public Integer criadoPorUserId;
    public String textoHistorico;
    public List<HistoricoFamiliarAnexoResponse> anexos;
}
