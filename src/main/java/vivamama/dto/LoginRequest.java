package vivamama.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class LoginRequest {

    @NotBlank @Email @Size(max = 120)
    public String email;
    @NotBlank @Size(max = 255)
    public String senha;

}
