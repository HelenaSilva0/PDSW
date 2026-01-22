package vivamama.dto;

import vivamama.model.UserType;

public class RegisterResponse {

    public Integer userId;
    public UserType role;

    public RegisterResponse( Integer userId, UserType role) {
        this.userId = userId;
        this.role = role;
    }
}