package vivamama.repository;

import vivamama.model.Paciente;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

	public interface PacienteRepository extends JpaRepository<Paciente, Integer> {
	Optional<Paciente> findByUser_Id(Integer userId);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select p from Paciente p where p.idPaciente = :id")
	Optional<Paciente> findByIdForUpdate(@Param("id") Integer id);
	}
