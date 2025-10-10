// Para cargar cuentas en el <select>
async function cargarCuentas() {
  try {
    const response = await fetch('/api/cuentas'); // Endpoint del backend
    const cuentas = await response.json();

    // Selecciona correctamente el elemento <select> por su atributo name
    const select = document.getElementById("selectCuenta");

    // Limpia opciones previas (opcional)
    select.innerHTML = '<option value="">Todas las cuentas</option>';

    // Crea las opciones dinámicamente
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

// Para cargar periodos en el <select>
async function cargarPeriodos() {
  try {
    const response = await fetch('/api/periodos'); // Endpoint del backend
    const periodos = await response.json();

    // Selecciona correctamente el elemento <select> por su atributo name
    const select = document.getElementById("selectPeriodo")

    // Limpia opciones previas (opcional)
    select.innerHTML = '<option value="">Seleccione un periodo</option>';

    // Crea las opciones dinámicamente
    periodos.forEach(periodo => {
      const option = document.createElement('option');
      option.value = periodo.idPeriodo; 
      option.textContent = periodo.nombre;
      select.appendChild(option);
    });

  } catch (error) {
    console.error('Error cargando los periodos:', error);
  }
}

window.addEventListener("DOMContentLoaded", () => {
    cargarCuentas();
    cargarPeriodos();
});