package com.ues.sic.usuarios;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Servicio para gestión de usuarios.
 * Encapsula la lógica de negocio relacionada con usuarios.
 */
@Service
public class UsuariosService {

    @Autowired
    private UsuariosRepository usuariosRepository;

    /**
     * Busca un usuario por su username.
     * @param username El nombre de usuario
     * @return El modelo de usuario o null si no existe
     */
    public UsuariosModel findByUsername(String username) {
        return usuariosRepository.findByUsername(username);
    }

    /**
     * Busca un usuario por su email.
     * @param email El correo electrónico
     * @return El modelo de usuario o null si no existe
     */
    public UsuariosModel findByEmail(String email) {
        return usuariosRepository.findByEmail(email);
    }

    /**
     * Busca un usuario por ID.
     * @param id El ID del usuario
     * @return Optional con el usuario si existe
     */
    public Optional<UsuariosModel> findById(Long id) {
        return usuariosRepository.findById(id);
    }

    /**
     * Obtiene todos los usuarios.
     * @return Lista de todos los usuarios
     */
    public List<UsuariosModel> findAll() {
        return usuariosRepository.findAll();
    }

    /**
     * Guarda o actualiza un usuario.
     * @param usuario El usuario a guardar
     * @return El usuario guardado
     */
    public UsuariosModel save(UsuariosModel usuario) {
        return usuariosRepository.save(usuario);
    }

    /**
     * Elimina un usuario por ID.
     * @param id El ID del usuario a eliminar
     */
    public void deleteById(Long id) {
        usuariosRepository.deleteById(id);
    }

    /**
     * Verifica si existe un usuario con el username dado (ignora mayúsculas).
     * @param username El nombre de usuario
     * @return true si existe, false en caso contrario
     */
    public boolean existsByUsername(String username) {
        return usuariosRepository.existsByUsernameIgnoreCase(username);
    }

    /**
     * Verifica si existe un usuario con el email dado (ignora mayúsculas).
     * @param email El correo electrónico
     * @return true si existe, false en caso contrario
     */
    public boolean existsByEmail(String email) {
        return usuariosRepository.existsByEmailIgnoreCase(email);
    }

    /**
     * Cuenta usuarios activos o inactivos.
     * @param active Estado de activación
     * @return Número de usuarios con ese estado
     */
    public long countByActive(Boolean active) {
        return usuariosRepository.countByActive(active);
    }
}
