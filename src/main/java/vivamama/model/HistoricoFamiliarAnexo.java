package vivamama.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class HistoricoFamiliarAnexo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "historico_id")
    @JsonIgnore
    private HistoricoFamiliar historico;

    @Column(nullable = false, length = 255)
    private String nomeOriginal;

    @Column(nullable = false, length = 255)
    private String nomeArmazenado;

    @Column(length = 120)
    private String contentType;

    private Long tamanho;

    @Column(nullable = false)
    private LocalDateTime enviadoEm;

    // quem fez upload (User.id)
    private Integer enviadoPorUserId;

    @PrePersist
    void prePersist() {
        if (enviadoEm == null) enviadoEm = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public HistoricoFamiliar getHistorico() { return historico; }
    public void setHistorico(HistoricoFamiliar historico) { this.historico = historico; }

    public String getNomeOriginal() { return nomeOriginal; }
    public void setNomeOriginal(String nomeOriginal) { this.nomeOriginal = nomeOriginal; }

    public String getNomeArmazenado() { return nomeArmazenado; }
    public void setNomeArmazenado(String nomeArmazenado) { this.nomeArmazenado = nomeArmazenado; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public Long getTamanho() { return tamanho; }
    public void setTamanho(Long tamanho) { this.tamanho = tamanho; }

    public LocalDateTime getEnviadoEm() { return enviadoEm; }
    public void setEnviadoEm(LocalDateTime enviadoEm) { this.enviadoEm = enviadoEm; }

    public Integer getEnviadoPorUserId() { return enviadoPorUserId; }
    public void setEnviadoPorUserId(Integer enviadoPorUserId) { this.enviadoPorUserId = enviadoPorUserId; }
}
