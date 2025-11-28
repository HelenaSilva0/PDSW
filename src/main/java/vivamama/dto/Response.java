package vivamama.dto;

import vivamama.model.UserType;

public class Response {
    public Integer userId;
    public UserType role;
    public Response(Integer userId, UserType role){
        this.userId = userId;
        this.role = role;
    }

}
