package vivamama.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
public class HistoricoFamiliar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "paciente_id")
    private Paciente paciente;

    @Column(length = 4000, nullable = false)
    private String textoHistorico;

    // versionamento por snapshot (append-only)
    @Column(nullable = false)
    private Integer versao;

    @Column(nullable = false)
    private LocalDateTime criadoEm;

    // quem criou o snapshot (User.id)
    private Integer criadoPorUserId;

    // legado: caminhos dos arquivos enviados (separados por ;)
    @Column(length = 2000)
    private String caminhosExames;

    @PrePersist
    void prePersist() {
        if (criadoEm == null) criadoEm = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Paciente getPaciente() { return paciente; }
    public void setPaciente(Paciente paciente) { this.paciente = paciente; }

    public String getTextoHistorico() { return textoHistorico; }
    public void setTextoHistorico(String textoHistorico) { this.textoHistorico = textoHistorico; }

    public Integer getVersao() { return versao; }
    public void setVersao(Integer versao) { this.versao = versao; }

    public LocalDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }

    public Integer getCriadoPorUserId() { return criadoPorUserId; }
    public void setCriadoPorUserId(Integer criadoPorUserId) { this.criadoPorUserId = criadoPorUserId; }

    public String getCaminhosExames() { return caminhosExames; }
    public void setCaminhosExames(String caminhosExames) { this.caminhosExames = caminhosExames; }
}