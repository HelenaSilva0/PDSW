package vivamama.service;

import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;

import vivamama.model.User;
import vivamama.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class UserService {
	@Autowired 
	private UserRepository repo;


	    @Autowired
	    private PasswordEncoder passwordEncoder;

	    public User criar(User u) {
	        // Criptografa a senha 
	        String senhaCriptografada = passwordEncoder.encode(u.getSenha());
	        u.setSenha(senhaCriptografada);

	        return repo.save(u);
	    }

	    public Optional<User> buscarPorEmail(String email) {
	        return repo.findByEmail(email);
	    }
	    
	    public Optional<User> buscarPorTelefone(String telefone){
	        return repo.findByTelefone(telefone);
	    }

	    // autenticar usuário usando 
	    public Optional<User> autenticar(String email, String senhaPura) {
	        return repo.findByEmail(email)
	                .filter(u -> passwordEncoder.matches(senhaPura, u.getSenha()));    
	       
	    }

	    public Optional<User> buscarPorId(@NonNull Integer id) {
	        return repo.findById(id);
	    }
}
