

// Función para mostrar notificaciones toast
function mostrarNotificacion(mensaje, tipo = 'info', duracion = 5000) {
    // Crear contenedor de notificaciones si no existe
    let contenedorToast = document.getElementById('toast-container');
    if (!contenedorToast) {
        contenedorToast = document.createElement('div');
        contenedorToast.id = 'toast-container';
        contenedorToast.className = 'fixed top-4 right-4 z-50 flex flex-col gap-2';
        contenedorToast.style.maxWidth = '400px';
        document.body.appendChild(contenedorToast);
    }

    // Determinar el tipo de alerta
    let alertClass = 'alert-info';
    let iconSVG = `
        <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" class="stroke-current shrink-0 w-6 h-6">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path>
        </svg>
    `;

    if (tipo === 'success') {
        alertClass = 'alert-success';
        iconSVG = `
            <svg xmlns="http://www.w3.org/2000/svg" class="stroke-current shrink-0 h-6 w-6" fill="none" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
        `;
    } else if (tipo === 'warning') {
        alertClass = 'alert-warning';
        iconSVG = `
            <svg xmlns="http://www.w3.org/2000/svg" class="stroke-current shrink-0 h-6 w-6" fill="none" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
            </svg>
        `;
    } else if (tipo === 'error') {
        alertClass = 'alert-error';
        iconSVG = `
            <svg xmlns="http://www.w3.org/2000/svg" class="stroke-current shrink-0 h-6 w-6" fill="none" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 14l2-2m0 0l2-2m-2 2l-2-2m2 2l2 2m7-2a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
        `;
    }

    // Crear el elemento de notificación
    const toast = document.createElement('div');
    toast.className = `alert ${alertClass} shadow-lg transition-all duration-300 transform translate-x-full opacity-0`;
    toast.innerHTML = `
        <div>
            ${iconSVG}
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

const btnFiltrar = document.getElementById('btnFiltrar');

async function cargarPeriodos() {
    try {
        const response = await fetch('/api/periodos'); // Endpoint del backend
        const periodos = await response.json();

        // Selecciona correctamente el elemento <select> por su atributo name
        const select = document.querySelector('select[name="idPeriodo"]');

        // Limpia opciones previas (opcional)
        select.innerHTML = '<option value="">Seleccione un periodo</option>';

        // Crea las opciones dinámicamente
        periodos.forEach(periodo => {
            const option = document.createElement('option');
            option.value = periodo.idPeriodo; // campo correcto según tu modelo
            option.textContent = periodo.nombre;
            select.appendChild(option);
        });

    } catch (error) {
        console.error('Error cargando los periodos:', error);
    }
}

// Función para obtener todas las cuentas y crear un mapa
async function cargarMapaCuentas() {
    try {
        const response = await fetch('/api/cuentas');
        const cuentas = await response.json();
        
        // Crear un mapa de ID -> Cuenta para fácil acceso
        const mapaCuentas = {};
        cuentas.forEach(cuenta => {
            mapaCuentas[cuenta.idCuenta.toString()] = cuenta;
        });
        
        return mapaCuentas;
    } catch (error) {
        console.error('Error cargando las cuentas:', error);
        return {};
    }
}

const select = document.querySelector('select[name="idPeriodo"]');
let periodoSeleccionado = null;
let nombrePeriodoSeleccionado = '';

select.addEventListener('change', (event) => {
    periodoSeleccionado = event.target.value;
    nombrePeriodoSeleccionado = event.target.options[event.target.selectedIndex].text;
    console.log('Periodo seleccionado:', periodoSeleccionado, nombrePeriodoSeleccionado);
    
    // Actualizar el título con el período seleccionado
    const tituloMes = document.querySelector('.mes-actual');
    if (periodoSeleccionado) {
        tituloMes.textContent = `Libro Diario - ${nombrePeriodoSeleccionado}`;
    } else {
        tituloMes.textContent = 'Libro Diario';
    }
});

// Función para obtener partidas por período
async function obtenerPartidasPorPeriodo(idPeriodo) {
    try {
        const response = await fetch(`/api/partidas/periodo?idPeriodo=${idPeriodo}`);
        if (!response.ok) {
            throw new Error('Error al obtener partidas');
        }
        return await response.json();
    } catch (error) {
        console.error('Error obteniendo partidas:', error);
        return [];
    }
}

// Función para obtener detalles de una partida
async function obtenerDetallesPartida(partidaId) {
    try {
        const response = await fetch(`/api/detalle-partida/partida?partidaId=${partidaId}`);
        if (!response.ok) {
            throw new Error('Error al obtener detalles');
        }
        return await response.json();
    } catch (error) {
        console.error('Error obteniendo detalles:', error);
        return [];
    }
}

// Función para renderizar las partidas en el libro diario
async function renderizarLibroDiario(partidas, mapaCuentas) {
    const diarioContainer = document.querySelector('.diario-container');
    
    // Limpiar contenido existente
    const tablasExistentes = diarioContainer.querySelectorAll('table');
    tablasExistentes.forEach((tabla) => {
        tabla.remove();
    });
    
    // Limpiar divisores
    const divisores = diarioContainer.querySelectorAll('.divider, .partida-separator');
    divisores.forEach(div => div.remove());
    
    if (partidas.length === 0) {
        // Mostrar mensaje en el contenedor y notificación
        diarioContainer.innerHTML = `
            <div class="flex flex-col items-center justify-center py-12 text-base-content/60">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-24 w-24 mb-4 opacity-50" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                </svg>
                <p class="text-xl font-semibold">No hay partidas registradas</p>
                <p class="text-sm">para el período seleccionado</p>
            </div>
        `;
        mostrarNotificacion('No se encontraron partidas para el período seleccionado', 'info', 4000);
        return;
    }
    
    // Notificación de éxito
    mostrarNotificacion(`Se cargaron ${partidas.length} partida(s) correctamente`, 'success', 3000);
    
    // Para cada partida, crear una tabla con diseño consistente
    for (let i = 0; i < partidas.length; i++) {
        const partida = partidas[i];
        const detalles = await obtenerDetallesPartida(partida.id);
        
        // Crear nueva tabla para cada partida
        const tabla = crearNuevaTabla();
        const tbody = tabla.querySelector('tbody');
        
        // Calcular totales
        let totalDebito = 0;
        let totalCredito = 0;
        
        // Renderizar los detalles de la partida
        detalles.forEach((detalle, index) => {
            totalDebito += detalle.debito || 0;
            totalCredito += detalle.credito || 0;
            
            const fila = document.createElement('tr');
            fila.className = 'hover';
            
            // Solo mostrar fecha, ID y descripción en la primera fila de cada partida
            if (index === 0) {
                fila.innerHTML = `
                    <td rowspan="${detalles.length}" class="align-top bg-base-200 font-semibold">${formatearFecha(partida.fecha)}</td>
                    <td rowspan="${detalles.length}" class="align-top bg-base-200 text-center font-semibold">#${partida.id}</td>
                    <td rowspan="${detalles.length}" class="align-top bg-base-200">${partida.descripcion}</td>
                    <td class="pl-4">${obtenerNombreCuenta(detalle.idCuenta, mapaCuentas)}</td>
                    <td class="text-right font-mono">${detalle.debito > 0 ? formatearMoneda(detalle.debito) : '-'}</td>
                    <td class="text-right font-mono">${detalle.credito > 0 ? formatearMoneda(detalle.credito) : '-'}</td>
                `;
            } else {
                fila.innerHTML = `
                    <td class="pl-4">${obtenerNombreCuenta(detalle.idCuenta, mapaCuentas)}</td>
                    <td class="text-right font-mono">${detalle.debito > 0 ? formatearMoneda(detalle.debito) : '-'}</td>
                    <td class="text-right font-mono">${detalle.credito > 0 ? formatearMoneda(detalle.credito) : '-'}</td>
                `;
            }
            
            tbody.appendChild(fila);
        });
        
        // Agregar fila de totales
        const filaTotales = document.createElement('tr');
        filaTotales.className = 'font-bold bg-base-300 border-t-2 border-primary';
        filaTotales.innerHTML = `
            <td colspan="4" class="text-right">TOTALES:</td>
            <td class="text-right font-mono text-primary">${formatearMoneda(totalDebito)}</td>
            <td class="text-right font-mono text-primary">${formatearMoneda(totalCredito)}</td>
        `;
        tbody.appendChild(filaTotales);
        
        // Agregar la tabla al contenedor
        diarioContainer.appendChild(tabla);
        
        // Agregar separador entre partidas (excepto después de la última)
        if (i < partidas.length - 1) {
            const separador = document.createElement('div');
            separador.className = 'partida-separator my-6 border-t-2 border-dashed border-base-300';
            diarioContainer.appendChild(separador);
        }
    }
}

// Función para crear una nueva tabla
function crearNuevaTabla() {
    const tabla = document.createElement('table');
    tabla.className = 'table w-full table-zebra mb-4';
    tabla.innerHTML = `
        <thead class="bg-primary text-primary-content sticky top-0">
            <tr>
                <th class="w-24">Fecha</th>
                <th class="w-16">ID</th>
                <th class="w-1/4">Descripción</th>
                <th class="w-1/3">Cuenta</th>
                <th class="w-32 text-right">Débito</th>
                <th class="w-32 text-right">Crédito</th>
            </tr>
        </thead>
        <tbody>
        </tbody>
    `;
    return tabla;
}

// Función auxiliar para obtener el nombre de la cuenta
function obtenerNombreCuenta(idCuenta, mapaCuentas) {
    const cuenta = mapaCuentas[idCuenta];
    if (cuenta) {
        return `<span class="font-semibold">${cuenta.codigo}</span> - ${cuenta.nombre}`;
    }
    return `<span class="text-error">Cuenta no encontrada (ID: ${idCuenta})</span>`;
}

// Función auxiliar para formatear moneda
function formatearMoneda(valor) {
    return new Intl.NumberFormat('es-SV', {
        style: 'currency',
        currency: 'USD',
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
    }).format(valor);
}

// Función auxiliar para formatear fecha
function formatearFecha(fecha) {
    const [year, month, day] = fecha.split('-');
    const meses = ['Ene', 'Feb', 'Mar', 'Abr', 'May', 'Jun', 'Jul', 'Ago', 'Sep', 'Oct', 'Nov', 'Dic'];
    return `${day} ${meses[parseInt(month) - 1]} ${year}`;
}

btnFiltrar.addEventListener('click', async (event) => {
    event.preventDefault();
    
    if (!periodoSeleccionado) {
        // Mostrar notificación de advertencia
        mostrarNotificacion('Por favor seleccione un período contable antes de filtrar', 'warning', 4000);
        return;
    }
    
    // Mostrar indicador de carga con spinner
    const textoOriginal = btnFiltrar.innerHTML;
    btnFiltrar.innerHTML = `
        <span class="loading loading-spinner"></span>
        Cargando...
    `;
    btnFiltrar.disabled = true;
    
    try {
        // Cargar mapa de cuentas
        const mapaCuentas = await cargarMapaCuentas();
        
        // Obtener partidas del período seleccionado
        const partidas = await obtenerPartidasPorPeriodo(periodoSeleccionado);
        
        // Renderizar el libro diario
        await renderizarLibroDiario(partidas, mapaCuentas);
        
        console.log('Libro diario actualizado para el período:', periodoSeleccionado);
        
    } catch (error) {
        console.error('Error al filtrar partidas:', error);
        
        // Mostrar notificación de error
        mostrarNotificacion('Error al cargar las partidas. Por favor intente nuevamente', 'error', 5000);
        
        // Limpiar el contenedor
        const diarioContainer = document.querySelector('.diario-container');
        diarioContainer.innerHTML = `
            <div class="flex flex-col items-center justify-center py-12 text-error">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-24 w-24 mb-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
                </svg>
                <p class="text-xl font-semibold">Error al cargar datos</p>
                <p class="text-sm opacity-70">Intente nuevamente más tarde</p>
            </div>
        `;
    } finally {
        // Restaurar botón
        btnFiltrar.innerHTML = textoOriginal;
        btnFiltrar.disabled = false;
    }
});

window.addEventListener('DOMContentLoaded', () => {
    cargarPeriodos();
});
