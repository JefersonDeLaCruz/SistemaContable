package com.ues.sic.usuarios;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "usuarios")
public class UsuariosModel {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO) //equivale a autoincremental pk
    private Long id;
    
    @NotNull
    @NotEmpty
    @NotBlank
    @Column(name = "username", unique = true)
    private String username;

    @NotNull
    @NotEmpty
    @NotBlank
    @Column(name = "password")
    private String password;

    @NotNull
    @NotEmpty
    @NotBlank
    @Column(name = "email", unique = true)
    private String email;

    @NotNull
    @NotEmpty
    @NotBlank
    @Column(name = "role")
    private String role;

    @NotNull
    //@NotEmpty
    //@NotBlank para un boolean no tiene sentido, basta con el @NotNull
    @Column(name = "active")
    private Boolean active;    

    
    @NotNull
    @NotEmpty
    @NotBlank
    @Column(name = "created_at")
    private String createdAt;

    @NotNull
    @NotEmpty
    @NotBlank
    @Column(name = "updated_at")
    private String updatedAt;

    //getter y setter de todas las variables
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return this.username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getPassword() {
        return this.password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public String getEmail() {
        return this.email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getRole() {
        return this.role;
    }
    public void setRole(String role) {
        this.role = role;
    }
    public Boolean getActive() {
        return this.active;
    }
    public void setActive(Boolean active) {
        this.active = active;
    }
    public String getCreatedAt() {
        return this.createdAt;
    }
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
    public String getUpdatedAt() {
        return this.updatedAt;
    }
    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

}
