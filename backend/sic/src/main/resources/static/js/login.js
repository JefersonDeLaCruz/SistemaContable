

// const loginForm = document.getElementById('loginForm');

// loginForm.addEventListener('submit', function(event) {
//     event.preventDefault(); // Evita el envío del formulario por defecto

//     const formData = new FormData(loginForm);
//     const data = Object.fromEntries(formData);

//     console.log(data); // Para verificar los datos del formulario

//     fetch('/api/usuarios/login', {
//         method: 'POST',
//         headers: {
//             'Content-Type': 'application/json'
//         },
//         body: JSON.stringify(data)
//     })
//     .then(response => {
//         if (response.ok) {
//             // Redirigir o mostrar un mensaje de éxito
//             console.log('Login exitoso');
//             window.location.href = '/dashboard';
//         } else {
//             // Manejar errores
//             console.error('Error en el login');
//             window.location.href = '/login';
//         }
//     });
// });
