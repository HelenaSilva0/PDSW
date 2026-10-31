package vivamama.controller;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vivamama.dto.AuthResponse;
import vivamama.dto.RegisterRequest;
import vivamama.dto.RegisterResponse;
import vivamama.dto.LoginRequest;
import vivamama.model.Medico;
import vivamama.model.Paciente;
import vivamama.model.User;
import vivamama.model.UserType;
import vivamama.model.UserStatus;
import vivamama.repository.MedicoRepository;
import vivamama.repository.PacienteRepository;
import vivamama.repository.UserRepository;
import vivamama.security.JwtService;
import vivamama.service.UserService;
import vivamama.service.AccountService;
import vivamama.service.LoginAttemptService;
import vivamama.security.AuthzService;

import java.util.Map;
import java.time.Instant;

@RestController
@RequestMapping("/auth")
public class UserController {

    private static final String TERMOS_VERSAO = "2026.1";
    private static final String PRIVACIDADE_VERSAO = "2026.1";

    @Autowired private UserService userService;
    @Autowired private PacienteRepository pacienteRepository;
    @Autowired private MedicoRepository medicoRepository;
    @Autowired private AuthenticationManager authenticationManager;
    @Autowired private JwtService jwtService;
    @Autowired private UserRepository userRepository;
    @Autowired private LoginAttemptService loginAttempts;
    @Autowired private AccountService accountService;
    @Autowired private AuthzService authz;

    @DeleteMapping("/me")
    @PreAuthorize("hasRole('PACIENTE')")
    @Transactional
    // Remove a conta do paciente autenticado e seus dados clínicos associados.
    public ResponseEntity<Void> deleteMyAccount() {
        Integer userId = authz.currentUserId();
        if (userId == null) return ResponseEntity.status(401).build();
        accountService.deletePatientAccount(userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/register")
    @Transactional
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req, jakarta.servlet.http.HttpServletRequest request) {
        String email = req.email == null ? null : req.email.trim().toLowerCase();
        String telefone = req.telefone == null ? null : req.telefone.trim();

        if (email == null || email.isBlank()) return ResponseEntity.badRequest().body("E-mail é obrigatório.");
        if (req.senha == null || req.senha.isBlank()) return ResponseEntity.badRequest().body("Senha é obrigatória.");
        if (req.senha.length() < 8) return ResponseEntity.badRequest().body("A senha deve ter pelo menos 8 caracteres.");
        if (telefone == null || telefone.isBlank()) return ResponseEntity.badRequest().body("Telefone é obrigatório.");
        if (req.role == null) return ResponseEntity.badRequest().body("Tipo de usuário é obrigatório.");

        if (!Boolean.TRUE.equals(req.aceiteTermos)) return ResponseEntity.badRequest().body("Aceite os Termos de Uso.");
        if (!Boolean.TRUE.equals(req.cienciaPrivacidade)) return ResponseEntity.badRequest().body("Confirme a leitura da Política de Privacidade.");
        if (req.role == UserType.PACIENTE && !Boolean.TRUE.equals(req.consentimentoDadosSaude)) {
            return ResponseEntity.badRequest().body("Autorize o tratamento dos dados de saúde para usar o acompanhamento.");
        }
        if (req.role == UserType.MEDICO && !Boolean.TRUE.equals(req.compromissoConfidencialidade)) {
            return ResponseEntity.badRequest().body("Aceite o compromisso de confidencialidade profissional.");
        }

        if (req.role == UserType.ADMIN) return ResponseEntity.badRequest().body("Tipo de usuario invalido.");

        String nome = req.nome == null ? null : req.nome.trim();
        if (nome == null || nome.isBlank()) {
            return ResponseEntity.badRequest().body("Nome é obrigatório.");
        }

        if (req.role == UserType.MEDICO) {
            String crm = req.crm == null ? null : req.crm.trim();
            String especialidade = req.especialidade == null ? null : req.especialidade.trim();
            if (crm == null || crm.isBlank()) return ResponseEntity.badRequest().body("CRM é obrigatório para cadastro médico.");
            if (especialidade == null || especialidade.isBlank()) return ResponseEntity.badRequest().body("Especialidade é obrigatória para cadastro médico.");
            if (medicoRepository.findByCrm(crm).isPresent()) return ResponseEntity.status(409).body("CRM já cadastrado.");
        }

        if (userService.buscarPorEmail(email).isPresent()) return ResponseEntity.status(409).body("E-mail já cadastrado.");
        if (userService.buscarPorTelefone(telefone).isPresent()) return ResponseEntity.status(409).body("Telefone já cadastrado.");

        User user = new User();
        user.setEmail(email);
        user.setSenha(req.senha);
        user.setTelefone(telefone);
        user.setRole(req.role);
        // Pacientes e médicos iniciam ativos.
        user.setStatus(UserStatus.ATIVO);
        Instant consentimentoEm = Instant.now();
        user.setTermosVersao(TERMOS_VERSAO);
        user.setTermosAceitosEm(consentimentoEm);
        user.setPrivacidadeVersao(PRIVACIDADE_VERSAO);
        user.setPrivacidadeCienteEm(consentimentoEm);
        user.setConsentimentoIp(request.getRemoteAddr());
        String userAgent = request.getHeader("User-Agent");
        user.setConsentimentoUserAgent(userAgent == null ? null : userAgent.substring(0, Math.min(userAgent.length(), 500)));
        if (req.role == UserType.PACIENTE) user.setDadosSaudeConsentimentoEm(consentimentoEm);
        if (req.role == UserType.MEDICO) user.setConfidencialidadeAceitaEm(consentimentoEm);
        User saved = userService.criar(user);

        if (saved.getRole() == UserType.PACIENTE) {
            Paciente paciente = new Paciente();
            paciente.setUser(saved);
            paciente.setNome(nome);
            paciente.setDataNascimento(req.dataNascimento);
            paciente.setHistoricoFamiliar("");
            paciente.setObservacoes(req.observacoes == null ? "" : req.observacoes.trim());
            char genero = req.genero == null ? 'N' : Character.toUpperCase(req.genero);
            paciente.setGenero("FMON".indexOf(genero) >= 0 ? genero : 'N');
            pacienteRepository.save(paciente);
        } else {
            Medico medico = new Medico();
            medico.setUser(saved);
            medico.setNome(nome);
            medico.setCrm(req.crm.trim());
            medico.setEspecialidade(req.especialidade.trim());
            medicoRepository.save(medico);
        }

        return ResponseEntity.ok(new RegisterResponse(saved.getId(), saved.getRole(), saved.getStatus()));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req, jakarta.servlet.http.HttpServletRequest request) {
        String email = req.email == null ? null : req.email.trim().toLowerCase();
        String key = request.getRemoteAddr() + ":" + (email == null ? "" : email);
        if (loginAttempts.isBlocked(key)) return ResponseEntity.status(429).body("Login temporariamente bloqueado.");
        if (email == null || email.isBlank()) return ResponseEntity.badRequest().body("E-mail é obrigatório.");

        var userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) loginAttempts.recordFailure(key);
        if (userOpt.isEmpty()) return ResponseEntity.status(401).body("Credenciais inválidas.");

        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, req.senha));
        } catch (AuthenticationException error) {
            loginAttempts.recordFailure(key);
            return ResponseEntity.status(401).body("Credenciais inválidas.");
        }

        loginAttempts.recordSuccess(key);
        User user = userOpt.get();
        String token = jwtService.generateToken(user.getEmail(),
                Map.of("role", user.getRole().name(), "userId", user.getId()));
        return ResponseEntity.ok(new AuthResponse(token, user.getId(), user.getRole()));
    }
}
