package vivamama.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = "email"),
        @UniqueConstraint(columnNames = "telefone")
})

public class User {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 120)
    private String email;

    @JsonIgnore
    @Column(nullable = false, length = 255)
    private String senha;

    @Column(nullable = false, length = 20)
    private String telefone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserType role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status;

    private Integer statusAlteradoPorUserId;
    private Instant statusAlteradoEm;

    @Column(name = "termos_versao", length = 20)
    private String termosVersao;

    @Column(name = "termos_aceitos_em")
    private Instant termosAceitosEm;

    @Column(name = "privacidade_versao", length = 20)
    private String privacidadeVersao;

    @Column(name = "privacidade_ciente_em")
    private Instant privacidadeCienteEm;

    @Column(name = "dados_saude_consentimento_em")
    private Instant dadosSaudeConsentimentoEm;

    @Column(name = "confidencialidade_aceita_em")
    private Instant confidencialidadeAceitaEm;

    @Column(name = "consentimento_ip", length = 45)
    private String consentimentoIp;

    @Column(name = "consentimento_user_agent", length = 500)
    private String consentimentoUserAgent;

    @PrePersist
    void prePersist() {
        if (status == null) status = role == UserType.MEDICO ? UserStatus.PENDENTE : UserStatus.ATIVO;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }
    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
    public UserType getRole() { return role; }
    public void setRole(UserType role) { this.role = role; }
    public UserStatus getStatus() { return status; }
    public void setStatus(UserStatus status) { this.status = status; }
    public Integer getStatusAlteradoPorUserId() { return statusAlteradoPorUserId; }
    public void setStatusAlteradoPorUserId(Integer value) { this.statusAlteradoPorUserId = value; }
    public Instant getStatusAlteradoEm() { return statusAlteradoEm; }
    public void setStatusAlteradoEm(Instant value) { this.statusAlteradoEm = value; }
    public String getTermosVersao() { return termosVersao; }
    public void setTermosVersao(String value) { this.termosVersao = value; }
    public Instant getTermosAceitosEm() { return termosAceitosEm; }
    public void setTermosAceitosEm(Instant value) { this.termosAceitosEm = value; }
    public String getPrivacidadeVersao() { return privacidadeVersao; }
    public void setPrivacidadeVersao(String value) { this.privacidadeVersao = value; }
    public Instant getPrivacidadeCienteEm() { return privacidadeCienteEm; }
    public void setPrivacidadeCienteEm(Instant value) { this.privacidadeCienteEm = value; }
    public Instant getDadosSaudeConsentimentoEm() { return dadosSaudeConsentimentoEm; }
    public void setDadosSaudeConsentimentoEm(Instant value) { this.dadosSaudeConsentimentoEm = value; }
    public Instant getConfidencialidadeAceitaEm() { return confidencialidadeAceitaEm; }
    public void setConfidencialidadeAceitaEm(Instant value) { this.confidencialidadeAceitaEm = value; }
    public String getConsentimentoIp() { return consentimentoIp; }
    public void setConsentimentoIp(String value) { this.consentimentoIp = value; }
    public String getConsentimentoUserAgent() { return consentimentoUserAgent; }
    public void setConsentimentoUserAgent(String value) { this.consentimentoUserAgent = value; }

}
