// Variables globales
let partidaId = null;
let totalDebito = 0;
let totalCredito = 0;
const contenedor = document.getElementById("contenedor-movimientos");
const btnAgregarMovimiento = document.getElementById("btnAgregarMovimiento");
const formEditarPartida = document.getElementById("formEditarPartida");

// Función para obtener el ID de la partida desde la URL
function obtenerPartidaIdDesdeURL() {
    const params = new URLSearchParams(window.location.search);
    return params.get('id');
}

// Función para cargar cuentas en un select específico
async function cargarCuentasEnSelect(select, cuentaSeleccionada = null) {
    try {
        const response = await fetch('/api/cuentas');
        const cuentas = await response.json();

        select.innerHTML = '';
        const optionDefault = document.createElement('option');
        optionDefault.value = "";
        optionDefault.textContent = "Seleccione una cuenta";
        select.appendChild(optionDefault);

        cuentas.forEach(cuenta => {
            const option = document.createElement('option');
            option.value = cuenta.idCuenta;
            option.textContent = `${cuenta.codigo} - ${cuenta.nombre}`;
            if (cuentaSeleccionada && cuenta.idCuenta == cuentaSeleccionada) {
                option.selected = true;
            }
            select.appendChild(option);
        });
    } catch (error) {
        console.error('Error cargando las cuentas:', error);
    }
}

// Función para cargar períodos
async function cargarPeriodos(periodoSeleccionado = null) {
    try {
        const response = await fetch('/api/periodos');
        const periodos = await response.json();

        const select = document.getElementById('idPeriodo');
        select.innerHTML = '<option value="">Seleccione un período...</option>';

        periodos.forEach(periodo => {
            const option = document.createElement('option');
            option.value = periodo.idPeriodo;
            option.textContent = periodo.nombre;
            if (periodoSeleccionado && periodo.idPeriodo == periodoSeleccionado) {
                option.selected = true;
            }
            select.appendChild(option);
        });
    } catch (error) {
        console.error('Error cargando los períodos:', error);
    }
}

// Función para formatear moneda
function formatearMoneda(valor) {
    return new Intl.NumberFormat('es-SV', {
        style: 'currency',
        currency: 'USD',
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
    }).format(valor);
}

// Función para calcular y actualizar totales
function actualizarTotales() {
    totalDebito = 0;
    totalCredito = 0;

    document.querySelectorAll('.debito-input').forEach(input => {
        const valor = parseFloat(input.value) || 0;
        totalDebito += valor;
    });

    document.querySelectorAll('.credito-input').forEach(input => {
        const valor = parseFloat(input.value) || 0;
        totalCredito += valor;
    });

    document.getElementById('total-debito').textContent = formatearMoneda(totalDebito);
    document.getElementById('total-credito').textContent = formatearMoneda(totalCredito);

    actualizarEstado();
}

// Función para actualizar el estado de la partida
function actualizarEstado() {
    const estadoElement = document.getElementById('estado');
    const estadoDescElement = document.getElementById('estado-desc');

    const diferencia = Math.abs(totalDebito - totalCredito);

    if (totalDebito === 0 && totalCredito === 0) {
        estadoElement.innerHTML = '<span class="loading loading-dots loading-sm"></span>';
        estadoElement.className = 'stat-value text-base-content';
        estadoDescElement.textContent = 'Esperando valores...';
    } else if (diferencia < 0.01) {
        estadoElement.innerHTML = `
            <div class="flex items-center gap-2">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-8 w-8" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
                <span>Balanceada</span>
            </div>
        `;
        estadoElement.className = 'stat-value text-success';
        estadoDescElement.textContent = '¡La partida está cuadrada!';
    } else {
        estadoElement.innerHTML = `
            <div class="flex items-center gap-2">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-8 w-8" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
                <span>Descuadrada</span>
            </div>
        `;
        estadoElement.className = 'stat-value text-error';
        estadoDescElement.textContent = `Diferencia: ${formatearMoneda(diferencia)}`;
    }
}

// Agregar event listeners a los inputs
function agregarEventListenersInputs() {
    document.querySelectorAll('.debito-input, .credito-input').forEach(input => {
        input.removeEventListener('input', actualizarTotales);
        input.addEventListener('input', actualizarTotales);

        input.addEventListener('input', function() {
            const linea = this.closest('.linea');
            const debitoInput = linea.querySelector('.debito-input');
            const creditoInput = linea.querySelector('.credito-input');

            if (this.classList.contains('debito-input') && this.value) {
                creditoInput.value = '';
            } else if (this.classList.contains('credito-input') && this.value) {
                debitoInput.value = '';
            }
        });
    });
}

// Crear una línea de movimiento
async function crearLineaMovimiento(detalle = null, index = 0) {
    const lineaHTML = `
        <div class="linea bg-base-100 p-4 rounded-lg shadow">
            <div class="grid grid-cols-1 md:grid-cols-5 gap-4 items-end">
                <div class="form-control md:col-span-2">
                    <label class="label">
                        <span class="label-text font-semibold">Cuenta Contable</span>
                        <span class="label-text-alt text-error">*</span>
                    </label>
                    <select name="detalles[${index}].idCuenta" class="select select-bordered w-full cuenta-select" required>
                        <option value="">Seleccione una cuenta...</option>
                    </select>
                </div>

                <div class="form-control md:col-span-2">
                    <label class="label">
                        <span class="label-text font-semibold">Descripción</span>
                    </label>
                    <input name="detalles[${index}].descripcion" type="text"
                        class="input input-bordered w-full descripcion-input"
                        value="${detalle ? detalle.descripcion : ''}"
                        placeholder="Detalle del movimiento" />
                </div>

                <div class="form-control">
                    <label class="label">
                        <span class="label-text font-semibold">Débito</span>
                    </label>
                    <input name="detalles[${index}].debito" type="number" step="0.01"
                        class="input input-bordered w-full debito-input"
                        value="${detalle ? detalle.debito : 0}"
                        placeholder="0.00" min="0" />
                </div>

                <div class="form-control">
                    <label class="label">
                        <span class="label-text font-semibold">Crédito</span>
                    </label>
                    <input name="detalles[${index}].credito" type="number" step="0.01"
                        class="input input-bordered w-full credito-input"
                        value="${detalle ? detalle.credito : 0}"
                        placeholder="0.00" min="0" />
                </div>

                <div class="form-control flex justify-end">
                    <button type="button" class="eliminar btn btn-error btn-sm gap-2">
                        <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none"
                            viewBox="0 0 24 24" stroke="currentColor">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                        </svg>
                        Eliminar
                    </button>
                </div>
            </div>
        </div>
    `;

    contenedor.insertAdjacentHTML('beforeend', lineaHTML);

    const nuevaLinea = contenedor.lastElementChild;
    const select = nuevaLinea.querySelector('.cuenta-select');
    await cargarCuentasEnSelect(select, detalle ? detalle.idCuenta : null);

    actualizarEventosEliminar();
    agregarEventListenersInputs();
}

// Agregar nueva línea vacía
async function agregarLinea() {
    const lineas = contenedor.querySelectorAll(".linea");
    await crearLineaMovimiento(null, lineas.length);
    actualizarTotales();
}

// Eliminar línea de movimiento
function eliminarLinea(event) {
    event.preventDefault();
    const lineas = contenedor.querySelectorAll(".linea");
    if (lineas.length > 2) {
        event.target.closest(".linea").remove();
        actualizarIndices();
        actualizarTotales();
    } else {
        const alerta = document.getElementById("alerta");
        alerta.classList.remove("hidden");
        setTimeout(() => {
            alerta.classList.add("hidden");
        }, 3000);
    }
}

// Actualizar índices de los inputs
function actualizarIndices() {
    const lineas = contenedor.querySelectorAll(".linea");
    lineas.forEach((linea, index) => {
        linea.querySelectorAll("input, select").forEach(el => {
            if (el.name.includes("detalles[")) {
                const nombreBase = el.name.replace(/detalles\[\d+\]\./, "");
                el.name = `detalles[${index}].${nombreBase}`;
            }
        });
    });
}

// Actualizar eventos de eliminar
function actualizarEventosEliminar() {
    contenedor.querySelectorAll(".eliminar").forEach(btn => {
        btn.onclick = eliminarLinea;
    });
}

// Cargar datos de la partida
async function cargarDatosPartida() {
    try {
        const response = await fetch(`/api/partidas/${partidaId}`);

        if (!response.ok) {
            throw new Error('No se pudo cargar la partida');
        }

        const partida = await response.json();

        // Poblar campos del formulario
        document.getElementById('partidaId').value = partida.id;
        document.getElementById('descripcion').value = partida.descripcion;
        document.getElementById('fecha').value = partida.fecha;

        // Cargar períodos y seleccionar el actual
        await cargarPeriodos(partida.idPeriodo);

        // Mostrar información de auditoría
        const creadorInfo = document.getElementById('creadorInfo');
        if (partida.createdBy) {
            creadorInfo.textContent = `${partida.createdBy} el ${new Date(partida.createdAt).toLocaleString('es-SV')}`;
        } else {
            creadorInfo.textContent = `${partida.idUsuario} el ${partida.fecha}`;
        }

        if (partida.updatedBy) {
            const infoModificacion = document.getElementById('infoModificacion');
            const modificacionInfo = document.getElementById('modificacionInfo');
            modificacionInfo.textContent = `${partida.updatedBy} el ${new Date(partida.updatedAt).toLocaleString('es-SV')}`;
            infoModificacion.classList.remove('hidden');
        }

        // Limpiar contenedor de movimientos
        contenedor.innerHTML = '';

        // Crear líneas de movimiento con los detalles existentes
        for (let i = 0; i < partida.detalles.length; i++) {
            await crearLineaMovimiento(partida.detalles[i], i);
        }

        // Actualizar totales
        actualizarTotales();

        // Ocultar alerta de carga y mostrar formulario
        document.getElementById('alertaCarga').classList.add('hidden');
        formEditarPartida.classList.remove('hidden');

    } catch (error) {
        console.error('Error cargando la partida:', error);
        document.getElementById('alertaCarga').classList.add('hidden');
        document.getElementById('alertaError').classList.remove('hidden');
        document.getElementById('mensajeError').textContent = error.message;
    }
}

// Recolectar datos del formulario
function recolectarDatosFormulario() {
    const detalles = [];
    const lineas = contenedor.querySelectorAll('.linea');

    lineas.forEach((linea, index) => {
        const idCuenta = linea.querySelector(`select[name="detalles[${index}].idCuenta"]`).value;
        const descripcion = linea.querySelector(`input[name="detalles[${index}].descripcion"]`).value;
        const debito = parseFloat(linea.querySelector(`input[name="detalles[${index}].debito"]`).value) || 0;
        const credito = parseFloat(linea.querySelector(`input[name="detalles[${index}].credito"]`).value) || 0;

        if (idCuenta && (debito > 0 || credito > 0)) {
            detalles.push({
                idCuenta: idCuenta,
                descripcion: descripcion || 'Movimiento contable',
                debito: debito,
                credito: credito
            });
        }
    });

    return {
        id: partidaId,
        descripcion: document.getElementById('descripcion').value,
        fecha: document.getElementById('fecha').value,
        idPeriodo: document.getElementById('idPeriodo').value,
        razonCambio: document.getElementById('razonCambio').value,
        detalles: detalles
    };
}

// Enviar cambios al servidor
async function guardarCambios(event) {
    event.preventDefault();

    // Validar que la partida esté balanceada
    const diferencia = Math.abs(totalDebito - totalCredito);
    if (diferencia >= 0.01) {
        mostrarAlerta('error', 'La partida no está balanceada',
            `El Debe debe ser igual al Haber. Diferencia: ${formatearMoneda(diferencia)}`);
        return;
    }

    // Validar que se proporcionó una razón del cambio
    const razonCambio = document.getElementById('razonCambio').value.trim();
    if (!razonCambio) {
        mostrarAlerta('warning', 'Razón del cambio requerida',
            'Debe proporcionar una razón para el cambio (auditoría contable)');
        return;
    }

    // Recolectar datos
    const datos = recolectarDatosFormulario();

    // Validar que hay detalles
    if (datos.detalles.length < 2) {
        mostrarAlerta('warning', 'Partida incompleta',
            'Debe haber al menos dos líneas de movimiento');
        return;
    }

    // Mostrar indicador de carga
    const btnGuardar = document.getElementById('btnGuardarCambios');
    const textoOriginal = btnGuardar.innerHTML;
    btnGuardar.innerHTML = '<span class="loading loading-spinner"></span> Guardando...';
    btnGuardar.disabled = true;

    try {
        const response = await fetch(`/api/partidas/${partidaId}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(datos)
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText || 'Error al guardar los cambios');
        }

        // Éxito
        mostrarAlerta('success', '¡Cambios guardados!',
            'La partida ha sido actualizada correctamente. Redirigiendo...');

        setTimeout(() => {
            window.location.href = '/libro-diario';
        }, 2000);

    } catch (error) {
        console.error('Error guardando cambios:', error);
        mostrarAlerta('error', 'Error al guardar', error.message);
        btnGuardar.innerHTML = textoOriginal;
        btnGuardar.disabled = false;
    }
}

// Mostrar alerta
function mostrarAlerta(tipo, titulo, mensaje) {
    const iconos = {
        success: '<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />',
        error: '<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 14l2-2m0 0l2-2m-2 2l-2-2m2 2l2 2m7-2a9 9 0 11-18 0 9 9 0 0118 0z" />',
        warning: '<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />',
        info: '<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />'
    };

    const alertaHTML = `
        <div class="alert alert-${tipo} shadow-lg mb-4" id="alerta-dinamica">
            <div>
                <svg xmlns="http://www.w3.org/2000/svg" class="stroke-current flex-shrink-0 h-6 w-6" fill="none" viewBox="0 0 24 24">
                    ${iconos[tipo]}
                </svg>
                <div>
                    <h3 class="font-bold">${titulo}</h3>
                    <div class="text-xs">${mensaje}</div>
                </div>
            </div>
        </div>
    `;

    const alertaExistente = document.getElementById('alerta-dinamica');
    if (alertaExistente) {
        alertaExistente.remove();
    }
    formEditarPartida.insertAdjacentHTML('afterbegin', alertaHTML);
    formEditarPartida.scrollIntoView({ behavior: 'smooth', block: 'start' });

    setTimeout(() => {
        const alerta = document.getElementById('alerta-dinamica');
        if (alerta && tipo !== 'success') {
            alerta.remove();
        }
    }, 5000);
}

// Ver historial de cambios
async function verHistorial() {
    const modal = document.getElementById('modalHistorial');
    const contenido = document.getElementById('contenidoHistorial');

    modal.showModal();
    contenido.innerHTML = '<div class="flex justify-center p-8"><span class="loading loading-spinner loading-lg"></span></div>';

    try {
        const response = await fetch(`/api/partidas/historial/${partidaId}`);
        if (!response.ok) {
            throw new Error('No se pudo cargar el historial');
        }

        const historial = await response.json();

        if (historial.length === 0) {
            contenido.innerHTML = '<p class="text-center p-8">No hay cambios registrados</p>';
            return;
        }

        let tablaHTML = `
            <table class="table table-zebra w-full">
                <thead>
                    <tr>
                        <th>Fecha</th>
                        <th>Operación</th>
                        <th>Usuario</th>
                        <th>Razón</th>
                        <th>IP</th>
                    </tr>
                </thead>
                <tbody>
        `;

        historial.forEach(cambio => {
            const fecha = new Date(cambio.fechaCambio).toLocaleString('es-SV');
            const operacion = cambio.operacion === 'UPDATE' ? 'Edición' : cambio.operacion;

            tablaHTML += `
                <tr>
                    <td>${fecha}</td>
                    <td><span class="badge badge-info">${operacion}</span></td>
                    <td>${cambio.usuarioId}</td>
                    <td>${cambio.razonCambio || '-'}</td>
                    <td class="text-xs">${cambio.ipOrigen || '-'}</td>
                </tr>
            `;
        });

        tablaHTML += '</tbody></table>';
        contenido.innerHTML = tablaHTML;

    } catch (error) {
        console.error('Error cargando historial:', error);
        contenido.innerHTML = '<p class="text-center text-error p-8">Error al cargar el historial</p>';
    }
}

// Inicialización
window.addEventListener("DOMContentLoaded", async () => {
    partidaId = obtenerPartidaIdDesdeURL();

    if (!partidaId) {
        document.getElementById('alertaCarga').classList.add('hidden');
        document.getElementById('alertaError').classList.remove('hidden');
        document.getElementById('mensajeError').textContent = 'ID de partida no proporcionado';
        return;
    }

    await cargarDatosPartida();

    // Event listeners
    btnAgregarMovimiento.addEventListener("click", agregarLinea);
    formEditarPartida.addEventListener("submit", guardarCambios);
    document.getElementById('btnCancelar').addEventListener('click', () => {
        if (confirm('¿Está seguro de cancelar? Los cambios no guardados se perderán.')) {
            window.location.href = '/libro-diario';
        }
    });
    document.getElementById('btnVerHistorial').addEventListener('click', verHistorial);
});
