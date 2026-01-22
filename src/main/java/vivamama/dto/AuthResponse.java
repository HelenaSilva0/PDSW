package vivamama.dto;

import vivamama.model.UserType;

public class AuthResponse {
    public String token;
    public Integer userId;
    public UserType role;

    public AuthResponse(String token, Integer userId, UserType role) {
        this.token = token;
        this.userId = userId;
        this.role = role;
    }
}

