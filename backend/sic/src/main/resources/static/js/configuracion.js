// Configuración de cuenta
document.addEventListener('DOMContentLoaded', function() {
    
    // Obtener elementos del DOM
    const btnCambiarPassword = document.getElementById('btnCambiarPassword');
    const modalCambiarPassword = document.getElementById('modalCambiarPassword');
    const formCambiarPassword = document.getElementById('formCambiarPassword');
    const passwordError = document.getElementById('passwordError');
    const newPasswordInput = document.getElementById('newPassword');
    const confirmPasswordInput = document.getElementById('confirmPassword');
    const usernameInput = document.getElementById('usernameInput');
    const usernameWarning = document.getElementById('usernameWarning');

    // Auto-cerrar alertas después de 5 segundos
    const alerts = document.querySelectorAll('.alert-success, .alert-error');
    alerts.forEach(alert => {
        setTimeout(() => {
            alert.style.transition = 'opacity 0.5s';
            alert.style.opacity = '0';
            setTimeout(() => alert.remove(), 500);
        }, 5000);
    });

    // Mostrar advertencia cuando se cambia el username
    if (usernameInput && usernameWarning) {
        const originalUsername = usernameInput.dataset.original;
        
        usernameInput.addEventListener('input', function() {
            if (this.value !== originalUsername && this.value.length > 0) {
                usernameWarning.classList.remove('hidden');
            } else {
                usernameWarning.classList.add('hidden');
            }
        });
    }

    // Abrir modal al hacer clic en el botón
    if (btnCambiarPassword) {
        btnCambiarPassword.addEventListener('click', function(e) {
            e.preventDefault();
            modalCambiarPassword.showModal();
        });
    }

    // Validar que las contraseñas coincidan al enviar el formulario
    if (formCambiarPassword) {
        formCambiarPassword.addEventListener('submit', function(e) {
            const newPassword = newPasswordInput.value;
            const confirmPassword = confirmPasswordInput.value;

            // Ocultar error previo
            passwordError.classList.add('hidden');

            // Validar que las contraseñas coincidan
            if (newPassword !== confirmPassword) {
                e.preventDefault();
                passwordError.classList.remove('hidden');
                document.getElementById('passwordErrorMessage').textContent = 'Las contraseñas no coinciden';
                confirmPasswordInput.focus();
                return false;
            }

            // Validar longitud mínima
            if (newPassword.length < 6) {
                e.preventDefault();
                passwordError.classList.remove('hidden');
                document.getElementById('passwordErrorMessage').textContent = 'La contraseña debe tener al menos 6 caracteres';
                newPasswordInput.focus();
                return false;
            }

            // Si todo está bien, el formulario se enviará
            return true;
        });
    }

    // Limpiar el formulario cuando se cierra el modal
    if (modalCambiarPassword) {
        modalCambiarPassword.addEventListener('close', function() {
            formCambiarPassword.reset();
            passwordError.classList.add('hidden');
        });
    }

    // Validación en tiempo real mientras el usuario escribe
    if (confirmPasswordInput) {
        confirmPasswordInput.addEventListener('input', function() {
            const newPassword = newPasswordInput.value;
            const confirmPassword = confirmPasswordInput.value;

            if (confirmPassword.length > 0) {
                if (newPassword !== confirmPassword) {
                    confirmPasswordInput.setCustomValidity('Las contraseñas no coinciden');
                    confirmPasswordInput.classList.add('input-error');
                } else {
                    confirmPasswordInput.setCustomValidity('');
                    confirmPasswordInput.classList.remove('input-error');
                }
            }
        });
    }

    // Limpiar validación personalizada al cambiar la nueva contraseña
    if (newPasswordInput) {
        newPasswordInput.addEventListener('input', function() {
            if (confirmPasswordInput.value.length > 0) {
                confirmPasswordInput.dispatchEvent(new Event('input'));
            }
        });
    }

});
