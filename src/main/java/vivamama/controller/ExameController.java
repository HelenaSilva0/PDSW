package vivamama.controller;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import vivamama.dto.ExameResponse;
import vivamama.security.AuthzService;
import vivamama.service.ExameService;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Controller
@RequestMapping("/exames")
public class ExameController {

    private final ExameService exameService;
    private final AuthzService authz;

    public ExameController(ExameService exameService, AuthzService authz) {
        this.exameService = exameService;
        this.authz = authz;
    }

    // Só redireciona pro HTML estático
    @GetMapping("/novo")
    public String mostrarPagina() {
        return "redirect:/Exames.html";
    }

    // Upload de 1 exame com descrição
    @PreAuthorize("hasRole('MEDICO') or hasRole('PACIENTE')")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public ResponseEntity<ExameResponse> upload(
            @RequestParam("pacienteId") Integer pacienteId,
            @RequestParam("descricao") String descricao,
            @RequestParam("arquivo") MultipartFile arquivo
    ) throws IOException {

        boolean isOwner = authz.isPacienteOwnerByPacienteId(pacienteId);
        boolean isMedico = org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication().getAuthorities().stream()
                .anyMatch(a -> "ROLE_MEDICO".equals(a.getAuthority()));
        if (!isMedico && !isOwner) {
            return ResponseEntity.status(403).build();
        }

        Integer userId = authz.currentUserId();
        ExameResponse resp = exameService.adicionarExame(pacienteId, descricao, arquivo, userId);
        return ResponseEntity.ok(resp);
    }

    @PreAuthorize("hasRole('MEDICO') or (hasRole('PACIENTE') and @authz.isPacienteOwnerByPacienteId(#idPaciente))")
    @GetMapping("/paciente/{idPaciente}")
    @ResponseBody
    public List<ExameResponse> listarPorPaciente(@PathVariable int idPaciente) {
        return exameService.listarPorPaciente(idPaciente);
    }

    @PreAuthorize("hasRole('MEDICO') or (hasRole('PACIENTE') and @authz.isPacienteOwnerByExameId(#exameId))")
    @GetMapping("/{exameId}")
    public ResponseEntity<Resource> baixar(@PathVariable Long exameId) throws MalformedURLException {

        var exameOpt = exameService.buscar(exameId);
        if (exameOpt.isEmpty()) return ResponseEntity.notFound().build();

        var exame = exameOpt.get();
        Path path = exameService.resolvePath(exame.getNomeArmazenado());

        if (!Files.exists(path)) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new UrlResource(path.toUri());
        String contentType = exame.getContentType();
        MediaType mt = (contentType == null || contentType.isBlank())
                ? MediaType.APPLICATION_OCTET_STREAM
                : MediaType.parseMediaType(contentType);

        String fileName = exame.getNomeOriginal() == null ? "arquivo" : exame.getNomeOriginal();
        String encoded = java.net.URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");

        return ResponseEntity.ok()
                .contentType(mt)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encoded)
                .body(resource);
    }
}
