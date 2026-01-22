package vivamama.security;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import vivamama.model.User;
import vivamama.repository.UserRepository;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository repo;

    public CustomUserDetailsService(UserRepository repo) {
        this.repo = repo;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = repo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

        String raw = user.getRole().name(); // pode ser PACIENTE ou ROLE_PACIENTE
        String role = raw.startsWith("ROLE_") ? raw : "ROLE_" + raw;

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getSenha(),
                List.of(new SimpleGrantedAuthority(role))
        );
    }
}

