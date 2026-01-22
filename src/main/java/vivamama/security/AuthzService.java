package vivamama.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import vivamama.repository.PacienteRepository;
import vivamama.repository.HistoricoFamiliarAnexoRepository;
import vivamama.repository.HistoricoFamiliarRepository;
import vivamama.repository.UserRepository;

@Component("authz")
public class AuthzService {

    private final UserRepository userRepo;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private HistoricoFamiliarRepository historicoRepo;

    @Autowired
    private HistoricoFamiliarAnexoRepository anexoRepo;

    public AuthzService(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    public Integer currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) return null;
        return userRepo.findByEmail(auth.getName()).map(u -> u.getId()).orElse(null);
    }

    public boolean isSelf(Integer userId) {
        Integer me = currentUserId();
        return me != null && me.equals(userId);
    }

    public boolean isPacienteOwnerByPacienteId(Integer pacienteId) {
        Integer userId = currentUserId();
        return pacienteRepository.findByUser_Id(userId)
                .map(p -> p.getIdPaciente() == pacienteId)
                .orElse(false);
    }

    public boolean isPacienteOwnerByHistoricoId(Long historicoId) {
        Integer userId = currentUserId();
        if (userId == null) return false;
        return pacienteRepository.findByUser_Id(userId)
                .map(p -> historicoRepo.findById(historicoId)
                        .map(h -> h.getPaciente() != null && h.getPaciente().getIdPaciente() == p.getIdPaciente())
                        .orElse(false))
                .orElse(false);
    }

    public boolean isPacienteOwnerByAnexoId(Long anexoId) {
        Integer userId = currentUserId();
        if (userId == null) return false;
        return pacienteRepository.findByUser_Id(userId)
                .map(p -> anexoRepo.findById(anexoId)
                        .map(a -> a.getHistorico() != null && a.getHistorico().getPaciente() != null
                                && a.getHistorico().getPaciente().getIdPaciente() == p.getIdPaciente())
                        .orElse(false))
                .orElse(false);
    }
}
