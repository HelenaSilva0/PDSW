package vivamama.dto;

import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import vivamama.model.UserType;

public class RegisterRequest {
    @Size(max = 120)
    public String email;
    @Size(min = 8, max = 255)
    public String senha;
    @Size(max = 20)
    public String telefone;
    public UserType role;

    @Size(max = 100)
    public String nome;
    public LocalDate dataNascimento;
    @Size(max = 4000)
    public String observacoes;
    public Character genero;

    @Size(max = 20)
    public String crm;
    @Size(max = 100)
    public String especialidade;
    @Size(max = 200)
    public String codigoMedico;
}
