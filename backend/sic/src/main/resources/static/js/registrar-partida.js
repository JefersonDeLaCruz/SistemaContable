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
            option.value = cuenta.id;
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

const contenedor = document.querySelector(".contenedor-movimientos");
const btnAgregarMovimiento = document.querySelector(".agregar-movimiento");
const body = document.body;

// Agrega una nueva linea de detalle de transaccion
async function agregarLinea() {
    const primeraLinea = contenedor.querySelector(".linea");
    const nuevaLinea = primeraLinea.cloneNode(true);
    const lineas = contenedor.querySelectorAll(".linea");
    const nuevoIndice = lineas.length;

    nuevaLinea.querySelectorAll("input, select").forEach(el => {
        el.value = "";
        // Actualizar los nombres para el array
        if (el.name.includes("detalles[")) {
            el.name = el.name.replace(/detalles\[\d+\]/, `detalles[${nuevoIndice}]`);
        }
    });

    contenedor.appendChild(nuevaLinea);
    
    // Cargar cuentas en el select de la nueva línea
    const nuevoSelect = nuevaLinea.querySelector('select[select_cuenta=""]');
    console.log(nuevoSelect, "nuevo select");
    if (nuevoSelect) {
        await cargarCuentasEnSelect(nuevoSelect);
    }
    
    actualizarEventosEliminar();
    actualizarIndices();
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
        actualizarIndices(); // Reindexar después de eliminar
    } else {
        // alert("Debe haber al menos dos líneas de movimiento.");
        const alerta = document.getElementById("alerta");
        let timeoutId = alerta.dataset.timeoutId;


        if (timeoutId) {
            clearTimeout(timeoutId);
            alerta.dataset.timeoutId = "";
        }


        if (alerta.classList.contains("hidden")) {
            alerta.classList.remove("hidden", "hide");
            setTimeout(() => alerta.classList.add("show"), 10);
        } else {
            alerta.classList.remove("hide");
            alerta.classList.add("show");
        }

        const id = setTimeout(() => {
            alerta.classList.remove("show");
            alerta.classList.add("hide");
            setTimeout(() => alerta.classList.add("hidden"), 300);
            alerta.dataset.timeoutId = "";
        }, 3000);

        alerta.dataset.timeoutId = id;
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
});

btnAgregarMovimiento.addEventListener("click", async (event) => {
    event.preventDefault();
    await agregarLinea();
});
