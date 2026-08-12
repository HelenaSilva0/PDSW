package vivamama.dto;

import vivamama.model.UserType;
import vivamama.model.UserStatus;

public class RegisterResponse {

    public Integer userId;
    public UserType role;
    public UserStatus status;

    public RegisterResponse(Integer userId, UserType role, UserStatus status) {
        this.userId = userId;
        this.role = role;
        this.status = status;
    }
}
