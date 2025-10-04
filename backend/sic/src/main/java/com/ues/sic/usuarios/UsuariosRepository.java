package com.ues.sic.usuarios;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuariosRepository extends JpaRepository<UsuariosModel, Long> {
    


    UsuariosModel findByUsername(String username);
}
