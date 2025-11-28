package vivamama.repository;

import vivamama.model.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;

	public interface PacienteRepository extends JpaRepository<Paciente, Integer> {
		
	}
