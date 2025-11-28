package vivamama.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import vivamama.model.Medico;
import vivamama.repository.MedicoRepository;

import java.util.List;
import java.util.Optional;

@Service
public class MedicoService {

    @Autowired
    private MedicoRepository repo;

    public Medico salvar(@NonNull Medico m) { return repo.save(m); }

    public List<Medico> listar() { return repo.findAll(); }

    public Optional<Medico> buscarPorId(@NonNull Integer id) { return repo.findById(id); }

    public Optional<Medico> atualizar(@NonNull Integer id, Medico novo) {
        return repo.findById(id).map(m -> {
            m.setNome(novo.getNome());
            //m.setEmail(novo.getEmail());
            //m.setSenha(novo.getSenha());
            m.setCrm(novo.getCrm());
            m.setEspecialidade(novo.getEspecialidade());
            return repo.save(m);
        });
    }

    public boolean excluir(@NonNull Integer id) {
        return repo.findById(id).map(m -> { repo.delete(m); return true; }).orElse(false);
    }
}
