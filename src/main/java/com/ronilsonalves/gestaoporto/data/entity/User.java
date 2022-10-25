package com.ronilsonalves.gestaoporto.data.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ronilsonalves.gestaoporto.data.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.annotation.Nonnull;
import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Set;

@Entity
@Table(name = "app_user")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class User extends GenericEntity {

    @NotNull
    private String username;

    @Nonnull
    private String name;

    @JsonIgnore
    private String hashedPassword;

    @Enumerated(EnumType.STRING)
    @ElementCollection(fetch = FetchType.EAGER)
    @NotNull
    private Set<Role> roles;

    @NotBlank
    private String profilePicture;
}
