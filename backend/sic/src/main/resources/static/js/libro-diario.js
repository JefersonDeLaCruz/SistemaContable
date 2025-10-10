

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
    
    // Limpiar contenido existente excepto la primera tabla (que sirve como template)
    const tablas = diarioContainer.querySelectorAll('table');
    tablas.forEach((tabla, index) => {
        if (index > 0) {
            tabla.remove();
        }
    });
    
    // Limpiar el tbody de la primera tabla
    const primeraTabla = diarioContainer.querySelector('table tbody');
    primeraTabla.innerHTML = '';
    
    if (partidas.length === 0) {
        primeraTabla.innerHTML = '<tr><td colspan="6" class="text-center">No hay partidas para el período seleccionado</td></tr>';
        return;
    }
    
    // Para cada partida, crear una tabla separada
    for (let i = 0; i < partidas.length; i++) {
        const partida = partidas[i];
        const detalles = await obtenerDetallesPartida(partida.id);
        
        // Si es la primera partida, usar la tabla existente
        let tabla, tbody;
        if (i === 0) {
            tabla = diarioContainer.querySelector('table');
            tbody = tabla.querySelector('tbody');
        } else {
            // Crear nueva tabla para las siguientes partidas
            tabla = crearNuevaTabla();
            diarioContainer.appendChild(tabla);
            
            // Agregar divisor entre tablas
            const divider = document.createElement('div');
            divider.className = 'divider';
            diarioContainer.insertBefore(divider, tabla);
            
            tbody = tabla.querySelector('tbody');
        }
        
        // Renderizar los detalles de la partida
        detalles.forEach((detalle, index) => {
            const fila = document.createElement('tr');
            
            // Solo mostrar fecha, ID y descripción en la primera fila de cada partida
            if (index === 0) {
                fila.innerHTML = `
                    <td rowspan="${detalles.length}">${partida.fecha}</td>
                    <td rowspan="${detalles.length}">${partida.id}</td>
                    <td rowspan="${detalles.length}">${partida.descripcion}</td>
                    <td>${obtenerNombreCuenta(detalle.idCuenta, mapaCuentas)}</td>
                    <td>${detalle.debito > 0 ? '$' + detalle.debito.toFixed(2) : ''}</td>
                    <td>${detalle.credito > 0 ? '$' + detalle.credito.toFixed(2) : ''}</td>
                `;
            } else {
                fila.innerHTML = `
                    <td>${obtenerNombreCuenta(detalle.idCuenta, mapaCuentas)}</td>
                    <td>${detalle.debito > 0 ? '$' + detalle.debito.toFixed(2) : ''}</td>
                    <td>${detalle.credito > 0 ? '$' + detalle.credito.toFixed(2) : ''}</td>
                `;
            }
            
            tbody.appendChild(fila);
        });
    }
}

// Función para crear una nueva tabla
function crearNuevaTabla() {
    const tabla = document.createElement('table');
    tabla.className = 'table w-full';
    tabla.innerHTML = `
        <thead>
            <tr>
                <th>Fecha</th>
                <th>ID</th>
                <th>Descripción</th>
                <th>Cuenta</th>
                <th>Débito</th>
                <th>Crédito</th>
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
        return `${cuenta.codigo} - ${cuenta.nombre}`;
    }
    return `Cuenta no encontrada (ID: ${idCuenta})`;
}

btnFiltrar.addEventListener('click', async (event) => {
    event.preventDefault();
    
    if (!periodoSeleccionado) {
        alert('Por favor seleccione un período');
        return;
    }
    
    // Mostrar indicador de carga
    btnFiltrar.textContent = 'Cargando...';
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
        alert('Error al cargar las partidas. Por favor intente nuevamente.');
    } finally {
        // Restaurar botón
        btnFiltrar.textContent = 'Filtrar';
        btnFiltrar.disabled = false;
    }
});

window.addEventListener('DOMContentLoaded', () => {
    cargarPeriodos();
});
