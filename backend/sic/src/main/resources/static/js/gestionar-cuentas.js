// Gestión de Cuentas Contables
document.addEventListener('DOMContentLoaded', function() {
    
    // Variables globales
    let cuentas = [];
    let cuentasFiltradas = [];
    let modoEdicion = false;

    // Elementos del DOM
    const tablaCuentas = document.getElementById('tablaCuentas');
    const btnNuevaCuenta = document.getElementById('btnNuevaCuenta');
    const modalCuenta = document.getElementById('modalCuenta');
    const modalEliminar = document.getElementById('modalEliminar');
    const formCuenta = document.getElementById('formCuenta');
    const tituloModal = document.getElementById('tituloModal');
    const buscarCuenta = document.getElementById('buscarCuenta');
    const filtroTipo = document.getElementById('filtroTipo');
    const filtroSaldo = document.getElementById('filtroSaldo');
    const btnLimpiarFiltros = document.getElementById('btnLimpiarFiltros');
    const btnConfirmarEliminar = document.getElementById('btnConfirmarEliminar');

    // Cargar cuentas al iniciar
    cargarCuentas();

    // Event Listeners
    btnNuevaCuenta.addEventListener('click', abrirModalNueva);
    formCuenta.addEventListener('submit', guardarCuenta);
    buscarCuenta.addEventListener('input', aplicarFiltros);
    filtroTipo.addEventListener('change', aplicarFiltros);
    filtroSaldo.addEventListener('change', aplicarFiltros);
    btnLimpiarFiltros.addEventListener('click', limpiarFiltros);
    btnConfirmarEliminar.addEventListener('click', confirmarEliminar);

    /**
     * Cargar todas las cuentas desde el API
     */
    function cargarCuentas() {
        fetch('/api/cuentas')
            .then(response => response.json())
            .then(data => {
                cuentas = data;
                cuentasFiltradas = [...cuentas];
                renderizarTabla();
                actualizarEstadisticas();
                cargarOpcionesPadre();
            })
            .catch(error => {
                console.error('Error al cargar cuentas:', error);
                mostrarToast('Error al cargar las cuentas', 'error');
                tablaCuentas.innerHTML = `
                    <tr>
                        <td colspan="6" class="text-center py-8 text-error">
                            Error al cargar las cuentas. Por favor, recarga la página.
                        </td>
                    </tr>
                `;
            });
    }

    /**
     * Renderizar la tabla de cuentas
     */
    function renderizarTabla() {
        if (cuentasFiltradas.length === 0) {
            tablaCuentas.innerHTML = `
                <tr>
                    <td colspan="6" class="text-center py-8">
                        <div class="flex flex-col items-center gap-2">
                            <svg xmlns="http://www.w3.org/2000/svg" class="h-12 w-12 text-base-content/50" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                            </svg>
                            <p class="text-base-content/70">No se encontraron cuentas</p>
                        </div>
                    </td>
                </tr>
            `;
            return;
        }

        tablaCuentas.innerHTML = cuentasFiltradas.map(cuenta => {
            const nombrePadre = cuenta.idPadre ? obtenerNombrePadre(cuenta.idPadre) : '-';
            
            return `
                <tr class="hover">
                    <td class="font-mono font-semibold">${cuenta.codigo}</td>
                    <td>${cuenta.nombre}</td>
                    <td>
                        <span class="badge ${obtenerColorTipo(cuenta.tipo)}">${cuenta.tipo}</span>
                    </td>
                    <td>
                        <span class="badge ${cuenta.saldoNormal === 'DEUDOR' ? 'badge-info' : 'badge-warning'}">
                            ${cuenta.saldoNormal}
                        </span>
                    </td>
                    <td class="text-sm text-base-content/70">${nombrePadre}</td>
                    <td>
                        <div class="flex gap-2 justify-center">
                            <button onclick="editarCuenta(${cuenta.idCuenta})" 
                                    class="btn btn-sm btn-ghost btn-square" 
                                    title="Editar">
                                <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
                                </svg>
                            </button>
                            <button onclick="abrirModalEliminar(${cuenta.idCuenta}, '${cuenta.nombre.replace(/'/g, "\\'")}')" 
                                    class="btn btn-sm btn-ghost btn-square text-error" 
                                    title="Eliminar">
                                <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                                </svg>
                            </button>
                        </div>
                    </td>
                </tr>
            `;
        }).join('');
    }

    /**
     * Obtener color del badge según el tipo de cuenta
     */
    function obtenerColorTipo(tipo) {
        switch(tipo) {
            case 'ACTIVO': return 'badge-success';
            case 'PASIVO': return 'badge-warning';
            case 'CAPITAL CONTABLE': return 'badge-info';
            default: return 'badge-ghost';
        }
    }

    /**
     * Obtener nombre de cuenta padre
     */
    function obtenerNombrePadre(idPadre) {
        const padre = cuentas.find(c => c.idCuenta === idPadre);
        return padre ? `${padre.codigo} - ${padre.nombre}` : 'Desconocida';
    }

    /**
     * Actualizar estadísticas
     */
    function actualizarEstadisticas() {
        const totalActivos = cuentas.filter(c => c.tipo === 'ACTIVO').length;
        const totalPasivos = cuentas.filter(c => c.tipo === 'PASIVO').length;
        const totalCapital = cuentas.filter(c => c.tipo === 'CAPITAL CONTABLE').length;

        document.getElementById('totalCuentas').textContent = cuentas.length;
        document.getElementById('totalActivos').textContent = totalActivos;
        document.getElementById('totalPasivos').textContent = totalPasivos;
        document.getElementById('totalCapital').textContent = totalCapital;
    }

    /**
     * Cargar opciones de cuenta padre en el select
     */
    function cargarOpcionesPadre(excluirId = null) {
        const selectPadre = document.getElementById('idPadre');
        selectPadre.innerHTML = '<option value="">Sin cuenta padre</option>';
        
        cuentas
            .filter(c => c.idCuenta !== excluirId)
            .forEach(cuenta => {
                const option = document.createElement('option');
                option.value = cuenta.idCuenta;
                option.textContent = `${cuenta.codigo} - ${cuenta.nombre}`;
                selectPadre.appendChild(option);
            });
    }

    /**
     * Abrir modal para nueva cuenta
     */
    function abrirModalNueva() {
        modoEdicion = false;
        tituloModal.textContent = 'Nueva Cuenta';
        formCuenta.reset();
        document.getElementById('cuentaId').value = '';
        cargarOpcionesPadre();
        modalCuenta.showModal();
    }

    /**
     * Editar cuenta existente
     */
    window.editarCuenta = function(id) {
        const cuenta = cuentas.find(c => c.idCuenta === id);
        if (!cuenta) {
            mostrarToast('Cuenta no encontrada', 'error');
            return;
        }

        modoEdicion = true;
        tituloModal.textContent = 'Editar Cuenta';
        
        document.getElementById('cuentaId').value = cuenta.idCuenta;
        document.getElementById('codigo').value = cuenta.codigo;
        document.getElementById('nombre').value = cuenta.nombre;
        document.getElementById('tipo').value = cuenta.tipo;
        document.getElementById('saldoNormal').value = cuenta.saldoNormal;
        
        cargarOpcionesPadre(cuenta.idCuenta);
        document.getElementById('idPadre').value = cuenta.idPadre || '';
        
        modalCuenta.showModal();
    };

    /**
     * Guardar cuenta (crear o actualizar)
     */
    function guardarCuenta(e) {
        e.preventDefault();

        const cuentaData = {
            codigo: document.getElementById('codigo').value.trim(),
            nombre: document.getElementById('nombre').value.trim().toUpperCase(),
            tipo: document.getElementById('tipo').value,
            saldoNormal: document.getElementById('saldoNormal').value,
            idPadre: document.getElementById('idPadre').value || null
        };

        const id = document.getElementById('cuentaId').value;

        if (modoEdicion && id) {
            // Actualizar cuenta existente
            cuentaData.idCuenta = parseInt(id);
            actualizarCuenta(cuentaData);
        } else {
            // Crear nueva cuenta
            crearCuenta(cuentaData);
        }
    }

    /**
     * Crear nueva cuenta
     */
    function crearCuenta(cuentaData) {
        fetch('/api/cuentas/insertar', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(cuentaData)
        })
        .then(response => {
            if (!response.ok) throw new Error('Error al crear la cuenta');
            return response.json();
        })
        .then(data => {
            mostrarToast('Cuenta creada exitosamente', 'success');
            modalCuenta.close();
            cargarCuentas();
        })
        .catch(error => {
            console.error('Error:', error);
            mostrarToast('Error al crear la cuenta', 'error');
        });
    }

    /**
     * Actualizar cuenta existente
     */
    function actualizarCuenta(cuentaData) {
        fetch(`/api/cuentas/${cuentaData.idCuenta}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(cuentaData)
        })
        .then(response => {
            if (!response.ok) throw new Error('Error al actualizar la cuenta');
            return response.json();
        })
        .then(data => {
            mostrarToast('Cuenta actualizada exitosamente', 'success');
            modalCuenta.close();
            cargarCuentas();
        })
        .catch(error => {
            console.error('Error:', error);
            mostrarToast('Error al actualizar la cuenta', 'error');
        });
    }

    /**
     * Abrir modal de confirmación para eliminar
     */
    window.abrirModalEliminar = function(id, nombre) {
        document.getElementById('cuentaEliminarId').value = id;
        document.getElementById('cuentaEliminarNombre').textContent = nombre;
        modalEliminar.showModal();
    };

    /**
     * Confirmar eliminación de cuenta
     */
    function confirmarEliminar() {
        const id = document.getElementById('cuentaEliminarId').value;
        
        fetch(`/api/cuentas/${id}`, {
            method: 'DELETE'
        })
        .then(response => {
            if (!response.ok) throw new Error('Error al eliminar la cuenta');
            mostrarToast('Cuenta eliminada exitosamente', 'success');
            modalEliminar.close();
            cargarCuentas();
        })
        .catch(error => {
            console.error('Error:', error);
            mostrarToast('Error al eliminar la cuenta. Puede tener registros asociados.', 'error');
        });
    }

    /**
     * Aplicar filtros de búsqueda y selects
     */
    function aplicarFiltros() {
        const textoBusqueda = buscarCuenta.value.toLowerCase();
        const tipoSeleccionado = filtroTipo.value;
        const saldoSeleccionado = filtroSaldo.value;

        cuentasFiltradas = cuentas.filter(cuenta => {
            const coincideTexto = 
                cuenta.codigo.toLowerCase().includes(textoBusqueda) ||
                cuenta.nombre.toLowerCase().includes(textoBusqueda);
            
            const coincideTipo = !tipoSeleccionado || cuenta.tipo === tipoSeleccionado;
            const coincideSaldo = !saldoSeleccionado || cuenta.saldoNormal === saldoSeleccionado;

            return coincideTexto && coincideTipo && coincideSaldo;
        });

        renderizarTabla();
    }

    /**
     * Limpiar todos los filtros
     */
    function limpiarFiltros() {
        buscarCuenta.value = '';
        filtroTipo.value = '';
        filtroSaldo.value = '';
        cuentasFiltradas = [...cuentas];
        renderizarTabla();
    }

});
