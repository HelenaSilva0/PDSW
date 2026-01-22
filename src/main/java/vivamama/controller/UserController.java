package vivamama.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import vivamama.dto.*;
import vivamama.model.Medico;
import vivamama.model.Paciente;
import vivamama.model.User;
import vivamama.model.UserType;
import vivamama.repository.MedicoRepository;
import vivamama.repository.PacienteRepository;
import vivamama.repository.UserRepository;
import vivamama.security.JwtService;
import vivamama.service.UserService;

@RestController
@RequestMapping("/auth")
public class UserController {

    @Autowired private UserService userService;
    @Autowired private PacienteRepository pacienteRepository;
    @Autowired private MedicoRepository medicoRepository;

    @Autowired private AuthenticationManager authenticationManager;
    @Autowired private JwtService jwtService;
    @Autowired private UserRepository userRepository;

    @PostMapping("/register")
    @Transactional
    public ResponseEntity<?> register(@RequestBody RegisterRequest req){

        String email = (req.email == null) ? null : req.email.trim().toLowerCase();
        String telefone = (req.telefone == null) ? null : req.telefone.trim();

        if (email == null || email.isBlank()) return ResponseEntity.badRequest().body("E-mail é obrigatório.");
        if (req.senha == null || req.senha.isBlank()) return ResponseEntity.badRequest().body("Senha é obrigatória.");
        if (telefone == null || telefone.isBlank()) return ResponseEntity.badRequest().body("Telefone é obrigatório.");
        if (req.role == null) return ResponseEntity.badRequest().body("Tipo de usuário é obrigatório.");

        // valida campos do perfil (para evitar usuário sem perfil)
        if (req.role == UserType.PACIENTE) {
            String nome = (req.nome == null) ? null : req.nome.trim();
            if (nome == null || nome.isBlank()) {
                return ResponseEntity.badRequest().body("Nome é obrigatório para cadastro de paciente.");
            }
        }

        if (req.role == UserType.MEDICO) {
            String nome = (req.nome == null) ? null : req.nome.trim();
            String crm = (req.crm == null) ? null : req.crm.trim();
            String esp = (req.especialidade == null) ? null : req.especialidade.trim();

            if (nome == null || nome.isBlank()) return ResponseEntity.badRequest().body("Nome é obrigatório para cadastro de médico(a).");
            if (crm == null || crm.isBlank()) return ResponseEntity.badRequest().body("CRM é obrigatório para cadastro de médico(a).");
            if (esp == null || esp.isBlank()) return ResponseEntity.badRequest().body("Especialidade é obrigatória para cadastro de médico(a).");

            if (medicoRepository.findByCrm(crm).isPresent()) {
                return ResponseEntity.status(409).body("CRM já cadastrado.");
            }
        }

        if (userService.buscarPorEmail(email).isPresent()) {
            return ResponseEntity.status(409).body("E-mail já cadastrado. Tente outro e-mail ou faça login.");
        }

        if (userService.buscarPorTelefone(telefone).isPresent()){
            return ResponseEntity.status(409).body("Telefone já cadastrado.");
        }

        User u = new User();
        u.setEmail(email);
        u.setSenha(req.senha);
        u.setTelefone(telefone);
        u.setRole(req.role);

        User saved = userService.criar(u);

        // ✅ cria perfil automaticamente
        if (saved.getRole() == UserType.PACIENTE) {
            Paciente p = new Paciente();
            p.setUser(saved);
            p.setNome(req.nome.trim());
            p.setDataNascimento(req.dataNascimento);
            p.setHistoricoFamiliar("");
            p.setObservacoes(req.observacoes != null ? req.observacoes : "");

            char g = (req.genero == null) ? 'N' : Character.toUpperCase(req.genero);
            if ("FMON".indexOf(g) < 0) g = 'N';
            p.setGenero(g);

            pacienteRepository.save(p);
        }

        if (saved.getRole() == UserType.MEDICO) {
            Medico m = new Medico();
            m.setUser(saved);
            m.setNome(req.nome.trim());
            m.setCrm(req.crm.trim());
            m.setEspecialidade(req.especialidade.trim());
            medicoRepository.save(m);
        }

        return ResponseEntity.ok(new RegisterResponse(saved.getId(), saved.getRole()));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {

        String email = (req.email == null) ? null : req.email.trim().toLowerCase();
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body("E-mail é obrigatório.");
        }

        // ✅ usuário não existe -> orienta cadastro
        var userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Usuário não cadastrado. Faça o cadastro antes de realizar o login.");
        }

        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, req.senha)
            );
        } catch (AuthenticationException ex) {
            return ResponseEntity.status(401).body("Senha inválida.");
        }

        var user = userOpt.get();

        String token = jwtService.generateToken(
            user.getEmail(),
            Map.of("role", user.getRole().name(), "userId", user.getId())
        );

        return ResponseEntity.ok(new AuthResponse(token, user.getId(), user.getRole()));
    }
}
