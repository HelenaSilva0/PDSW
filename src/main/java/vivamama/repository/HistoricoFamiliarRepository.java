package vivamama.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import vivamama.model.HistoricoFamiliar;   

public interface HistoricoFamiliarRepository extends JpaRepository<HistoricoFamiliar, Long> {
	 List<HistoricoFamiliar> findByPaciente_IdPacienteOrderByIdDesc(int idPaciente);
	 @Query("select coalesce(max(h.versao), 0) from HistoricoFamiliar h where h.paciente.idPaciente = :idPaciente")
	    int findMaxVersaoByPacienteId(@Param("idPaciente") int idPaciente);
	}


   
