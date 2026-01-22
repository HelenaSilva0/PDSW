package vivamama.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vivamama.model.HistoricoFamiliarAnexo;

import java.util.List;

public interface HistoricoFamiliarAnexoRepository extends JpaRepository<HistoricoFamiliarAnexo, Long> {
    List<HistoricoFamiliarAnexo> findByHistorico_IdOrderByIdDesc(Long historicoId);
    List<HistoricoFamiliarAnexo> findByHistorico_Paciente_IdPacienteOrderByIdDesc(int idPaciente);
}
