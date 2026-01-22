package vivamama.model;


	import jakarta.persistence.*;

	@Entity
	public class Medico {

	    @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private int idMedico;
	    
	    @OneToOne
	    @JoinColumn(name = "user_id", nullable = false, unique = true)
	    private User user;

	    @Column(length = 100, nullable = false)
	    private String nome;

	    @Column(length = 20, nullable = false, unique = true)
	    private String crm;

	    @Column(length = 100, nullable = false)
	    private String especialidade;

	    public int getIdMedico() { return idMedico; }
	    public void setIdMedico(int idMedico) { this.idMedico = idMedico; }

	    public String getNome() { return nome; }
	    public void setNome(String nome) { this.nome = nome; }
	    
	    public User getUser() { 
			return user; }

		public void setUser(User user) { 
			this.user = user; }

	    public String getCrm() { return crm; }
	    public void setCrm(String crm) { this.crm = crm; }

	    public String getEspecialidade() { return especialidade; }
	    public void setEspecialidade(String especialidade) { this.especialidade = especialidade; }


}
