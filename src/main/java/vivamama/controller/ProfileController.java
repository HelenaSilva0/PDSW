package vivamama.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import vivamama.dto.*;
import vivamama.model.*;
import vivamama.repository.*;
import vivamama.service.UserService;

@RestController
@RequestMapping("/profiles")
public class ProfileController {
	
	@Autowired private UserService userService;
    @Autowired private PacienteRepository pacienteRepository;
    @Autowired private MedicoRepository medicoRepository;

    @PostMapping("/paciente")
    public ResponseEntity<Paciente> criarPaciente(@RequestBody PacienteProfile req){
        return userService.buscarPorId(req.userId)
                .map(user -> {
                    Paciente p = new Paciente();
                    p.setUser(user);
                    p.setNome(req.nome);
                    p.setDataNascimento(req.dataNascimento);
                    p.setHistoricoFamiliar(req.historicoFamiliar);
                    p.setObservacoes(req.observacoes);
                    p.setGenero(req.genero);
                    return ResponseEntity.ok(pacienteRepository.save(p));
                })
                .orElse(ResponseEntity.badRequest().build());
    }

    @PostMapping("/medico")
    public ResponseEntity<Medico> criarMedico(@RequestBody MedicoProfile req){
        return userService.buscarPorId(req.userId)
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
