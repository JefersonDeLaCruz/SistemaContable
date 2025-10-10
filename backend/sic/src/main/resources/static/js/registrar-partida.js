// Para cargar cuentas en un select específico
async function cargarCuentasEnSelect(select) {
    try {
        const response = await fetch('/api/cuentas');
        const cuentas = await response.json();

        // Limpiar el select y agregar opción por defecto
        select.innerHTML = '';
        const optionDefault = document.createElement('option');
        optionDefault.value = "";
        optionDefault.textContent = "Seleccione una cuenta";
        select.appendChild(optionDefault);
        
        // Agregar las cuentas
        cuentas.forEach(cuenta => {
            const option = document.createElement('option');
            option.value = cuenta.idCuenta;
            option.textContent = `${cuenta.codigo} - ${cuenta.nombre}`;
            select.appendChild(option);
        });
    } catch (error) {
        console.error('Error cargando las cuentas:', error);
    }
}

// Para cargar cuentas en todos los selects de cuenta existentes
async function cargarTodasLasCuentas() {
    const selects = contenedor.querySelectorAll('select[select_cuenta=""]');
    for (const select of selects) {
        await cargarCuentasEnSelect(select);
    }
}

 // Para cargar periodos en el <select>
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


const contenedor = document.querySelector(".contenedor-movimientos");
const btnAgregarMovimiento = document.querySelector(".agregar-movimiento");
const formPartida = document.getElementById("formPartida");

// Variables para los totales
let totalDebito = 0;
let totalCredito = 0;

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
    
    // Sumar todos los débitos
    document.querySelectorAll('.debito-input').forEach(input => {
        const valor = parseFloat(input.value) || 0;
        totalDebito += valor;
    });
    
    // Sumar todos los créditos
    document.querySelectorAll('.credito-input').forEach(input => {
        const valor = parseFloat(input.value) || 0;
        totalCredito += valor;
    });
    
    // Actualizar los elementos en el DOM
    document.getElementById('total-debito').textContent = formatearMoneda(totalDebito);
    document.getElementById('total-credito').textContent = formatearMoneda(totalCredito);
    
    // Actualizar estado
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
    } else if (diferencia < 0.01) { // Consideramos iguales si la diferencia es menor a 1 centavo
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

// Agregar event listeners a los inputs existentes y futuros
function agregarEventListenersInputs() {
    document.querySelectorAll('.debito-input, .credito-input').forEach(input => {
        // Remover listeners anteriores para evitar duplicados
        input.removeEventListener('input', actualizarTotales);
        input.addEventListener('input', actualizarTotales);
        
        // Validación: no permitir valores negativos ni ambos campos llenos
        input.addEventListener('input', function() {
            const linea = this.closest('.linea');
            const debitoInput = linea.querySelector('.debito-input');
            const creditoInput = linea.querySelector('.credito-input');
            
            // Si se llena un campo, limpiar el otro
            if (this.classList.contains('debito-input') && this.value) {
                creditoInput.value = '';
            } else if (this.classList.contains('credito-input') && this.value) {
                debitoInput.value = '';
            }
        });
    });
}

// Agrega una nueva linea de detalle de transaccion
async function agregarLinea() {
    const primeraLinea = contenedor.querySelector(".linea");
    const nuevaLinea = primeraLinea.cloneNode(true);
    const lineas = contenedor.querySelectorAll(".linea");
    const nuevoIndice = lineas.length;

    // Limpiar valores y actualizar nombres
    nuevaLinea.querySelectorAll("input, select").forEach(el => {
        el.value = "";
        // Actualizar los nombres para el array
        if (el.name.includes("detalles[")) {
            el.name = el.name.replace(/detalles\[\d+\]/, `detalles[${nuevoIndice}]`);
        }
    });

    // Agregar animación de entrada
    nuevaLinea.style.opacity = '0';
    nuevaLinea.style.transform = 'translateY(-10px)';
    contenedor.appendChild(nuevaLinea);
    
    // Animar entrada
    setTimeout(() => {
        nuevaLinea.style.transition = 'all 0.3s ease';
        nuevaLinea.style.opacity = '1';
        nuevaLinea.style.transform = 'translateY(0)';
    }, 10);
    
    // Cargar cuentas en el select de la nueva línea
    const nuevoSelect = nuevaLinea.querySelector('select[select_cuenta=""]');
    if (nuevoSelect) {
        await cargarCuentasEnSelect(nuevoSelect);
    }
    
    actualizarEventosEliminar();
    actualizarIndices();
    agregarEventListenersInputs();
    actualizarTotales();
}

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

function actualizarEventosEliminar() {
    contenedor.querySelectorAll(".eliminar").forEach(btn => {
        btn.onclick = eliminarLinea;
    });
}

window.addEventListener("DOMContentLoaded", async () => {
    // Primero verificar si necesitamos agregar una segunda línea
    const lineas = contenedor.querySelectorAll(".linea");
    if (lineas.length < 2) {
        await agregarLinea();
    }
    
    // Luego cargar cuentas en todos los selects existentes
    await cargarTodasLasCuentas();
    
    actualizarEventosEliminar();
    cargarPeriodos();
    agregarEventListenersInputs();
    actualizarTotales();
});

btnAgregarMovimiento.addEventListener("click", async (event) => {
    event.preventDefault();
    await agregarLinea();
});

// Validación antes de enviar el formulario
formPartida.addEventListener("submit", function(event) {
    const diferencia = Math.abs(totalDebito - totalCredito);
    
    // Verificar que la partida esté balanceada
    if (diferencia >= 0.01) {
        event.preventDefault();
        
        // Mostrar alerta de error
        const alertaHTML = `
            <div class="alert alert-error shadow-lg mb-4" id="alerta-desbalance">
                <div>
                    <svg xmlns="http://www.w3.org/2000/svg" class="stroke-current flex-shrink-0 h-6 w-6" fill="none" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 14l2-2m0 0l2-2m-2 2l-2-2m2 2l2 2m7-2a9 9 0 11-18 0 9 9 0 0118 0z" />
                    </svg>
                    <div>
                        <h3 class="font-bold">¡Error! La partida no está balanceada</h3>
                        <div class="text-xs">El Debe debe ser igual al Haber. Diferencia: ${formatearMoneda(diferencia)}</div>
                    </div>
                </div>
            </div>
        `;
        
        // Insertar alerta al inicio del formulario
        const alertaExistente = document.getElementById('alerta-desbalance');
        if (alertaExistente) {
            alertaExistente.remove();
        }
        formPartida.insertAdjacentHTML('afterbegin', alertaHTML);
        
        // Scroll hacia arriba para ver la alerta
        formPartida.scrollIntoView({ behavior: 'smooth', block: 'start' });
        
        // Remover alerta después de 5 segundos
        setTimeout(() => {
            const alerta = document.getElementById('alerta-desbalance');
            if (alerta) {
                alerta.remove();
            }
        }, 5000);
        
        return false;
    }
    
    // Verificar que haya al menos un débito y un crédito
    if (totalDebito === 0 || totalCredito === 0) {
        event.preventDefault();
        
        const alertaHTML = `
            <div class="alert alert-warning shadow-lg mb-4" id="alerta-vacia">
                <div>
                    <svg xmlns="http://www.w3.org/2000/svg" class="stroke-current flex-shrink-0 h-6 w-6" fill="none" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
                    </svg>
                    <div>
                        <h3 class="font-bold">¡Advertencia! Partida incompleta</h3>
                        <div class="text-xs">Debe ingresar al menos un débito y un crédito</div>
                    </div>
                </div>
            </div>
        `;
        
        const alertaExistente = document.getElementById('alerta-vacia');
        if (alertaExistente) {
            alertaExistente.remove();
        }
        formPartida.insertAdjacentHTML('afterbegin', alertaHTML);
        
        formPartida.scrollIntoView({ behavior: 'smooth', block: 'start' });
        
        setTimeout(() => {
            const alerta = document.getElementById('alerta-vacia');
            if (alerta) {
                alerta.remove();
            }
        }, 5000);
        
        return false;
    }
    
    // Si pasa todas las validaciones, mostrar confirmación
    const btnRegistrar = document.getElementById('btnRegistrar');
    btnRegistrar.innerHTML = `
        <span class="loading loading-spinner"></span>
        Registrando...
    `;
    btnRegistrar.disabled = true;
});
