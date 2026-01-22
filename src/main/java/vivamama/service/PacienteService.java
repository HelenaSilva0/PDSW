package vivamama.service;

import vivamama.model.Paciente;
import vivamama.repository.PacienteRepository;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.Optional;

@Service
public class PacienteService {
	
	 private final PacienteRepository pacienteRepository;

	    public PacienteService(PacienteRepository pacienteRepository) {
	        this.pacienteRepository = pacienteRepository;
	    }

	    public Paciente salvarPaciente(@NonNull Paciente paciente) {
	        return pacienteRepository.save(paciente);
	    }

	    public List<Paciente> listarPacientes() {
	        return pacienteRepository.findAll();
	    }

	    public Optional<Paciente> buscarPacientePorId(@NonNull Integer id) {
	        return pacienteRepository.findById(id);
	    }

	    public Optional<Paciente> buscarPacientePorUserId(@NonNull Integer userId) {
	        return pacienteRepository.findByUser_Id(userId); // ou findByUser_Id(userId)
	    }

	    public Optional<Paciente> atualizarPaciente(@NonNull Integer id, Paciente atualizado) {
	        return pacienteRepository.findById(id)
	                .map(p -> {
	                    aplicarAtualizacao(p, atualizado);
	                    return pacienteRepository.save(p);
	                });
	    }

	    public Optional<Paciente> atualizarPacientePorUserId(@NonNull Integer userId, Paciente atualizado) {
	        return pacienteRepository.findByUser_Id(userId) // ou findByUser_Id(userId)
	                .map(p -> {
	                    aplicarAtualizacao(p, atualizado);
	                    return pacienteRepository.save(p);
	                });
	    }

	    public boolean excluirPaciente(@NonNull Integer id) {
	        return pacienteRepository.findById(id)
	                .map(p -> { pacienteRepository.delete(p); return true; })
	                .orElse(false);
	    }

	    public boolean excluirPacientePorUserId(@NonNull Integer userId) {
	        return pacienteRepository.findByUser_Id(userId) // ou findByUser_Id(userId)
	                .map(p -> { pacienteRepository.delete(p); return true; })
	                .orElse(false);
	    }

	    private void aplicarAtualizacao(Paciente paciente, Paciente atualizado) {
	    	if (atualizado.getNome() != null) paciente.setNome(atualizado.getNome());
	        if (atualizado.getDataNascimento() != null) paciente.setDataNascimento(atualizado.getDataNascimento());
	        if (atualizado.getHistoricoFamiliar() != null) paciente.setHistoricoFamiliar(atualizado.getHistoricoFamiliar());
	        if (atualizado.getObservacoes() != null) paciente.setObservacoes(atualizado.getObservacoes());
	        if (atualizado.getGenero() != '\u0000') paciente.setGenero(atualizado.getGenero());
	    }
	}
