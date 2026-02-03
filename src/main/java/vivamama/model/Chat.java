package vivamama.model;

import java.time.Instant;

import jakarta.persistence.*;

@Entity
public class Chat {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "medico_id")
	private Medico medico;

	@ManyToOne(optional = false)
	@JoinColumn(name = "paciente_id")
	private Paciente paciente;

	private Instant criadoEm = Instant.now();

	public Long getId() {
		return id;
	}

	public Medico getMedico() {
		return medico;
	}

	public void setMedico(Medico medico) {
		this.medico = medico;
	}

	public Paciente getPaciente() {
		return paciente;
	}

	public void setPaciente(Paciente paciente) {
		this.paciente = paciente;
	}

	public Instant getCriadoEm() {
		return criadoEm;
	}
}
