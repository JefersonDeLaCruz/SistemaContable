/**
 * Sistema de Notificaciones Toast
 * Función global para mostrar notificaciones flotantes en la aplicación
 * 
 * @param {string} mensaje - El texto a mostrar en la notificación
 * @param {string} tipo - Tipo de notificación: 'info', 'success', 'warning', 'error'
 * @param {number} duracion - Duración en milisegundos antes de que desaparezca (default: 5000)
 */
function mostrarToast(mensaje, tipo = 'info', duracion = 5000) {
    // Crear contenedor de notificaciones si no existe
    let contenedorToast = document.getElementById('toast-container');
    if (!contenedorToast) {
        contenedorToast = document.createElement('div');
        contenedorToast.id = 'toast-container';
        contenedorToast.className = 'fixed top-4 right-4 z-50 flex flex-col gap-2';
        contenedorToast.style.maxWidth = '400px';
        document.body.appendChild(contenedorToast);
    }

    // Determinar el tipo de alerta y su ícono
    const tiposAlerta = {
        info: {
            clase: 'alert-info',
            icono: `
                <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" class="stroke-current shrink-0 w-6 h-6">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path>
                </svg>
            `
        },
        success: {
            clase: 'alert-success',
            icono: `
                <svg xmlns="http://www.w3.org/2000/svg" class="stroke-current shrink-0 h-6 w-6" fill="none" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
            `
        },
        warning: {
            clase: 'alert-warning',
            icono: `
                <svg xmlns="http://www.w3.org/2000/svg" class="stroke-current shrink-0 h-6 w-6" fill="none" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
                </svg>
            `
        },
        error: {
            clase: 'alert-error',
            icono: `
                <svg xmlns="http://www.w3.org/2000/svg" class="stroke-current shrink-0 h-6 w-6" fill="none" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 14l2-2m0 0l2-2m-2 2l-2-2m2 2l2 2m7-2a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
            `
        }
    };

    // Obtener configuración del tipo de alerta
    const config = tiposAlerta[tipo] || tiposAlerta.info;

    // Crear el elemento de notificación
    const toast = document.createElement('div');
    toast.className = `alert ${config.clase} shadow-lg transition-all duration-300 transform translate-x-full opacity-0`;
    toast.innerHTML = `
        <div class="flex items-center gap-2">
            ${config.icono}
            <span>${mensaje}</span>
        </div>
    `;

    // Agregar al contenedor
    contenedorToast.appendChild(toast);

    // Animar entrada
    setTimeout(() => {
        toast.classList.remove('translate-x-full', 'opacity-0');
    }, 10);

    // Animar salida y eliminar
    setTimeout(() => {
        toast.classList.add('translate-x-full', 'opacity-0');
        setTimeout(() => {
            toast.remove();
            // Limpiar contenedor si está vacío
            if (contenedorToast.children.length === 0) {
                contenedorToast.remove();
            }
        }, 300);
    }, duracion);
}

/**
 * Función auxiliar para formatear moneda
 * @param {number} valor - El valor numérico a formatear
 * @returns {string} - Valor formateado como moneda
 */
function formatearMoneda(valor) {
    return new Intl.NumberFormat('es-SV', {
        style: 'currency',
        currency: 'USD',
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
    }).format(valor);
}

/**
 * Función auxiliar para formatear fecha
 * @param {string} fecha - Fecha en formato YYYY-MM-DD
 * @returns {string} - Fecha formateada como "DD Mes YYYY"
 */
function formatearFecha(fecha) {
    const [year, month, day] = fecha.split('-');
    const meses = ['Ene', 'Feb', 'Mar', 'Abr', 'May', 'Jun', 'Jul', 'Ago', 'Sep', 'Oct', 'Nov', 'Dic'];
    return `${day} ${meses[parseInt(month) - 1]} ${year}`;
}
