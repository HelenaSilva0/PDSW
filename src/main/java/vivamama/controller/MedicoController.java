package vivamama.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vivamama.model.Medico;
import vivamama.dto.MedicoProfile;
import vivamama.dto.MedicoResponse;
import vivamama.security.AuthzService;
import vivamama.service.MedicoService;

import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping("/medicos")
public class MedicoController {

    @Autowired
    private MedicoService service;

    @Autowired
    private AuthzService authz;

    @PreAuthorize("denyAll()")
    @PostMapping
    public MedicoResponse criar(@RequestBody Medico medico) {
        return MedicoResponse.from(service.salvar(medico));
    }

    @PreAuthorize("hasRole('MEDICO')")
    @GetMapping
    public List<MedicoResponse> listar() {
        return service.listar().stream().map(MedicoResponse::from).toList();
    }
    
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    public ResponseEntity<MedicoResponse> buscar(@PathVariable Integer id) {
        Optional<Medico> m = service.buscarPorId(id);
        return m.map(MedicoResponse::from).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasRole('MEDICO') and @authz.isMedicoOwner(#id)")
    @PutMapping("/{id}")
    public ResponseEntity<MedicoResponse> atualizar(@PathVariable Integer id, @RequestBody MedicoProfile profile) {
        Medico medico = new Medico();
        medico.setNome(profile.nome);
        medico.setCrm(profile.crm);
        medico.setEspecialidade(profile.especialidade);
        Optional<Medico> m = service.atualizar(id, medico);
        return m.map(MedicoResponse::from).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasRole('MEDICO') and @authz.isMedicoOwner(#id)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Integer id) {
        return service.excluir(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
