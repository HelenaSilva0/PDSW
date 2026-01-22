package vivamama.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import vivamama.dto.MedicoProfile;
import vivamama.dto.PacienteProfile;
import vivamama.model.Medico;
import vivamama.model.Paciente;
import vivamama.repository.MedicoRepository;
import vivamama.repository.PacienteRepository;
import vivamama.security.AuthzService;
import vivamama.service.UserService;

@RestController
@RequestMapping("/profiles")
public class ProfileController {

    @Autowired private UserService userService;
    @Autowired private PacienteRepository pacienteRepository;
    @Autowired private MedicoRepository medicoRepository;
    @Autowired private AuthzService authz;

    // ✅ NÃO depende de userId vindo do front
    @PreAuthorize("hasRole('PACIENTE')")
    @PostMapping("/paciente")
    public ResponseEntity<Paciente> criarPaciente(@RequestBody PacienteProfile req){
        Integer userId = authz.currentUserId();
        if (userId == null) return ResponseEntity.status(401).build();

        // idempotente (se já existir, devolve o existente)
        var existente = pacienteRepository.findByUser_Id(userId);
        if (existente.isPresent()) return ResponseEntity.ok(existente.get());

        return userService.buscarPorId(userId)
                .map(user -> {
                    Paciente p = new Paciente();
                    p.setUser(user);
                    p.setNome(req.nome);
                    p.setDataNascimento(req.dataNascimento);
                    p.setHistoricoFamiliar(req.historicoFamiliar != null ? req.historicoFamiliar : "");
                    p.setObservacoes(req.observacoes != null ? req.observacoes : "");
                    p.setGenero(req.genero == '\u0000' ? 'N' : req.genero);
                    return ResponseEntity.ok(pacienteRepository.save(p));
                })
                .orElse(ResponseEntity.badRequest().build());
    }

    @PreAuthorize("hasRole('MEDICO')")
    @PostMapping("/medico")
    public ResponseEntity<Medico> criarMedico(@RequestBody MedicoProfile req){
        Integer userId = authz.currentUserId();
        if (userId == null) return ResponseEntity.status(401).build();

        var existente = medicoRepository.findByUser_Id(userId);
        if (existente.isPresent()) return ResponseEntity.ok(existente.get());

        return userService.buscarPorId(userId)
                .map(user -> {
                    Medico m = new Medico();
                    m.setUser(user);
                    m.setNome(req.nome);
                    m.setCrm(req.crm);
                    m.setEspecialidade(req.especialidade);
                    return ResponseEntity.ok(medicoRepository.save(m));
                })
                .orElse(ResponseEntity.badRequest().build());
    }
}
