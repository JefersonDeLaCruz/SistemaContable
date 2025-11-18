// Para cargar cuentas en el <select>
// async function cargarCuentas() {
//   try {
//     const response = await fetch('/api/cuentas'); // Endpoint del backend
//     const cuentas = await response.json();

//     // Selecciona correctamente el elemento <select> por su atributo name
//     const select = document.getElementById("selectCuenta");

//     // Limpia opciones previas (opcional)
//     select.innerHTML = '<option value="ALL">Todas las cuentas</option>';

//     // Crea las opciones dinámicamente
//     cuentas.forEach(cuenta => {
//       const option = document.createElement('option');
//       option.value = cuenta.idCuenta; 
//       option.textContent = `${cuenta.codigo} - ${cuenta.nombre}`;
//       select.appendChild(option);
//     });

//   } catch (error) {
//     console.error('Error cargando las cuentas:', error);
//   }
// }


// ===== Cargar periodos en el <select> =====
async function cargarPeriodos() {
  try {
    const response = await fetch('/api/periodos');
    const periodos = await response.json();

    const select = document.getElementById("selectPeriodo");
    if (!select) return;

    select.innerHTML = '<option value="">Seleccione un periodo</option>';

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
  cargarPeriodos();
});

// ===== Utilidades =====
const fmt = new Intl.NumberFormat('es-SV', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
const n = v => Number(v ?? 0);
const add2 = (a, b) => Math.round((n(a) + n(b)) * 100) / 100;

function sum2(arr, key) {
  return arr.reduce((acc, it) => add2(acc, n(it[key])), 0);
}

function buildMovRows(movs) {
  return movs.map(m => `
    <tr class="border-t border-gray-100">
      <td class="px-6 py-3">${m.fecha ?? ''}</td>
      <td class="px-6 py-3">${m.descripcion ?? '-'}</td>
      <td class="px-6 py-3 text-green-600 font-medium">${fmt.format(n(m.debito))}</td>
      <td class="px-6 py-3 text-red-600 font-medium">${fmt.format(n(m.credito))}</td>
    </tr>
  `).join('');
}

// Robusto: soporta JSON {codigo: "..."} o texto plano
async function obtenerCodigoPorId(idCuenta) {
  const response = await fetch(`/api/cuentas/codigo/${idCuenta}`);
  const ct = response.headers.get('content-type') || '';
  if (ct.includes('application/json')) {
    const json = await response.json();
    return json?.codigo ?? json?.data?.codigo ?? '';
  }
  return (await response.text()).trim();
}

// === Asíncrono: construye UNA tabla de cuenta ===
async function buildCuentaTable(cuenta) {
  const totalDeb = sum2(cuenta.movimientos, 'debito');
  const totalCred = sum2(cuenta.movimientos, 'credito');
  const saldo = Math.abs(totalDeb - totalCred);

  // Espera el código si no viene el nombre
  const codigo = await obtenerCodigoPorId(cuenta.idCuenta);

  return `
    <div class="mb-6">
      <div class="flex items-baseline gap-3 mb-2">
       <h3 class="text-lg font-semibold p-4">
     ${codigo || cuenta.idCuenta} - ${cuenta.nombreCuenta ?? 'Cuenta sin nombre'}
  </h3>

      </div>

      <div class="overflow-x-auto rounded-md border border-gray-100">
        <table class="table-auto border border-gray-300 w-full">
          <thead class="bg-primary text-primary-content sticky top-0">
            <tr>
              <th class="px-6 py-3 text-left">Fecha</th>
              <th class="px-6 py-3 text-left">Descripción</th>
              <th class="px-6 py-3 text-left">Débito</th>
              <th class="px-6 py-3 text-left">Crédito</th>
            </tr>
          </thead>
          <tbody class="text-sm">
            ${buildMovRows(cuenta.movimientos)}

            <tr class="font-bold bg-base-300 border-1 border-primary">
              <td class="px-6 py-3"></td>
              <td class="px-6 py-3 font-bold">SUMA</td>
              <td class="px-6 py-3 text-green-600 font-medium">${fmt.format(totalDeb)}</td>
              <td class="px-6 py-3 text-red-600 font-medium">${fmt.format(totalCred)}</td>
            </tr>

            <tr class="font-bold bg-base-300 border-1 border-primary">
              <td class="px-6 py-3"></td>
              <td class="px-6 py-3 font-bold">SALDO</td>
              ${
                totalDeb >= totalCred
                  ? `<td class="px-6 py-3 text-green-600 font-medium">${fmt.format(saldo)}</td><td class="px-6 py-3 text-red-600 font-medium"></td>`
                  : `<td class="px-6 py-3 text-green-600 font-medium"></td><td class="px-6 py-3 text-red-600 font-medium">${fmt.format(saldo)}</td>`
              }
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  `;
}

// === Asíncrono: renderiza TODO el libro mayor ===
async function renderLibroMayor(data, containerId = 'libroMayor') {
  const el = document.getElementById(containerId);
  if (!el) return;

  el.innerHTML = `<p class="text-sm text-gray-500">Cargando libro mayor…</p>`;

  const header = `
    <div class="mb-4">
      <h2 class="text-xl font-bold">Libro Mayor - Período ${data.periodoId}</h2>
      <p class="text-sm text-gray-600">
        Débitos del período: <span class="font-medium text-green-700">${fmt.format(n(data.totalDebitosPeriodo))}</span> ·
        Créditos del período: <span class="font-medium text-red-700">${fmt.format(n(data.totalCreditosPeriodo))}</span>
      </p>
    </div>
  `;

  // Espera todas las tablas (cada una puede hacer fetch de código)
  const tables = await Promise.all(
    (data.cuentas ?? []).map(buildCuentaTable)
  );

  el.innerHTML = header + tables.join('');
}

// === Handler del botón Filtrar ===
document.addEventListener("DOMContentLoaded", () => {
  const btnFiltrar = document.getElementById("filtrar");
  if (!btnFiltrar) return;

  btnFiltrar.addEventListener("click", async (event) => {
    event.preventDefault();

    const periodoId = document.getElementById("selectPeriodo")?.value || 1;

    btnFiltrar.disabled = true;
    btnFiltrar.dataset._oldText = btnFiltrar.textContent;
    btnFiltrar.textContent = 'Filtrando…';

    try {
      const data = await fetch(`/api/libromayor?periodoId=${periodoId}`).then(r => r.json());
      await renderLibroMayor(data, 'libroMayor'); // IMPORTANTE: espera al render asíncrono
    } catch (err) {
      console.error('Error cargando libro mayor:', err);
    } finally {
      btnFiltrar.disabled = false;
      btnFiltrar.textContent = btnFiltrar.dataset._oldText || 'Filtrar';
    }
  });
});
