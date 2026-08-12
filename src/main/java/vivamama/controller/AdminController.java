package vivamama.controller;

import java.time.Instant;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vivamama.dto.AdminMedicoResponse;
import vivamama.model.UserStatus;
import vivamama.model.UserType;
import vivamama.repository.MedicoRepository;
import vivamama.repository.UserRepository;
import vivamama.security.AuthzService;

@RestController
@RequestMapping("/admin/medicos")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private final UserRepository users;
    private final MedicoRepository medicos;
    private final AuthzService authz;

    public AdminController(UserRepository users, MedicoRepository medicos, AuthzService authz) {
        this.users = users; this.medicos = medicos; this.authz = authz;
    }

    @GetMapping("/pendentes")
    public List<AdminMedicoResponse> pendentes() {
        return users.findByRoleAndStatusOrderByIdAsc(UserType.MEDICO, UserStatus.PENDENTE).stream()
                .map(user -> medicos.findByUser_Id(user.getId()).map(m -> AdminMedicoResponse.from(user,
                        m.getIdMedico(), m.getNome(), m.getCrm(), m.getEspecialidade())).orElse(null))
                .filter(java.util.Objects::nonNull).toList();
    }

    @PostMapping("/{id}/aprovar")
    public ResponseEntity<Void> aprovar(@PathVariable Integer id) { return alterar(id, UserStatus.ATIVO); }

    @PostMapping("/{id}/rejeitar")
    public ResponseEntity<Void> rejeitar(@PathVariable Integer id) { return alterar(id, UserStatus.BLOQUEADO); }

    private ResponseEntity<Void> alterar(Integer id, UserStatus status) {
        return users.findById(id).filter(user -> user.getRole() == UserType.MEDICO).map(user -> {
            user.setStatus(status); user.setStatusAlteradoPorUserId(authz.currentUserId());
            user.setStatusAlteradoEm(Instant.now()); users.save(user);
            return ResponseEntity.noContent().<Void>build();
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
