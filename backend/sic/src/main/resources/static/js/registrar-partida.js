console.log("avla que jopa");

const contenedor = document.querySelector(".contenedor-movimientos");
const btnAgregarMovimiento = document.querySelector(".agregar-movimiento");
const body = document.body;

function agregarLinea() {
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

window.addEventListener("DOMContentLoaded", () => {
    const lineas = contenedor.querySelectorAll(".linea");
    if (lineas.length < 2) {
        agregarLinea();
    }
    actualizarEventosEliminar();
});

btnAgregarMovimiento.addEventListener("click", (event) => {
    event.preventDefault();
    agregarLinea();
});
