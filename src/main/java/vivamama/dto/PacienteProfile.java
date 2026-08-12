package vivamama.dto;

import java.time.LocalDate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PacienteProfile {
	
	public Integer userId;
	@NotBlank @Size(max = 100)
    public String nome;
    public LocalDate dataNascimento;
    @Size(max = 4000) public String historicoFamiliar;
    @Size(max = 4000)
    public String observacoes;
    public char genero;

}
