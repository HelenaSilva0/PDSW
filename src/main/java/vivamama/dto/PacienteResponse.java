package vivamama.dto;

import java.time.LocalDate;
import vivamama.model.Paciente;

/** Representação pública do paciente, sem credenciais ou entidade User. */
public record PacienteResponse(
        int idPaciente,
        String nome,
        LocalDate dataNascimento,
        String historicoFamiliar,
        String observacoes,
        char genero
) {
    public static PacienteResponse from(Paciente paciente) {
        return new PacienteResponse(
                paciente.getIdPaciente(), paciente.getNome(), paciente.getDataNascimento(),
                paciente.getHistoricoFamiliar(), paciente.getObservacoes(), paciente.getGenero()
        );
    }
}
