package vivamama.controller;

import vivamama.model.HistoricoFamiliar;
import vivamama.repository.HistoricoFamiliarRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/historico-familiar")
public class HistoricoFamiliarController {

    private final HistoricoFamiliarRepository repository;

    public HistoricoFamiliarController(HistoricoFamiliarRepository repository) {
        this.repository = repository;
    }

    // Só REDIRECIONA para o HTML estático
    @GetMapping("/novo")
    public String mostrarFormulario() {
        return "redirect:/HistoricoFamiliar.html";
    }

    @PostMapping("/salvar")
    public String salvar(
            @RequestParam("textoHistorico") String textoHistorico,
            @RequestParam("exames") List<MultipartFile> exames
    ) throws IOException {

        // pasta local onde os arquivos vão ficar
        Path pastaUploads = Paths.get("uploads/exames");
        Files.createDirectories(pastaUploads);

        List<String> caminhos = new ArrayList<>();

        if (exames != null) {
            for (MultipartFile arquivo : exames) {
                if (!arquivo.isEmpty()) {
                    String nomeArquivo = System.currentTimeMillis() + "_" + arquivo.getOriginalFilename();
                    Path destino = pastaUploads.resolve(nomeArquivo);
                    Files.copy(arquivo.getInputStream(), destino);
                    caminhos.add(destino.toString());
                }
            }
        }

        HistoricoFamiliar historico = new HistoricoFamiliar();
        historico.setTextoHistorico(textoHistorico);

        if (!caminhos.isEmpty()) {
            historico.setCaminhosExames(String.join(";", caminhos));
        }

        repository.save(historico);

        // volta para a página do form, com flag de sucesso
        return "redirect:/HistoricoFamiliar.html?sucesso";
    }
}
