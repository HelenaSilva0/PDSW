package vivamama.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class SendChatMessageRequest {
    @NotBlank @Size(max = 2000)
    private String texto;
    public String getTexto() { return texto; }
    public void setTexto(String texto) { this.texto = texto; }
}
