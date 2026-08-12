package vivamama.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class HistoricoFamiliarSnapshotRequest {
    @NotNull
    public Integer pacienteId;
    @NotBlank @Size(max = 4000)
    public String textoHistorico;
}
