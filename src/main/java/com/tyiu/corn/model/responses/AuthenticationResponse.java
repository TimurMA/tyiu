package com.tyiu.corn.model.responses;

import com.tyiu.corn.model.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AuthenticationResponse {
    private String token;
    private String email;
    private String lastName;
    private String firstName;
    private List<Role> roles;
}