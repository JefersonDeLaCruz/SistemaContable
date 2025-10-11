package com.ues.sic.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.ues.sic.usuarios.UsuariosModel;
import com.ues.sic.usuarios.UsuariosRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Configuration
public class DataSeeder {

<<<<<<< HEAD
    // Usuarios por defecto para cada rol
    private static final String DEFAULT_ADMIN_USERNAME = "admin";
    private static final String DEFAULT_ADMIN_PASSWORD = "admin123";
    private static final String DEFAULT_ADMIN_EMAIL = "admin@sic.com";
    
    private static final String DEFAULT_CONTADOR_USERNAME = "contador";
    private static final String DEFAULT_CONTADOR_PASSWORD = "contador123";
    private static final String DEFAULT_CONTADOR_EMAIL = "contador@sic.com";
    
    private static final String DEFAULT_AUDITOR_USERNAME = "auditor";
    private static final String DEFAULT_AUDITOR_PASSWORD = "auditor123";
    private static final String DEFAULT_AUDITOR_EMAIL = "auditor@sic.com";
=======
    private static final String DEFAULT_ADMIN_USERNAME = "adminoa";
    private static final String DEFAULT_ADMIN_EMAIL = "adminoa@sistema.local";
    private static final String DEFAULT_ADMIN_ROLE = "ADMIN";
>>>>>>> Walt-discriminador
    
    @Bean
    CommandLineRunner seedDefaultUser(UsuariosRepository repo, PasswordEncoder encoder, Environment env) {
        return args -> {
            System.out.println("\n========================================");
            System.out.println("  INICIANDO SEED DE USUARIOS DEFAULT");
            System.out.println("========================================\n");
            
            createDefaultUsers(repo, encoder);
            
            System.out.println("\n========================================");
            System.out.println("  SEED DE USUARIOS COMPLETADO");
            System.out.println("========================================\n");
        };
    }
    
    /**
     * Crea usuarios por defecto para cada rol si no existen
     */
    private void createDefaultUsers(UsuariosRepository repo, PasswordEncoder encoder) {
        // Crear usuario ADMIN
        createUserIfNotExists(
            repo, encoder, 
            DEFAULT_ADMIN_USERNAME, 
            DEFAULT_ADMIN_EMAIL, 
            "ADMIN", 
            DEFAULT_ADMIN_PASSWORD
        );
        
        // Crear usuario CONTADOR
        createUserIfNotExists(
            repo, encoder, 
            DEFAULT_CONTADOR_USERNAME, 
            DEFAULT_CONTADOR_EMAIL, 
            "CONTADOR", 
            DEFAULT_CONTADOR_PASSWORD
        );
        
        // Crear usuario AUDITOR
        createUserIfNotExists(
            repo, encoder, 
            DEFAULT_AUDITOR_USERNAME, 
            DEFAULT_AUDITOR_EMAIL, 
            "AUDITOR", 
            DEFAULT_AUDITOR_PASSWORD
        );
    }
    
    /**
     * Crea un usuario si no existe
     */
    private void createUserIfNotExists(UsuariosRepository repo, PasswordEncoder encoder, 
                                      String username, String email, String role, String password) {
        
        // Verificar si el usuario ya existe
        boolean userExists = repo.existsByUsernameIgnoreCase(username) 
                          || repo.existsByEmailIgnoreCase(email);
        
        if (userExists) {
            System.out.println(" Usuario '" + username + "' (" + role + ") ya existe - omitiendo creación");
            return;
        }
        
        // Crear nuevo usuario
        UsuariosModel user = new UsuariosModel();
        user.setUsername(username);
        user.setEmail(email);
        user.setRole(role);
        user.setActive(true);
        user.setPassword(encoder.encode(password));
        
        // Establecer timestamps
        String currentTimestamp = LocalDateTime.now()
            .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        user.setCreatedAt(currentTimestamp);
        user.setUpdatedAt(currentTimestamp);
        
        try {
            repo.save(user);
            System.out.println("Usuario creado exitosamente:");
            System.out.println("   ├─ Usuario: " + username);
            System.out.println("   ├─ Email: " + email);
            System.out.println("   ├─ Rol: " + role);
            System.out.println("   └─ Contraseña: " + password);
            System.out.println();
        } catch (Exception e) {
            System.err.println("ERROR al crear usuario '" + username + "': " + e.getMessage());
            System.err.println();
        }
    }
}
