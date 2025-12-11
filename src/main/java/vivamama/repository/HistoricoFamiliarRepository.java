package vivamama.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vivamama.model.HistoricoFamiliar;   // <-- FALTA ESSE IMPORT

public interface HistoricoFamiliarRepository extends JpaRepository<HistoricoFamiliar, Long> {
}
