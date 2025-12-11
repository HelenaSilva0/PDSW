package vivamama.model;

import jakarta.persistence.*;

@Entity
public class HistoricoFamiliar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // depois dá pra ligar com Paciente, por enquanto deixo solto
    // @ManyToOne private Paciente paciente;

    @Column(length = 4000, nullable = false)
    private String textoHistorico;

    // caminhos dos arquivos enviados (separados por ;)
    @Column(length = 2000)
    private String caminhosExames;

    // getters e setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTextoHistorico() { return textoHistorico; }
    public void setTextoHistorico(String textoHistorico) { this.textoHistorico = textoHistorico; }

    public String getCaminhosExames() { return caminhosExames; }
    public void setCaminhosExames(String caminhosExames) { this.caminhosExames = caminhosExames; }
}
