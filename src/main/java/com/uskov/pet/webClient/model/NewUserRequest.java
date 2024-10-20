package com.uskov.pet.webClient.model;

import com.uskov.pet.data.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NewUserRequest {

    private String username;
    private String email;
    private Set<Role> roles;
    private String password;
}
