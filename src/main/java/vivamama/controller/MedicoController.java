package vivamama.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vivamama.model.Medico;
import vivamama.service.MedicoService;

import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping("/medicos")
public class MedicoController {

    @Autowired
    private MedicoService service;

    @PreAuthorize("hasRole('MEDICO')")
    @PostMapping
    public Medico criar(@RequestBody Medico medico) {
        return service.salvar(medico);
    }

    @PreAuthorize("hasRole('MEDICO')")
    @GetMapping
    public List<Medico> listar() {
        return service.listar();
    }
    
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    public ResponseEntity<Medico> buscar(@PathVariable Integer id) {
        Optional<Medico> m = service.buscarPorId(id);
        return m.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasRole('MEDICO')")
    @PutMapping("/{id}")
    public ResponseEntity<Medico> atualizar(@PathVariable Integer id, @RequestBody Medico medico) {
        Optional<Medico> m = service.atualizar(id, medico);
        return m.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasRole('MEDICO')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Integer id) {
        return service.excluir(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
