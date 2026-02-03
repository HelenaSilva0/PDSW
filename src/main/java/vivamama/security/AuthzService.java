package vivamama.security;

import java.util.Objects;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import vivamama.repository.ChatRepository;
import vivamama.repository.ExameRepository;
import vivamama.repository.HistoricoFamiliarAnexoRepository;
import vivamama.repository.HistoricoFamiliarRepository;
import vivamama.repository.MedicoRepository;
import vivamama.repository.PacienteRepository;
import vivamama.repository.UserRepository;

@Component("authz")
public class AuthzService {

	private final UserRepository userRepo;
	private final PacienteRepository pacienteRepository;
	private final HistoricoFamiliarRepository historicoRepo;
	private final HistoricoFamiliarAnexoRepository anexoRepo;
	private final ExameRepository exameRepo;
	private final ChatRepository chatRepo;
	private final MedicoRepository medicoRepo;


	public AuthzService(UserRepository userRepo, PacienteRepository pacienteRepository, MedicoRepository medicoRepo,
			HistoricoFamiliarRepository historicoRepo, HistoricoFamiliarAnexoRepository anexoRepo,
			ExameRepository exameRepo, ChatRepository chatRepo) {
		this.userRepo = userRepo;
		this.pacienteRepository = pacienteRepository;
		this.medicoRepo = medicoRepo;
		this.historicoRepo = historicoRepo;
		this.anexoRepo = anexoRepo;
		this.exameRepo = exameRepo;
		this.chatRepo = chatRepo;
	}

	public Integer currentUserId() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || auth.getName() == null)
			return null;
		return userRepo.findByEmail(auth.getName()).map(u -> u.getId()).orElse(null);
	}

	public boolean isSelf(Integer userId) {
		Integer me = currentUserId();
		return me != null && me.equals(userId);
	}

	public boolean isPacienteOwnerByPacienteId(Integer pacienteId) {
		Integer userId = currentUserId();
		if (userId == null || pacienteId == null)
			return false;

		return pacienteRepository.findByUser_Id(userId).map(p -> p.getIdPaciente() == pacienteId.intValue())
				.orElse(false);
	}

	public boolean isPacienteOwnerByHistoricoId(Long historicoId) {
		Integer userId = currentUserId();
		if (userId == null || historicoId == null)
			return false;

		return pacienteRepository.findByUser_Id(userId)
				.map(p -> historicoRepo.findById(historicoId)
						.map(h -> h.getPaciente() != null && h.getPaciente().getIdPaciente() == p.getIdPaciente())
						.orElse(false))
				.orElse(false);
	}

	public boolean isPacienteOwnerByAnexoId(Long anexoId) {
		Integer userId = currentUserId();
		if (userId == null || anexoId == null)
			return false;

		return pacienteRepository.findByUser_Id(userId)
				.map(p -> anexoRepo.findById(anexoId)
						.map(a -> a.getHistorico() != null && a.getHistorico().getPaciente() != null
								&& a.getHistorico().getPaciente().getIdPaciente() == p.getIdPaciente())
						.orElse(false))
				.orElse(false);
	}

	public boolean isPacienteOwnerByExameId(Long exameId) {
		Integer userId = currentUserId();
		if (userId == null || exameId == null)
			return false;

		return pacienteRepository.findByUser_Id(userId)
				.map(p -> exameRepo.findById(exameId)
						.map(e -> e.getPaciente() != null && e.getPaciente().getIdPaciente() == p.getIdPaciente())
						.orElse(false))
				.orElse(false);
	}

	public boolean canAccessChat(Long chatId) {
		Integer userId = currentUserId();
		if (userId == null || chatId == null)
			return false;

		return userRepo.findById(userId).map(u -> {
			if (u.getRole() == vivamama.model.UserType.MEDICO) {
				return medicoRepo.findByUser_Id(userId)
						.map(m -> chatRepo.findById(chatId)
								.map(c -> c.getMedico() != null && c.getMedico().getIdMedico() == m.getIdMedico())
								.orElse(false))
						.orElse(false);
			}

			if (u.getRole() == vivamama.model.UserType.PACIENTE) {
				return pacienteRepository.findByUser_Id(userId)
						.map(p -> chatRepo.findById(chatId).map(
								c -> c.getPaciente() != null && c.getPaciente().getIdPaciente() == p.getIdPaciente())
								.orElse(false))
						.orElse(false);
			}
			return false;
		}).orElse(false);
	}
}
