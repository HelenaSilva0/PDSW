package vivamama.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vivamama.model.Exame;

import java.util.List;

public interface ExameRepository extends JpaRepository<Exame, Long> {

    List<Exame> findByPaciente_IdPacienteOrderByIdDesc(int idPaciente);

    @Query("select coalesce(max(e.versao), 0) from Exame e where e.paciente.idPaciente = :idPaciente")
    int findMaxVersaoByPacienteId(@Param("idPaciente") int idPaciente);
}
