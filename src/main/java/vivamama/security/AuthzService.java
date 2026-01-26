package vivamama.security;

import java.util.Objects;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import vivamama.repository.ExameRepository;
import vivamama.repository.HistoricoFamiliarAnexoRepository;
import vivamama.repository.HistoricoFamiliarRepository;
import vivamama.repository.PacienteRepository;
import vivamama.repository.UserRepository;

@Component("authz")
public class AuthzService {

    private final UserRepository userRepo;
    private final PacienteRepository pacienteRepository;
    private final HistoricoFamiliarRepository historicoRepo;
    private final HistoricoFamiliarAnexoRepository anexoRepo;
    private final ExameRepository exameRepo;

    public AuthzService(
            UserRepository userRepo,
            PacienteRepository pacienteRepository,
            HistoricoFamiliarRepository historicoRepo,
            HistoricoFamiliarAnexoRepository anexoRepo,
            ExameRepository exameRepo
    ) {
        this.userRepo = userRepo;
        this.pacienteRepository = pacienteRepository;
        this.historicoRepo = historicoRepo;
        this.anexoRepo = anexoRepo;
        this.exameRepo = exameRepo;
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
        if (userId == null || pacienteId == null) return false;

        return pacienteRepository.findByUser_Id(userId)
                .map(p -> p.getIdPaciente() == pacienteId.intValue())
                .orElse(false);
    }

    public boolean isPacienteOwnerByHistoricoId(Long historicoId) {
        Integer userId = currentUserId();
        if (userId == null || historicoId == null) return false;

        return pacienteRepository.findByUser_Id(userId)
                .map(p -> historicoRepo.findById(historicoId)
                        .map(h -> h.getPaciente() != null
                                && h.getPaciente().getIdPaciente() == p.getIdPaciente())
                        .orElse(false))
                .orElse(false);
    }

    public boolean isPacienteOwnerByAnexoId(Long anexoId) {
        Integer userId = currentUserId();
        if (userId == null || anexoId == null) return false;

        return pacienteRepository.findByUser_Id(userId)
                .map(p -> anexoRepo.findById(anexoId)
                        .map(a -> a.getHistorico() != null
                                && a.getHistorico().getPaciente() != null
                                && a.getHistorico().getPaciente().getIdPaciente() == p.getIdPaciente())
                        .orElse(false))
                .orElse(false);
    }

    public boolean isPacienteOwnerByExameId(Long exameId) {
        Integer userId = currentUserId();
        if (userId == null || exameId == null) return false;

        return pacienteRepository.findByUser_Id(userId)
                .map(p -> exameRepo.findById(exameId)
                        .map(e -> e.getPaciente() != null
                                && e.getPaciente().getIdPaciente() == p.getIdPaciente())
                        .orElse(false))
                .orElse(false);
    }
}
