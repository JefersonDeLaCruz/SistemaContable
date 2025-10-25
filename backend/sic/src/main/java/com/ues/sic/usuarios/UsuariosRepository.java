package com.ues.sic.usuarios;

import org.springframework.data.jpa.repository.JpaRepository;
/* import org.springframework.data.repository.PagingAndSortingRepository; */

public interface UsuariosRepository extends JpaRepository<UsuariosModel, Long>{
    
    UsuariosModel findByUsername(String username);
    
    // Métodos para verificar existencia de manera segura
    boolean existsByUsernameIgnoreCase(String username);
    boolean existsByEmailIgnoreCase(String email);
    long countByActive(Boolean active);
}
