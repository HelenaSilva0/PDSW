package vivamama.repository;

import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import vivamama.model.User;
import vivamama.model.UserStatus;
import vivamama.model.UserType;

public interface UserRepository extends JpaRepository <User, Integer>  {
	
	 Optional<User> findByEmail(String email);
	 Optional<User> findByTelefone(String telefone);
	 List<User> findByRoleAndStatusOrderByIdAsc(UserType role, UserStatus status);

}
