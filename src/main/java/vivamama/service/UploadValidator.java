package vivamama.service;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class UploadValidator {
    private static final long MAX_SIZE = 10L * 1024 * 1024;
    private static final Map<String, String> TYPES = Map.of(
            "pdf", "application/pdf", "jpg", "image/jpeg", "jpeg", "image/jpeg",
            "png", "image/png", "webp", "image/webp"
    );

    public String validate(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("Arquivo é obrigatório.");
        if (file.getSize() > MAX_SIZE) throw new IllegalArgumentException("O arquivo deve ter no máximo 10 MB.");
        String original = Optional.ofNullable(file.getOriginalFilename()).orElse("arquivo");
        String safeName = original.replace('\\', '_').replace('/', '_').replaceAll("[\\p{Cntrl}]", "");
        String extension = safeName.contains(".") ? safeName.substring(safeName.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT) : "";
        String expected = TYPES.get(extension);
        if (expected == null || file.getContentType() == null || !expected.equalsIgnoreCase(file.getContentType())) {
            throw new IllegalArgumentException("Formato inválido. Use PDF, JPG, PNG ou WEBP.");
        }
        return safeName;
    }
}
