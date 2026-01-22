package vivamama.repository;

import vivamama.model.Paciente;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

	public interface PacienteRepository extends JpaRepository<Paciente, Integer> {
		Optional<Paciente> findByUser_Id(Integer userId);
	}
