package com.kanhaiya.monkeyquest.domain.dto.request;

import com.kanhaiya.monkeyquest.domain.enums.UserRole;
import lombok.Data;

@Data
public class SignUpDto {
    private String userName;
    private String password;
    private UserRole role = UserRole.USER;
}
