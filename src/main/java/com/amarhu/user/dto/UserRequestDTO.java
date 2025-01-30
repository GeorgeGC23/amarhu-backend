package com.amarhu.user.dto;

import com.amarhu.user.entity.Role;
import lombok.Data;

@Data
public class UserRequestDTO {
    private String name;
    private String email;
    private Role requestedRole;
}
