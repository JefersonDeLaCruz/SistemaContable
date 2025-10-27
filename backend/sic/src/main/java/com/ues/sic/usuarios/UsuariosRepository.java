package com.ues.sic.usuarios;

import org.springframework.data.jpa.repository.JpaRepository;
/* import org.springframework.data.repository.PagingAndSortingRepository; */

public interface UsuariosRepository extends JpaRepository<UsuariosModel, Long>{
    
    UsuariosModel findByUsername(String username);
    UsuariosModel findByEmail(String email);
    
    // Métodos para verificar existencia de manera segura
    boolean existsByUsernameIgnoreCase(String username);
    boolean existsByEmailIgnoreCase(String email);
    
    // Métodos para verificar existencia excluyendo un ID específico
    boolean existsByUsernameIgnoreCaseAndIdNot(String username, Long id);
    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
    
    long countByActive(Boolean active);
}
