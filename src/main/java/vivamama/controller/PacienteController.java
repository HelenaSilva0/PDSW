package vivamama.controller;

import vivamama.model.Paciente;
import vivamama.service.PacienteService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/pacientes")

public class PacienteController {
	
	 @Autowired
	    private PacienteService pacienteService;
	    
	 
	 public PacienteController(PacienteService pacienteService) {
	        this.pacienteService = pacienteService;
	    }

	    @PostMapping
	    public Paciente cadastrarPaciente(@RequestBody Paciente paciente) {
	        return pacienteService.salvarPaciente(paciente);
	    }

	    @GetMapping
	    public List<Paciente> listarPacientes() {
	        return pacienteService.listarPacientes();
	    }

	    @GetMapping("/{id}")
	    public ResponseEntity<Paciente> buscarPacientePorId(@PathVariable Integer id) {
	        Optional<Paciente> paciente = pacienteService.buscarPacientePorId(id);
	        return paciente.map(ResponseEntity::ok)
	                       .orElseGet(() -> ResponseEntity.notFound().build());
	    }

	    @PutMapping("/{id}")
	    public ResponseEntity<Paciente> atualizarPaciente(@PathVariable Integer id, @RequestBody Paciente pacienteAtualizado) {
	        Optional<Paciente> paciente = pacienteService.atualizarPaciente(id, pacienteAtualizado);
	        return paciente.map(ResponseEntity::ok)
	                       .orElseGet(() -> ResponseEntity.notFound().build());
	    }

	    @DeleteMapping("/{id}")
	    public ResponseEntity<Void> excluirPaciente(@PathVariable Integer id) {
	        boolean removido = pacienteService.excluirPaciente(id);
	        return removido ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
	    }

}
