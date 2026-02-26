package vivamama.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import vivamama.model.HistoricoFamiliar;   

public interface HistoricoFamiliarRepository extends JpaRepository<HistoricoFamiliar, Long> {
	
	 interface HistoricoResumo {
	        Integer getPacienteId();
	        Long getTotal();
	        LocalDateTime getUltimoCriadoEm();
	    }

	 List<HistoricoFamiliar> findByPaciente_IdPacienteOrderByIdDesc(int idPaciente);
	 @Query("select coalesce(max(h.versao), 0) from HistoricoFamiliar h where h.paciente.idPaciente = :idPaciente")
	    int findMaxVersaoByPacienteId(@Param("idPaciente") int idPaciente);
	 
	 @Query("select h.paciente.idPaciente as pacienteId, count(h) as total, max(h.criadoEm) as ultimoCriadoEm " +
	           "from HistoricoFamiliar h where h.paciente.idPaciente in :ids group by h.paciente.idPaciente")
	    List<HistoricoResumo> resumoPorPacientes(@Param("ids") List<Integer> ids);
	}


   
