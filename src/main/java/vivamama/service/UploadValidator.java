package vivamama.service;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
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
        if (!hasExpectedSignature(file, extension)) {
            throw new IllegalArgumentException("O conteúdo do arquivo não corresponde ao formato informado.");
        }
        return safeName;
    }

    private boolean hasExpectedSignature(MultipartFile file, String extension) {
        try (InputStream input = file.getInputStream()) {
            byte[] header = input.readNBytes(12);
            return switch (extension) {
                case "pdf" -> startsWith(header, new byte[]{'%', 'P', 'D', 'F', '-'});
                case "jpg", "jpeg" -> startsWith(header, new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF});
                case "png" -> startsWith(header, new byte[]{(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A});
                case "webp" -> startsWith(header, new byte[]{'R', 'I', 'F', 'F'})
                        && header.length >= 12
                        && Arrays.equals(Arrays.copyOfRange(header, 8, 12), new byte[]{'W', 'E', 'B', 'P'});
                default -> false;
            };
        } catch (IOException error) {
            return false;
        }
    }

    private boolean startsWith(byte[] value, byte[] prefix) {
        return value.length >= prefix.length && Arrays.equals(Arrays.copyOf(value, prefix.length), prefix);
    }
}
