package com.uskov.pet.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.uskov.pet.webClient.model.NewUserRequest;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "app_users")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User extends AbstractEntity {

    private String username;
    private String name;
    @JsonIgnore
    private String hashedPassword;
    @ElementCollection(targetClass = Role.class, fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "roles", nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Set<Role> roles = new HashSet<>();
    @Lob
    @Column(length = 1000000)
    private byte[] profilePicture;

    public static User userFromRequest(NewUserRequest request) {
        User newUser = new User();
        newUser.setUsername(request.getUsername());
        newUser.setHashedPassword(request.getPassword());
        newUser.setRoles(request.getRoles());

        return newUser;
    }
}
