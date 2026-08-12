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

}
