package vivamama.dto;

import java.time.LocalDate;

import vivamama.model.UserType;

public class RegisterRequest {

    public String email;
    public String senha;
    public String telefone;
    public UserType role;

  
    public String nome;
    public LocalDate dataNascimento; 
    public String observacoes;       
    public Character genero;         

    public String crm;              
    public String especialidade;    
}
