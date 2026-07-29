package vivamama.controller;

import vivamama.model.Paciente;
import vivamama.dto.PacienteResponse;
import vivamama.security.AuthzService;
import vivamama.service.PacienteService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import vivamama.dto.PacienteDashboardResponse;
import vivamama.service.PacienteDashboardService;

@RestController
@RequestMapping("/pacientes")

public class PacienteController {

	private final PacienteService pacienteService;
	private final AuthzService authz;
	private final PacienteDashboardService dashboardService;

	public PacienteController(PacienteService pacienteService, AuthzService authz,
			PacienteDashboardService dashboardService) {
		this.pacienteService = pacienteService;
		this.authz = authz;
		this.dashboardService = dashboardService;

	}

	@PreAuthorize("hasRole('MEDICO')")
	@GetMapping
	public List<PacienteResponse> listarPacientes() {
		return pacienteService.listarPacientes().stream().map(PacienteResponse::from).toList();
	}

	@PreAuthorize("hasRole('MEDICO')")
	@GetMapping("/{id}")
	public ResponseEntity<PacienteResponse> buscarPacientePorId(@PathVariable Integer id) {
		Optional<Paciente> paciente = pacienteService.buscarPacientePorId(id);
		return paciente.map(PacienteResponse::from).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
	}

	// paciente pode consultar "por-usuario" apenas o próprio userId, médico pode
	// qualquer um
	@PreAuthorize("hasRole('MEDICO') or @authz.isSelf(#userId)")
	@GetMapping("/por-usuario/{userId}")
	public ResponseEntity<PacienteResponse> buscarPacientePorUserId(@PathVariable Integer userId) {
		Optional<Paciente> paciente = pacienteService.buscarPacientePorUserId(userId);
		return paciente.map(PacienteResponse::from).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
	}

	@PreAuthorize("hasRole('PACIENTE')")
	@GetMapping("/me")
	public ResponseEntity<PacienteResponse> me() {
		Integer userId = authz.currentUserId();
		return pacienteService.buscarPacientePorUserId(userId).map(PacienteResponse::from).map(ResponseEntity::ok)
				.orElseGet(() -> ResponseEntity.notFound().build());
	}

	@PreAuthorize("hasRole('PACIENTE')")
	@PutMapping("/me")
	public ResponseEntity<PacienteResponse> atualizarMe(@RequestBody Paciente atualizado) {
		Integer userId = authz.currentUserId();
		return pacienteService.atualizarPacientePorUserId(userId, atualizado).map(PacienteResponse::from).map(ResponseEntity::ok)
				.orElseGet(() -> ResponseEntity.notFound().build());
	}

	@PreAuthorize("hasRole('PACIENTE')")
	@DeleteMapping("/me")
	public ResponseEntity<Void> apagarMinhaConta() {
		Integer userId = authz.currentUserId();
		boolean ok = pacienteService.excluirPacientePorUserId(userId);
		return ok ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
	}

	@PreAuthorize("hasRole('MEDICO')")
	@GetMapping("/dashboard")
	public List<PacienteDashboardResponse> dashboard() {
		Integer me = authz.currentUserId();
		return dashboardService.listarDashboard(me);
	}

}
