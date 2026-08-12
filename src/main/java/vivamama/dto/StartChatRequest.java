package vivamama.dto;

import jakarta.validation.constraints.NotNull;

public class StartChatRequest {
	@NotNull
	private Integer pacienteId;

	public Integer getPacienteId() {
		return pacienteId;
	}

	public void setPacienteId(Integer pacienteId) {
		this.pacienteId = pacienteId;
	}
}
