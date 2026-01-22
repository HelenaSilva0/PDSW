package vivamama.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import vivamama.model.Medico;

public interface MedicoRepository extends JpaRepository<Medico, Integer> {
    Optional<Medico> findByUser_Id(Integer userId);
    Optional<Medico> findByCrm(String crm);
}
