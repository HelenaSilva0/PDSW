package vivamama.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vivamama.model.Medico;

public interface MedicoRepository extends JpaRepository<Medico, Integer> {
	
}
