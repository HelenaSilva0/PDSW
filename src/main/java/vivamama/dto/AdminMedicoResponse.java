package vivamama.dto;

import java.time.Instant;
import vivamama.model.User;
import vivamama.model.UserStatus;

public record AdminMedicoResponse(Integer userId, String email, String telefone, Integer medicoId,
        String nome, String crm, String especialidade, UserStatus status,
        Integer statusAlteradoPorUserId, Instant statusAlteradoEm) {
    public static AdminMedicoResponse from(User user, Integer medicoId, String nome, String crm, String especialidade) {
        return new AdminMedicoResponse(user.getId(), user.getEmail(), user.getTelefone(), medicoId, nome, crm,
                especialidade, user.getStatus(), user.getStatusAlteradoPorUserId(), user.getStatusAlteradoEm());
    }
}
