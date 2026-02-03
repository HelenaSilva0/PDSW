package vivamama.repository;


import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import vivamama.model.Chat;

public interface ChatRepository extends JpaRepository<Chat, Long> {

    Optional<Chat> findByMedico_IdMedicoAndPaciente_IdPaciente(int medicoId, int pacienteId);

    List<Chat> findByMedico_IdMedicoOrderByCriadoEmDesc(int medicoId);

    List<Chat> findByPaciente_IdPacienteOrderByCriadoEmDesc(int pacienteId);
}