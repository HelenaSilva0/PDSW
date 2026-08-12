package vivamama.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class MedicoProfile {
	
	 public Integer userId;
	 @NotBlank @Size(max = 100) public String nome;
	    @NotBlank @Size(max = 20) public String crm;
	    @NotBlank @Size(max = 100) public String especialidade;

}
