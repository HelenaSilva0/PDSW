package vivamama.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;

import java.time.LocalDate;


@Entity
public class Paciente {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int idPaciente;

	@OneToOne @JoinColumn(name="user_id",nullable=false,unique=true)

	private User user;

	@Column(length = 100, nullable = false)
	private String nome;
	@Column(name = "data_nascimento")
	private LocalDate dataNascimento;
	private String historicoFamiliar;
	private String observacoes;
	private char genero;

	public int getIdPaciente() {
		return idPaciente;
	}

	public void setIdPaciente(int idPaciente) {
		this.idPaciente = idPaciente;
	}

	public User getUser() { 
		return user; }

	public void setUser(User user) { 
		this.user = user; }

	public String getNome() {
		return nome; }

	public void setNome(String nome) { 
		this.nome = nome; }

	public LocalDate getDataNascimento() {
		return dataNascimento;	}

	public void  setDataNascimento(LocalDate dataNascimento) {
		this.dataNascimento = dataNascimento;
	}

	public String getHistoricoFamiliar() {
		return historicoFamiliar;
	}

	public void setHistoricoFamiliar(String historicoFamiliar) {
		this.historicoFamiliar = historicoFamiliar;
	}

	public String getObservacoes() {
		return observacoes;
	}

	public void setObservacoes(String observacoes) {
		this.observacoes = observacoes;
	}

	public char getGenero() {
		return genero;
	}

	public void setGenero(char genero) {
		this.genero = genero;
	}


}
