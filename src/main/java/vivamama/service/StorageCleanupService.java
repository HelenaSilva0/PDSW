package vivamama.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import vivamama.repository.ExameRepository;
import vivamama.repository.HistoricoFamiliarAnexoRepository;

@Service
public class StorageCleanupService {
    private final String uploadDir;
    private final ExameRepository exames;
    private final HistoricoFamiliarAnexoRepository anexos;

    public StorageCleanupService(@Value("${app.upload-dir}") String uploadDir,
            ExameRepository exames, HistoricoFamiliarAnexoRepository anexos) {
        this.uploadDir = uploadDir; this.exames = exames; this.anexos = anexos;
    }

    @Scheduled(cron = "0 30 3 * * *")
    public void removerOrfaos() {
        Path root = Paths.get(uploadDir).toAbsolutePath().normalize();
        if (!Files.isDirectory(root)) return;
        Set<String> usados = new HashSet<>();
        exames.findAll().forEach(e -> { if (e.getNomeArmazenado() != null) usados.add(e.getNomeArmazenado()); });
        anexos.findAll().forEach(a -> { if (a.getNomeArmazenado() != null) usados.add(a.getNomeArmazenado()); });
        try (var files = Files.list(root)) {
            files.filter(Files::isRegularFile).filter(path -> !usados.contains(path.getFileName().toString()))
                    .forEach(path -> { try { Files.deleteIfExists(path); } catch (Exception ignored) { } });
        } catch (Exception ignored) { }
    }
}
