package vivamama.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vivamama.model.Exame;

import java.time.LocalDateTime;
import java.util.List;

public interface ExameRepository extends JpaRepository<Exame, Long> {

	 interface ExameResumo {
	        Integer getPacienteId();
	        Long getTotal();
	        LocalDateTime getUltimoEnviadoEm();
	    }

	    List<Exame> findByPaciente_IdPacienteOrderByIdDesc(int idPaciente);
    void deleteByPaciente_IdPaciente(int idPaciente);

	    @Query("select coalesce(max(e.versao), 0) from Exame e where e.paciente.idPaciente = :idPaciente")
	    int findMaxVersaoByPacienteId(@Param("idPaciente") int idPaciente);

	    @Query("select e.paciente.idPaciente as pacienteId, count(e) as total, max(e.enviadoEm) as ultimoEnviadoEm " +
	           "from Exame e where e.paciente.idPaciente in :ids group by e.paciente.idPaciente")
	    List<ExameResumo> resumoPorPacientes(@Param("ids") List<Integer> ids);
}
