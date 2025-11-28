package vivamama.service;

import vivamama.model.Paciente;
import vivamama.repository.PacienteRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.Optional;

@Service
public class PacienteService {
	
	 @Autowired
	    private PacienteRepository pacienteRepository;

	    public Paciente salvarPaciente(@NonNull Paciente paciente) {
	        return pacienteRepository.save(paciente);
	    }

	    public List<Paciente> listarPacientes() {
	        return pacienteRepository.findAll();
	    }

	    public Optional<Paciente> buscarPacientePorId(@NonNull Integer id) {
	        return pacienteRepository.findById(id);
	    }

	    public Optional<Paciente> atualizarPaciente(@NonNull Integer id, Paciente pacienteAtualizado) {
	        return pacienteRepository.findById(id)
	                .map(paciente -> {
	                    paciente.setNome(pacienteAtualizado.getNome());
	                    paciente.setDataNascimento(pacienteAtualizado.getDataNascimento());
	                    paciente.setHistoricoFamiliar(pacienteAtualizado.getHistoricoFamiliar());
	                    paciente.setObservacoes(pacienteAtualizado.getObservacoes());
	                    paciente.setGenero(pacienteAtualizado.getGenero());
	                    return pacienteRepository.save(paciente);
	                });
	    }

	    public boolean excluirPaciente(@NonNull Integer id) {
	        return pacienteRepository.findById(id)
	                .map(paciente -> {
	                    pacienteRepository.delete(paciente);
	                    return true;
	                })
	                .orElse(false);
	    }
	}
