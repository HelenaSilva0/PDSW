package vivamama.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import vivamama.model.User;

public interface UserRepository extends JpaRepository <User, Integer>  {
	
	 Optional<User> findByEmail(String email);
	 Optional<User> findByTelefone(String telefone);

}
