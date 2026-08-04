package vivamama.dto;

import vivamama.model.Medico;

/** Representação pública do médico sem expor a entidade User. */
public record MedicoResponse(int idMedico, String nome, String crm, String especialidade) {
    public static MedicoResponse from(Medico medico) {
        return new MedicoResponse(medico.getIdMedico(), medico.getNome(), medico.getCrm(), medico.getEspecialidade());
    }
}
