package vivamama.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vivamama.dto.ExameResponse;
import vivamama.model.Exame;
import vivamama.model.Paciente;
import vivamama.repository.ExameRepository;
import vivamama.repository.PacienteRepository;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

@Service
public class ExameService {

    @Value("${app.upload-dir}")
    private String uploadDir;

    private final ExameRepository exameRepo;
    private final PacienteRepository pacienteRepo;

    public ExameService(ExameRepository exameRepo, PacienteRepository pacienteRepo) {
        this.exameRepo = exameRepo;
        this.pacienteRepo = pacienteRepo;
    }

    public ExameResponse adicionarExame(int pacienteId, String descricao, MultipartFile arquivo, Integer enviadoPorUserId) throws IOException {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new IllegalArgumentException("Arquivo do exame é obrigatório");
        }
        if (descricao == null || descricao.trim().isEmpty()) {
            throw new IllegalArgumentException("Descrição do exame é obrigatória");
        }

        Paciente paciente = pacienteRepo.findById(pacienteId)
                .orElseThrow(() -> new IllegalArgumentException("Paciente não encontrado: " + pacienteId));

        int versao = exameRepo.findMaxVersaoByPacienteId(pacienteId) + 1;

        Path pastaUploads = Paths.get(uploadDir);
        Files.createDirectories(pastaUploads);

        String original = Optional.ofNullable(arquivo.getOriginalFilename()).orElse("arquivo");
        original = original.replaceAll("[\\\\/]", "_");

        String ext = "";
        int dot = original.lastIndexOf('.');
        if (dot >= 0 && dot < original.length() - 1) {
            ext = original.substring(dot);
        }

        String nomeArmazenado = UUID.randomUUID() + ext;
        Path destino = pastaUploads.resolve(nomeArmazenado);
        Files.copy(arquivo.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);

        Exame e = new Exame();
        e.setPaciente(paciente);
        e.setDescricao(descricao.trim());
        e.setNomeOriginal(original);
        e.setNomeArmazenado(nomeArmazenado);
        e.setContentType(arquivo.getContentType());
        e.setTamanho(arquivo.getSize());
        e.setVersao(versao);
        e.setEnviadoPorUserId(enviadoPorUserId);

        Exame saved = exameRepo.save(e);
        return toResponse(saved);
    }

    public List<ExameResponse> listarPorPaciente(int pacienteId) {
        List<Exame> exames = exameRepo.findByPaciente_IdPacienteOrderByIdDesc(pacienteId);

        // Normalização leve para registros antigos (se existir)
        boolean precisa = exames.stream().anyMatch(e -> e.getVersao() == null || e.getEnviadoEm() == null);
        if (precisa && !exames.isEmpty()) {
            List<Exame> asc = new ArrayList<>(exames);
            asc.sort(Comparator.comparing(Exame::getId));
            int v = 1;
            for (Exame e : asc) {
                if (e.getVersao() == null) e.setVersao(v);
                v++;
                if (e.getEnviadoEm() == null) e.setEnviadoEm(java.time.LocalDateTime.now());
            }
            exameRepo.saveAll(asc);
            exames = exameRepo.findByPaciente_IdPacienteOrderByIdDesc(pacienteId);
        }

        List<ExameResponse> out = new ArrayList<>();
        for (Exame e : exames) out.add(toResponse(e));
        return out;
    }

    public Optional<Exame> buscar(Long id) {
        return exameRepo.findById(id);
    }

    public Path resolvePath(String nomeArmazenado) {
        return Paths.get(uploadDir).resolve(nomeArmazenado).toAbsolutePath().normalize();
    }

    private ExameResponse toResponse(Exame e) {
        ExameResponse r = new ExameResponse();
        r.id = e.getId();
        r.pacienteId = e.getPaciente() != null ? e.getPaciente().getIdPaciente() : null;
        r.versao = e.getVersao();
        r.descricao = e.getDescricao();
        r.nomeOriginal = e.getNomeOriginal();
        r.contentType = e.getContentType();
        r.tamanho = e.getTamanho();
        r.enviadoEm = e.getEnviadoEm();
        r.enviadoPorUserId = e.getEnviadoPorUserId();
        return r;
    }
}
