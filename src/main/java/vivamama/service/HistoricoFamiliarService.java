package vivamama.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vivamama.dto.HistoricoFamiliarAnexoResponse;
import vivamama.dto.HistoricoFamiliarSnapshotResponse;
import vivamama.model.HistoricoFamiliar;
import vivamama.model.HistoricoFamiliarAnexo;
import vivamama.model.Paciente;
import vivamama.repository.HistoricoFamiliarAnexoRepository;
import vivamama.repository.HistoricoFamiliarRepository;
import vivamama.repository.PacienteRepository;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

@Service
public class HistoricoFamiliarService {

    @Value("${app.upload-dir}")
    private String uploadDir;

    private final HistoricoFamiliarRepository historicoRepo;
    private final HistoricoFamiliarAnexoRepository anexoRepo;
    private final PacienteRepository pacienteRepo;

    public HistoricoFamiliarService(HistoricoFamiliarRepository historicoRepo,
                                   HistoricoFamiliarAnexoRepository anexoRepo,
                                   PacienteRepository pacienteRepo) {
        this.historicoRepo = historicoRepo;
        this.anexoRepo = anexoRepo;
        this.pacienteRepo = pacienteRepo;
    }

    public HistoricoFamiliarSnapshotResponse criarSnapshot(int pacienteId, String textoHistorico, Integer criadoPorUserId) {
        if (textoHistorico == null || textoHistorico.trim().isEmpty()) {
            throw new IllegalArgumentException("textoHistorico é obrigatório");
        }

        Paciente paciente = pacienteRepo.findById(pacienteId)
                .orElseThrow(() -> new IllegalArgumentException("Paciente não encontrado: " + pacienteId));

        int versao = historicoRepo.findMaxVersaoByPacienteId(pacienteId) + 1;

        HistoricoFamiliar h = new HistoricoFamiliar();
        h.setPaciente(paciente);
        h.setTextoHistorico(textoHistorico);
        h.setVersao(versao);
        h.setCriadoPorUserId(criadoPorUserId);

        HistoricoFamiliar salvo = historicoRepo.save(h);
        return toResponse(salvo, List.of());
    }

    public List<HistoricoFamiliarSnapshotResponse> listarPorPaciente(int pacienteId) {
        List<HistoricoFamiliar> historicos = historicoRepo.findByPaciente_IdPacienteOrderByIdDesc(pacienteId);

        // Normalização: versões/createdAt para registros antigos (pré-colunas novas)
        boolean precisaNormalizar = historicos.stream().anyMatch(h -> h.getVersao() == null || h.getCriadoEm() == null);
        if (precisaNormalizar && !historicos.isEmpty()) {
            List<HistoricoFamiliar> asc = new ArrayList<>(historicos);
            asc.sort(Comparator.comparing(HistoricoFamiliar::getId));
            int v = 1;
            for (HistoricoFamiliar h : asc) {
                h.setVersao(v++);
                if (h.getCriadoEm() == null) {
                    h.setCriadoEm(java.time.LocalDateTime.now());
                }
            }
            historicoRepo.saveAll(asc);
            historicos = historicoRepo.findByPaciente_IdPacienteOrderByIdDesc(pacienteId);
        }

        // Migração leve: se existirem registros antigos com caminhosExames, cria registros de anexo.
        for (HistoricoFamiliar h : historicos) {
            migrarCaminhosAntigosParaAnexosSeNecessario(h);
        }

        List<HistoricoFamiliarSnapshotResponse> out = new ArrayList<>();
        for (HistoricoFamiliar h : historicos) {
            List<HistoricoFamiliarAnexo> anexos = anexoRepo.findByHistorico_IdOrderByIdDesc(h.getId());
            out.add(toResponse(h, anexos));
        }
        return out;
    }

    public List<HistoricoFamiliarAnexoResponse> adicionarAnexos(Long historicoId, List<MultipartFile> arquivos, Integer enviadoPorUserId) throws IOException {
        if (arquivos == null || arquivos.isEmpty()) return List.of();

        HistoricoFamiliar historico = historicoRepo.findById(historicoId)
                .orElseThrow(() -> new IllegalArgumentException("Histórico não encontrado: " + historicoId));

        Path pastaUploads = Paths.get(uploadDir);
        Files.createDirectories(pastaUploads);

        List<HistoricoFamiliarAnexoResponse> created = new ArrayList<>();
        for (MultipartFile arquivo : arquivos) {
            if (arquivo == null || arquivo.isEmpty()) continue;

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

            HistoricoFamiliarAnexo a = new HistoricoFamiliarAnexo();
            a.setHistorico(historico);
            a.setNomeOriginal(original);
            a.setNomeArmazenado(nomeArmazenado);
            a.setContentType(arquivo.getContentType());
            a.setTamanho(arquivo.getSize());
            a.setEnviadoPorUserId(enviadoPorUserId);

            HistoricoFamiliarAnexo saved = anexoRepo.save(a);
            created.add(toResponse(saved));
        }
        return created;
    }

    public Optional<HistoricoFamiliarAnexo> buscarAnexo(Long anexoId) {
        return anexoRepo.findById(anexoId);
    }

    public Path resolveAnexoPath(String nomeArmazenado) {
        return Paths.get(uploadDir).resolve(nomeArmazenado).toAbsolutePath().normalize();
    }

    private void migrarCaminhosAntigosParaAnexosSeNecessario(HistoricoFamiliar h) {
        String antigos = h.getCaminhosExames();
        if (antigos == null || antigos.isBlank()) return;

        // Se já tem anexos na tabela nova, não duplica.
        List<HistoricoFamiliarAnexo> existentes = anexoRepo.findByHistorico_IdOrderByIdDesc(h.getId());
        if (!existentes.isEmpty()) return;

        String[] nomes = antigos.split(";");
        for (String n : nomes) {
            String nome = (n == null ? "" : n.trim());
            if (nome.isEmpty()) continue;

            HistoricoFamiliarAnexo a = new HistoricoFamiliarAnexo();
            a.setHistorico(h);
            a.setNomeOriginal(nome);
            a.setNomeArmazenado(nome);
            a.setContentType(null);
            a.setTamanho(null);
            a.setEnviadoPorUserId(h.getCriadoPorUserId());
            anexoRepo.save(a);
        }

        // opcional: limpar o campo antigo para não re-migrar.
        h.setCaminhosExames(null);
        historicoRepo.save(h);
    }

    private HistoricoFamiliarSnapshotResponse toResponse(HistoricoFamiliar h, List<HistoricoFamiliarAnexo> anexos) {
        HistoricoFamiliarSnapshotResponse r = new HistoricoFamiliarSnapshotResponse();
        r.id = h.getId();
        r.pacienteId = h.getPaciente() != null ? h.getPaciente().getIdPaciente() : null;
        r.versao = h.getVersao();
        r.criadoEm = h.getCriadoEm();
        r.criadoPorUserId = h.getCriadoPorUserId();
        r.textoHistorico = h.getTextoHistorico();

        List<HistoricoFamiliarAnexoResponse> list = new ArrayList<>();
        for (HistoricoFamiliarAnexo a : anexos) {
            list.add(toResponse(a));
        }
        r.anexos = list;
        return r;
    }

    private HistoricoFamiliarAnexoResponse toResponse(HistoricoFamiliarAnexo a) {
        HistoricoFamiliarAnexoResponse r = new HistoricoFamiliarAnexoResponse();
        r.id = a.getId();
        r.nomeOriginal = a.getNomeOriginal();
        r.contentType = a.getContentType();
        r.tamanho = a.getTamanho();
        r.enviadoEm = a.getEnviadoEm();
        return r;
    }
}
