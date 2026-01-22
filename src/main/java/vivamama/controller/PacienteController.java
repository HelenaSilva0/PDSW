package vivamama.controller;

import vivamama.model.Paciente;
import vivamama.security.AuthzService;
import vivamama.service.PacienteService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/pacientes")

public class PacienteController {
	
	    private final PacienteService pacienteService;
	 	private final AuthzService authz;
	 	
	 	 public PacienteController(PacienteService pacienteService, AuthzService authz) {
	         this.pacienteService = pacienteService;
	         this.authz = authz;
	     }
	 	
	    @PreAuthorize("hasRole('MEDICO')")
	    @GetMapping
	    public List<Paciente> listarPacientes() {
	        return pacienteService.listarPacientes();
	    }

	    @PreAuthorize("hasRole('MEDICO')")
	    @GetMapping("/{id}")
	    public ResponseEntity<Paciente> buscarPacientePorId(@PathVariable Integer id) {
	        Optional<Paciente> paciente = pacienteService.buscarPacientePorId(id);
	        return paciente.map(ResponseEntity::ok)
	                       .orElseGet(() -> ResponseEntity.notFound().build());
	    }

	   
	   // paciente pode consultar "por-usuario" apenas o próprio userId, médico pode qualquer um
	    @PreAuthorize("hasRole('MEDICO') or @authz.isSelf(#userId)")
	    @GetMapping("/por-usuario/{userId}")
	    public ResponseEntity<Paciente> buscarPacientePorUserId(@PathVariable Integer userId) {
	        Optional<Paciente> paciente = pacienteService.buscarPacientePorUserId(userId);
	        return paciente.map(ResponseEntity::ok)
	                       .orElseGet(() -> ResponseEntity.notFound().build());
	    }
	    
	    @PreAuthorize("hasRole('PACIENTE')")
	    @GetMapping("/me")
	    public ResponseEntity<Paciente> me() {
	        Integer userId = authz.currentUserId();
	        return pacienteService.buscarPacientePorUserId(userId)
	                .map(ResponseEntity::ok)
	                .orElseGet(() -> ResponseEntity.notFound().build());
	    }

	    @PreAuthorize("hasRole('PACIENTE')")
	    @PutMapping("/me")
	    public ResponseEntity<Paciente> atualizarMe(@RequestBody Paciente atualizado) {
	        Integer userId = authz.currentUserId();
	        return pacienteService.atualizarPacientePorUserId(userId, atualizado)
	                .map(ResponseEntity::ok)
	                .orElseGet(() -> ResponseEntity.notFound().build());
	    }

	    @PreAuthorize("hasRole('PACIENTE')")
	    @DeleteMapping("/me")
	    public ResponseEntity<Void> apagarMinhaConta() {
	        Integer userId = authz.currentUserId();
	        boolean ok = pacienteService.excluirPacientePorUserId(userId);
	        return ok ? ResponseEntity.noContent().build()
	                  : ResponseEntity.notFound().build();
	    }
	}