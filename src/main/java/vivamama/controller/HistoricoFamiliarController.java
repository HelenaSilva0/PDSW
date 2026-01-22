package vivamama.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import vivamama.dto.HistoricoFamiliarAnexoResponse;
import vivamama.dto.HistoricoFamiliarSnapshotRequest;
import vivamama.dto.HistoricoFamiliarSnapshotResponse;
import vivamama.security.AuthzService;
import vivamama.service.HistoricoFamiliarService;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Controller
@RequestMapping("/historico-familiar")
public class HistoricoFamiliarController {

    private final HistoricoFamiliarService historicoService;
    private final AuthzService authz;

    public HistoricoFamiliarController(HistoricoFamiliarService historicoService, AuthzService authz) {
        this.historicoService = historicoService;
        this.authz = authz;
    }

    // Só redireciona pro HTML estático
    @GetMapping("/novo")
    public String mostrarFormulario() {
        return "redirect:/HistoricoFamiliar.html";
    }

    // ---- NOVO: cria snapshot (texto) ----
    @PreAuthorize("hasRole('MEDICO') or (hasRole('PACIENTE') and @authz.isPacienteOwnerByPacienteId(#req.pacienteId))")
    @PostMapping(value = "/snapshots", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<HistoricoFamiliarSnapshotResponse> criarSnapshot(@RequestBody HistoricoFamiliarSnapshotRequest req) {
        int pacienteId = req.pacienteId == null ? 0 : req.pacienteId;
        Integer userId = authz.currentUserId();
        HistoricoFamiliarSnapshotResponse created = historicoService.criarSnapshot(pacienteId, req.textoHistorico, userId);
        return ResponseEntity.ok(created);
    }

    // ---- NOVO: adiciona anexos a um snapshot já criado ----
    @PreAuthorize("hasRole('MEDICO') or (hasRole('PACIENTE') and @authz.isPacienteOwnerByHistoricoId(#historicoId))")
    @PostMapping(value = "/snapshots/{historicoId}/anexos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public ResponseEntity<List<HistoricoFamiliarAnexoResponse>> adicionarAnexos(
            @PathVariable Long historicoId,
            @RequestParam(value = "exames", required = false) List<MultipartFile> exames
    ) throws IOException {
        Integer userId = authz.currentUserId();
        List<HistoricoFamiliarAnexoResponse> anexos = historicoService.adicionarAnexos(historicoId, exames, userId);
        return ResponseEntity.ok(anexos);
    }

    // ---- LISTA: snapshots por paciente (com anexos) ----
    @PreAuthorize("hasRole('MEDICO') or (hasRole('PACIENTE') and @authz.isPacienteOwnerByPacienteId(#idPaciente))")
    @GetMapping("/paciente/{idPaciente}")
    @ResponseBody
    public List<HistoricoFamiliarSnapshotResponse> listarPorPaciente(@PathVariable int idPaciente) {
        return historicoService.listarPorPaciente(idPaciente);
    }

    // ---- DOWNLOAD autenticado de anexo ----
    @PreAuthorize("hasRole('MEDICO') or (hasRole('PACIENTE') and @authz.isPacienteOwnerByAnexoId(#anexoId))")
    @GetMapping("/anexos/{anexoId}")
    public ResponseEntity<Resource> baixarAnexo(@PathVariable Long anexoId) throws MalformedURLException {

        var anexoOpt = historicoService.buscarAnexo(anexoId);
        if (anexoOpt.isEmpty()) return ResponseEntity.notFound().build();

        var anexo = anexoOpt.get();
        Path path = historicoService.resolveAnexoPath(anexo.getNomeArmazenado());

        if (!Files.exists(path)) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new UrlResource(path.toUri());
        String contentType = anexo.getContentType();
        MediaType mt = (contentType == null || contentType.isBlank())
                ? MediaType.APPLICATION_OCTET_STREAM
                : MediaType.parseMediaType(contentType);

        String fileName = anexo.getNomeOriginal() == null ? "arquivo" : anexo.getNomeOriginal();
        String encoded = java.net.URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");

        return ResponseEntity.ok()
                .contentType(mt)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encoded)
                .body(resource);
    }

    // ---- LEGADO: mantém endpoints antigos ----

    @PreAuthorize("hasRole('MEDICO') or @authz.isPacienteOwnerByPacienteId(#pacienteId)")
    @PostMapping("/salvar")
    public String salvarLegacyRedirect(
            @RequestParam("textoHistorico") String textoHistorico,
            @RequestParam(value = "exames", required = false) List<MultipartFile> exames,
            @RequestParam("pacienteId") Integer pacienteId
    ) throws IOException {
        salvarLegacy(textoHistorico, exames, pacienteId);
        return "redirect:/HistoricoFamiliar.html?sucesso";
    }

    @PreAuthorize("hasRole('MEDICO') or (hasRole('PACIENTE') and @authz.isPacienteOwnerByPacienteId(#pacienteId))")
    @PostMapping(value = "/salvar-json", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public ResponseEntity<HistoricoFamiliarSnapshotResponse> salvarLegacyJson(
            @RequestParam("textoHistorico") String textoHistorico,
            @RequestParam(value = "exames", required = false) List<MultipartFile> exames,
            @RequestParam("pacienteId") Integer pacienteId
    ) throws IOException {
        HistoricoFamiliarSnapshotResponse resp = salvarLegacy(textoHistorico, exames, pacienteId);
        return ResponseEntity.ok(resp);
    }

    private HistoricoFamiliarSnapshotResponse salvarLegacy(String textoHistorico, List<MultipartFile> exames, Integer pacienteId) throws IOException {
        Integer userId = authz.currentUserId();
        HistoricoFamiliarSnapshotResponse snapshot = historicoService.criarSnapshot(pacienteId, textoHistorico, userId);
        historicoService.adicionarAnexos(snapshot.id, exames, userId);
        return historicoService.listarPorPaciente(pacienteId).stream()
                .filter(h -> h.id != null && h.id.equals(snapshot.id))
                .findFirst()
                .orElse(snapshot);
    }
}
