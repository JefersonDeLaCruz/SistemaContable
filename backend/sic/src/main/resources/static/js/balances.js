async function cargarPeriodosBalance() {
  try {
    const res = await fetch('/api/periodos');
    const periodos = await res.json();
    const select = document.getElementById('selectPeriodo');
    select.innerHTML = '<option value="">Seleccione un periodo</option>';
    periodos.forEach(p => {
      const opt = document.createElement('option');
      opt.value = p.idPeriodo;
      opt.textContent = p.nombre;
      select.appendChild(opt);
    });
  } catch (e) {
    console.error('Error cargando periodos', e);
    mostrarToast('No se pudieron cargar los periodos', 'error');
  }
}

function fmtMoneda(v) { return new Intl.NumberFormat('es-SV', { style: 'currency', currency: 'USD' }).format(v || 0); }

function renderSeccion(titulo, data) {
  const filas = (data.cuentas || []).map(c => `
    <tr class="border-t border-base-200">
      <td class="px-4 py-2 text-sm tabular-nums">${c.codigo}</td>
      <td class="px-4 py-2">${c.nombre}</td>
      <td class="px-4 py-2 text-right font-mono">${fmtMoneda(c.saldo)}</td>
    </tr>
  `).join('');

  return `
    <div class="card bg-base-100 shadow">
      <div class="card-body">
        <h3 class="card-title">${titulo}</h3>
        <div class="overflow-x-auto">
          <table class="table w-full">
            <thead>
              <tr>
                <th class="w-28">Código</th>
                <th>Cuenta</th>
                <th class="w-40 text-right">Saldo</th>
              </tr>
            </thead>
            <tbody>
              ${filas || '<tr><td colspan="3" class="px-4 py-2 opacity-70">Sin datos</td></tr>'}
            </tbody>
            <tfoot>
              <tr class="font-bold border-t-2 border-primary">
                <td colspan="2" class="text-right">TOTAL ${titulo.toUpperCase()}:</td>
                <td class="text-right font-mono text-primary">${fmtMoneda(data.total)}</td>
              </tr>
            </tfoot>
          </table>
        </div>
      </div>
    </div>
  `;
}

function renderBalance(data) {
  const cont = document.getElementById('balanceContainer');
  const totalActivo = Number(data?.activo?.total || 0);
  const totalPasivo = Number(data?.pasivo?.total || 0);
  const totalCapital = Number(data?.capital?.total || 0);
  const totalPC = totalPasivo + totalCapital;
  const dif = Number(data?.diferencia || 0);
  const cuadra = !!data?.cuadra;
  const progressMax = Math.max(totalActivo, totalPC, 1);
  const pAct = Math.round((totalActivo / progressMax) * 100);
  const pPC = Math.round((totalPC / progressMax) * 100);

  const header = `
    <div class="alert ${cuadra ? 'alert-success' : 'alert-warning'}">
      <div>
        <span>Fecha de corte: <b>${data.fechaCorte}</b></span>
        <span class="ml-4">Estado: <span class="badge ${cuadra ? 'badge-success' : 'badge-error'}">${cuadra ? 'Cuadrado' : 'No cuadrado'}</span></span>
        ${!cuadra ? `<span class="ml-2">Diferencia: <b>${fmtMoneda(dif)}</b></span>` : ''}
      </div>
    </div>
  `;

  const stats = `
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-4">
      <div class="stat bg-base-200 rounded-xl">
        <div class="stat-title">Total Activo</div>
        <div class="stat-value text-info">${fmtMoneda(totalActivo)}</div>
      </div>
      <div class="stat bg-base-200 rounded-xl">
        <div class="stat-title">Total Pasivo</div>
        <div class="stat-value text-secondary">${fmtMoneda(totalPasivo)}</div>
      </div>
      <div class="stat bg-base-200 rounded-xl">
        <div class="stat-title">Total Capital</div>
        <div class="stat-value text-primary">${fmtMoneda(totalCapital)}</div>
      </div>
      <div class="stat bg-base-200 rounded-xl">
        <div class="stat-title">Pasivo + Capital</div>
        <div class="stat-value ${cuadra ? 'text-success' : 'text-error'}">${fmtMoneda(totalPC)}</div>
        <div class="stat-desc">Diferencia: ${fmtMoneda(dif)}</div>
      </div>
    </div>
  `;

  const compare = `
    <div class="card bg-base-100 shadow mb-6">
      <div class="card-body">
        <h3 class="card-title">Comparativo</h3>
        <div class="space-y-2">
          <div class="flex items-center justify-between text-sm"><span>Activo</span><span class="font-mono">${fmtMoneda(totalActivo)}</span></div>
          <progress class="progress progress-info w-full" value="${pAct}" max="100"></progress>
          <div class="flex items-center justify-between text-sm mt-3"><span>Pasivo + Capital</span><span class="font-mono">${fmtMoneda(totalPC)}</span></div>
          <progress class="progress ${cuadra ? 'progress-success' : 'progress-warning'} w-full" value="${pPC}" max="100"></progress>
        </div>
      </div>
    </div>
  `;

  cont.innerHTML = `
    ${header}
    ${stats}
    ${compare}
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
      ${renderSeccion('Activo', data.activo)}
      <div class="space-y-6">
        ${renderSeccion('Pasivo', data.pasivo)}
        ${renderSeccion('Capital', data.capital)}
      </div>
    </div>
  `;
}


// Utilidad - Obtiene saldo final de una cuenta
function getSaldo(data, nombreCuenta) {
  if (!data || !data.cuentas) return 0.00;

  const cuenta = data.cuentas.find(c => c.nombreCuenta === nombreCuenta);

  return cuenta ? Number(cuenta.saldoFinal) : 0.00;
}

// Estado de Resultados
async function renderEstadoResultados() {
  const cont = document.getElementById("balanceContainer");
  const periodoId = Number(document.getElementById("selectPeriodo").value);
  let data = null; // libro mayor
  let periodos = null; // periodos
  let periodoActivo = null; // periodo seleccionado para crear estado de resultados

  // Utilizamos la API de libro mayor para traer la informacion
  // del saldo de cada cuenta, esta informacion es necesaria para
  // crear el estado de resultados
  try {
    data = await fetch(`/api/libromayor?periodoId=${periodoId}`).then((r) =>
      r.json()
    );
    periodos = await fetch(`/api/periodos`).then((r) => r.json()); // Todos los periodos
    periodoActivo = periodos.find((p) => p.idPeriodo === periodoId) || null; // Filtramos
    console.log(data);
  } catch (err) {
    console.error("Error cargando datos para crear estado de resultados", err);
    return;
  }

  // DATOS NECESARIOS PARA HACER CALCULOS
  // Total Ingresos
  let ingresos = getSaldo(data, "INGRESOS");
  let ventas = getSaldo(data, "VENTAS");
  let descuentoSobreVentas = getSaldo(data, "DESCUENTOS SOBRE VENTAS");
  let otrosIngresos = getSaldo(data, "OTROS INGRESOS");
  let totalIngresos = ingresos + ventas  - descuentoSobreVentas + otrosIngresos;

  // Utilidad Bruta
  let costoDeVentas = getSaldo(data, "COSTO DE VENTAS");
  let costoMercaderiasVendidas = getSaldo(data, "COSTO DE MERCADERÍAS VENDIDAS");
  let utilidadBruta = totalIngresos - (costoDeVentas + costoMercaderiasVendidas)

  // Gastos de operacion
  let gastosDeOperacion = getSaldo(data, "GASTOS DE OPERACIÓN");

  // Gastos de Administracion
  let gastosDeAdministracion = getSaldo(data, "GASTOS DE ADMINISTRACIÓN");
  let sueldosSalarios = getSaldo(data, "SUELDOS Y SALARIOS");
  let serviciosBasicos = getSaldo(data, "SERVICIOS BÁSICOS");
  let papeleriaUtiles = getSaldo(data, "PAPELERÍA Y ÚTILES");
  let subtotalGastosAdministracion = gastosDeAdministracion + sueldosSalarios + serviciosBasicos + papeleriaUtiles;

   // Gastos de Venta
  let gastosDeVenta = getSaldo(data, "GASTOS DE VENTAS");
  let publicidadPropaganda = getSaldo(data, "PUBLICIDAD Y PROPAGANDA");
  let subtotalGastosVenta = gastosDeVenta + publicidadPropaganda;

  // Otros gastos operativos
  let otrosGastosOperativos = getSaldo(data, "OTROS GASTOS OPERATIVOS");
  let depreciacionDelPeriodo = getSaldo(data, "DEPRECIACIÓN DEL PERIODO");
  let subtotalOtrosGastosOperativos = otrosGastosOperativos + depreciacionDelPeriodo;

  let totalGastosDeOperacion = gastosDeOperacion + subtotalGastosAdministracion + subtotalGastosVenta + subtotalOtrosGastosOperativos;

  // Utilidad operativa
  let utilidadOperativa = utilidadBruta - totalGastosDeOperacion;

  // Gastos No Operativos
  let gastosNoOperativos = getSaldo(data, "GASTOS NO OPERATIVOS");
  let gastosFinancieros = getSaldo(data, "GASTOS FINANCIEROS");
  let totalGatosNoOperativos = gastosNoOperativos + gastosFinancieros;

  // UTILIDAD DEL PERIODO
  let utilidadDelPeriodo = utilidadOperativa - totalGatosNoOperativos;

  const estadoHTML = `
   <div class="w-full bg-blue-100 border border-blue-300 rounded-lg p-4 text-center shadow-sm">
      <p class="text-lg font-bold">
        ESTADO DE RESULTADOS
      </p>
      <p class="text-sm mt-1">
        <span class="font-semibold">Periodo:</span> ${
          periodoActivo.fechaInicio
        } - ${periodoActivo.fechaFin} <br>
         (Expresado en dólares de los Estados Unidos de América)
      </p>
   </div>

   <table class="min-w-full text-sm">
        <thead class="bg-sky-950 text-slate-200">
          <tr>
            <th class="px-4 py-3 text-left font-semibold uppercase tracking-wide text-xs">Código</th>
            <th class="px-4 py-3 text-left font-semibold uppercase tracking-wide text-xs">Cuenta</th>
            <th class="px-4 py-3 text-right font-semibold uppercase tracking-wide text-xs">Monto ($)</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-slate-800">

          <!-- I. INGRESOS -->
          <tr class="bg-emerald-400">
            <td colspan="3" class="px-4 py-2 font-semibold uppercase tracking-wide text-xs">
              I. INGRESOS
            </td>
          </tr>
          <tr>
            <td class="px-4 py-2">4</td>
            <td class="px-4 py-2">Ingresos</td>
            <td class="px-4 py-2 text-right font-mono">$ ${
              parseFloat(ingresos).toFixed(2)
            }</td>
          </tr>
          <tr>
            <td class="px-4 py-2">4.1</td>
            <td class="px-4 py-2">Ventas</td>
            <td class="px-4 py-2 text-right font-mono">$ ${
              parseFloat(ventas).toFixed(2)
            }</td>
          </tr>
          <tr>
            <td class="px-4 py-2">4.2</td>
            <td class="px-4 py-2">(-) Descuentos sobre ventas</td>
            <td class="px-4 py-2 text-right font-mono">$ ${
              parseFloat(descuentoSobreVentas).toFixed(2)
            }</td>
          </tr>
          <tr>
            <td class="px-4 py-2">4.3</td>
            <td class="px-4 py-2">Otros ingresos</td>
            <td class="px-4 py-2 text-right font-mono">$ ${
              parseFloat(otrosIngresos).toFixed(2)
            }</td>
          </tr>
          <tr class="bg-slate-900/60">
            <td class="px-4 py-2 text-xs font-semibold text-slate-400"></td>
            <td class="px-4 py-2 text-xs font-semibold">Total ingresos</td>
            <td class="px-4 py-2 text-right font-mono font-semibold text-emerald-300">$ ${
              parseFloat(totalIngresos).toFixed(2) 
            }</td>
          </tr>

          <!-- II. COSTO DE VENTAS -->
          <tr class="bg-emerald-400">
            <td colspan="3" class="px-4 py-2 font-semibold uppercase tracking-wide text-xs">
              II. COSTO DE VENTAS
            </td>
          </tr>
          <tr>
            <td class="px-4 py-2">5</td>
            <td class="px-4 py-2">Costo de Ventas</td>
            <td class="px-4 py-2 text-right font-mono">$ ${parseFloat(costoDeVentas).toFixed(2)}</td>
          </tr>
          <tr>
            <td class="px-4 py-2">5.1</td>
            <td class="px-4 py-2">Costo de mercaderías vendidas</td>
            <td class="px-4 py-2 text-right font-mono">$ ${parseFloat(costoMercaderiasVendidas).toFixed(2)}</td>
          </tr>
          <tr class="bg-slate-900/60">
            <td class="px-4 py-2 text-xs font-semibold text-slate-400"></td>
            <td class="px-4 py-2 text-xs font-semibold">UTILIDAD BRUTA</td>
            <td class="px-4 py-2 text-right font-mono font-semibold text-emerald-300">$ ${parseFloat(utilidadBruta).toFixed(2)}</td>
          </tr>

          <!-- III. GASTOS DE OPERACIÓN -->
          <tr class="bg-emerald-400">
            <td colspan="3" class="px-4 py-2 font-semibold uppercase tracking-wide text-xs">
              III. GASTOS DE OPERACIÓN
            </td>
          </tr>
          <tr>
            <td class="px-4 py-2">6</td>
            <td class="px-4 py-2">Gastos de Operacion</td>
            <td class="px-4 py-2 text-right font-mono">$ ${parseFloat(gastosDeOperacion).toFixed(2)}</td>
          </tr>
          <tr>
            <td class="px-4 py-2">6.1</td>
            <td class="px-4 py-2">Gastos de Administración</td>
            <td class="px-4 py-2 text-right font-mono">$ ${parseFloat(gastosDeAdministracion).toFixed(2)}</td>
          </tr>
          <tr>
            <td class="px-4 py-2">6.1.1</td>
            <td class="px-4 py-2">Sueldos y salarios</td>
            <td class="px-4 py-2 text-right font-mono">$ ${parseFloat(sueldosSalarios).toFixed(2)}</td>
          </tr>
          <tr>
            <td class="px-4 py-2">6.1.2</td>
            <td class="px-4 py-2">Servicios básicos</td>
            <td class="px-4 py-2 text-right font-mono">$ ${parseFloat(serviciosBasicos).toFixed(2)}</td>
          </tr>
          <tr>
            <td class="px-4 py-2">6.1.3</td>
            <td class="px-4 py-2">Papelería y útiles</td>
            <td class="px-4 py-2 text-right font-mono">$ ${parseFloat(papeleriaUtiles).toFixed(2)}</td>
          </tr>
          <tr class="bg-slate-900/60">
            <td class="px-4 py-2 text-xs font-semibold text-slate-400"></td>
            <td class="px-4 py-2 text-xs font-semibold">
              Subtotal gastos de administración
            </td>
            <td class="px-4 py-2 text-right font-mono font-semibold text-sky-200">$ ${parseFloat(subtotalGastosAdministracion).toFixed(2)}</td>
          </tr>

          <!-- b) Gastos de Ventas -->
          <tr>
            <td class="px-4 py-2">6.2</td>
            <td class="px-4 py-2">Gastos de Ventas</td>
            <td class="px-4 py-2 text-right font-mono">$ ${parseFloat(gastosDeVenta).toFixed(2)}</td>
          </tr>
          <tr>
            <td class="px-4 py-2">6.2.1</td>
            <td class="px-4 py-2">Publicidad y propaganda</td>
            <td class="px-4 py-2 text-right font-mono">$ ${parseFloat(publicidadPropaganda).toFixed(2)}</td>
          </tr>
          <tr class="bg-slate-900/60">
            <td class="px-4 py-2 text-xs font-semibold text-slate-400"></td>
            <td class="px-4 py-2 text-xs font-semibold">
              Subtotal gastos de ventas
            </td>
            <td class="px-4 py-2 text-right font-mono font-semibold text-sky-200">$ ${parseFloat(subtotalGastosVenta).toFixed(2)}</td>
          </tr>

          <!-- c) Otros gastos operativos -->
          <tr>
            <td class="px-4 py-2">6.3</td>
            <td class="px-4 py-2">Otros gastos operativos</td>
            <td class="px-4 py-2 text-right font-mono">$ ${parseFloat(otrosGastosOperativos).toFixed(2)}</td>
          </tr>
          <tr>
            <td class="px-4 py-2">6.3.1</td>
            <td class="px-4 py-2">Depreciación del período</td>
            <td class="px-4 py-2 text-right font-mono">$ ${parseFloat(depreciacionDelPeriodo).toFixed(2)}</td>
          </tr>
          <tr class="bg-slate-900/60">
            <td class="px-4 py-2 text-xs font-semibold text-slate-400"></td>
            <td class="px-4 py-2 text-xs font-semibold">
              Subtotal otros gastos operativos
            </td>
            <td class="px-4 py-2 text-right font-mono font-semibold text-sky-200">$ ${parseFloat(subtotalOtrosGastosOperativos).toFixed(2)}</td>
          </tr>
          <tr class="bg-slate-900/60">
            <td class="px-4 py-2 text-xs font-semibold text-slate-400"></td>
            <td class="px-4 py-2 text-xs font-semibold">
               Total gastos de operación
            </td>
            <td class="px-4 py-2 text-right font-mono font-semibold text-sky-200">$ ${parseFloat(totalGastosDeOperacion).toFixed(2)}</td>
          </tr>

          <!-- IV. UTILIDAD OPERATIVA -->
          <tr class="bg-slate-900/60">
            <td class="px-4 py-2 text-xs font-semibold text-slate-400"></td>
            <td class="px-4 py-2 text-xs font-semibold">UTILIDAD OPERATIVA</td>
            <td class="px-4 py-2 text-right font-mono font-semibold text-emerald-300">$ ${parseFloat(utilidadOperativa).toFixed(2)}</td>
          </tr>

          <!-- IV. GASTOS NO OPERATIVOS -->
          <tr class="bg-emerald-400">
            <td colspan="3" class="px-4 py-2 font-semibold uppercase tracking-wide text-xs">
              IV. GASTOS NO OPERATIVOS
            </td>
          </tr>
          <tr>
            <td class="px-4 py-2">7</td>
            <td class="px-4 py-2">Gastos No Operativos</td>
            <td class="px-4 py-2 text-right font-mono">$ ${parseFloat(gastosNoOperativos).toFixed(2)}</td>
          </tr>
          <tr>
            <td class="px-4 py-2">7.1</td>
            <td class="px-4 py-2">Gastos financieros</td>
            <td class="px-4 py-2 text-right font-mono">$ ${parseFloat(gastosFinancieros).toFixed(2)}</td>
          </tr>
          <tr class="bg-slate-900/60">
            <td class="px-4 py-2 text-xs font-semibold text-slate-400"></td>
            <td class="px-4 py-2 text-xs font-semibold">
              Total gastos no operativos
            </td>
            <td class="px-4 py-2 text-right font-mono font-semibold text-rose-200">$ ${parseFloat(totalGatosNoOperativos).toFixed(2)}</td>
          </tr>

          <!-- V. UTILIDAD NETA DEL PERIODO -->
          <tr class="bg-emerald-400">
            <td colspan="2" class="px-4 py-3 text-xs font-semibold uppercase">
              V. UTILIDAD NETA DEL PERIODO
            </td>
            <td class="px-4 py-3 text-right font-mono text-lg font-bold text-rose-200">
              $ ${parseFloat(utilidadDelPeriodo).toFixed(2)}
            </td>
          </tr>

        </tbody>
      </table>
  `;

  cont.innerHTML = estadoHTML;
}


window.addEventListener('DOMContentLoaded', () => {
  cargarPeriodosBalance();
  const btn = document.getElementById('btnCalcular');
  const selPeriodo = document.getElementById('selectPeriodo');
  const selTipo = document.getElementById('selectTipo');
  btn.addEventListener('click', async () => {
    const periodo = selPeriodo.value;
    const tipo = selTipo ? selTipo.value : 'general';
    if (!periodo) {
      mostrarToast('Seleccione un periodo primero', 'warning');
      return;
    }
    const prev = btn.innerHTML;
    btn.innerHTML = '<span class="loading loading-spinner"></span> Calculando...';
    btn.disabled = true;
    try {
      const url = tipo === 'estado' ? `/api/balances/estado?periodo=${encodeURIComponent(periodo)}` : 
                  tipo === 'flujos' ? `/api/balances/flujos-efectivo?periodo=${encodeURIComponent(periodo)}` :
                  tipo === 'patrimonio' ? `/api/balances/cambios-patrimonio?periodo=${encodeURIComponent(periodo)}` :
                  tipo === 'comprobacion' ? `/api/balances/comprobacion?periodo=${encodeURIComponent(periodo)}` :
                  `/api/balances/general?periodo=${encodeURIComponent(periodo)}`;
      const res = await fetch(url);
      if (!res.ok) throw new Error('Error al calcular');
      const data = await res.json();
      if (tipo === 'estado') renderEstadoResultados(data); 
      else if (tipo === 'comprobacion') renderBalanceComprobacion(data); 
      else if (tipo === 'flujos') renderFlujosEfectivo(data);
      else if (tipo === 'patrimonio') await renderCambiosPatrimonio();
      else renderBalance(data);
      mostrarToast('Reporte actualizado', 'success', 2500);
    } catch (e) {
      console.error(e);
      mostrarToast('No se pudo calcular', 'error');
    } finally {
      btn.innerHTML = prev;
      btn.disabled = false;
    }
  });
});

function renderBalanceComprobacion(data) {
  const cont = document.getElementById('balanceContainer');
  const filas = (data.cuentas || []).map(c => `
    <tr class="border-t border-base-200">
      <td class="px-4 py-2 text-sm tabular-nums">${c.codigo}</td>
      <td class="px-4 py-2">${c.nombre}</td>
      <td class="px-4 py-2 text-right font-mono">${fmtMoneda(c.saldoInicial)}</td>
      <td class="px-4 py-2 text-right font-mono text-success">${fmtMoneda(c.debitos)}</td>
      <td class="px-4 py-2 text-right font-mono text-error">${fmtMoneda(c.creditos)}</td>
      <td class="px-4 py-2 text-right font-mono">${fmtMoneda(c.saldoFinal)}</td>
    </tr>
  `).join('');

  cont.innerHTML = `
    <div class="alert">
      <span>Periodo: <b>${data.periodo.inicio}</b> a <b>${data.periodo.fin}</b></span>
    </div>
    <div class="card bg-base-100 shadow">
      <div class="card-body">
        <div class="overflow-x-auto">
          <table class="table w-full">
            <thead>
              <tr>
                <th class="w-28">Código</th>
                <th>Cuenta</th>
                <th class="w-32 text-right">Saldo Inicial</th>
                <th class="w-32 text-right">Débitos</th>
                <th class="w-32 text-right">Créditos</th>
                <th class="w-32 text-right">Saldo Final</th>
              </tr>
            </thead>
            <tbody>
              ${filas || '<tr><td colspan="6" class="px-4 py-2 opacity-70">Sin datos</td></tr>'}
            </tbody>
            <tfoot>
              <tr class="font-bold border-t-2 border-primary">
                <td colspan="2" class="text-right">Totales:</td>
                <td class="text-right font-mono">${fmtMoneda(data.totales.saldoInicial)}</td>
                <td class="text-right font-mono text-success">${fmtMoneda(data.totales.debitos)}</td>
                <td class="text-right font-mono text-error">${fmtMoneda(data.totales.creditos)}</td>
                <td class="text-right font-mono">${fmtMoneda(data.totales.saldoFinal)}</td>
              </tr>
            </tfoot>
          </table>
        </div>
      </div>
    </div>
  `;
}

function renderFlujosEfectivo(data) {
  const cont = document.getElementById('balanceContainer');
  const header = `<div class="alert ${data.cuadra ? 'alert-success' : 'alert-warning'}"><div><span>Período: <b>${data.periodo.nombre}</b> (${data.periodo.inicio} - ${data.periodo.fin})</span><span class="ml-4">Estado: <span class="badge ${data.cuadra ? 'badge-success' : 'badge-warning'}">${data.cuadra ? 'Cuadrado' : 'No cuadrado'}</span></span></div></div>`;
  const resumen = `<div class="grid grid-cols-1 md:grid-cols-2 gap-4 mb-6"><div class="stat bg-base-200 rounded-xl"><div class="stat-title">Efectivo Inicial</div><div class="stat-value text-info">${fmtMoneda(data.saldoInicial)}</div></div><div class="stat bg-base-200 rounded-xl"><div class="stat-title">Efectivo Final</div><div class="stat-value text-success">${fmtMoneda(data.saldoFinal)}</div></div><div class="stat bg-base-200 rounded-xl"><div class="stat-title">Aumento Neto en Efectivo</div><div class="stat-value ${data.aumentoNetoEfectivo >= 0 ? 'text-success' : 'text-error'}">${fmtMoneda(data.aumentoNetoEfectivo)}</div></div><div class="stat bg-base-200 rounded-xl"><div class="stat-title">Flujo Neto de Operación</div><div class="stat-value ${data.operacion.flujoNetoOperacion >= 0 ? 'text-primary' : 'text-warning'}">${fmtMoneda(data.operacion.flujoNetoOperacion)}</div></div></div>`;
  const renderDet = (items, colorClass = 'text-base-content') => { if (!items || items.length === 0) return ''; return items.map(item => `<tr class="border-t border-base-200"><td class="px-4 py-2 text-sm tabular-nums">${item.codigo}</td><td class="px-4 py-2">${item.nombre}</td><td class="px-4 py-2 text-right font-mono ${colorClass}">${fmtMoneda(item.monto)}</td></tr>`).join(''); };
  const secOp = `<div class="card bg-base-100 shadow mb-6"><div class="card-body"><h3 class="card-title">Actividades de Operación</h3><div class="overflow-x-auto"><table class="table w-full"><tbody><tr class="font-semibold bg-base-200"><td colspan="2" class="px-4 py-3">Utilidad Neta</td><td class="px-4 py-3 text-right font-mono">${fmtMoneda(data.operacion.utilidadNeta)}</td></tr>${data.operacion.ajustesNoEfectivo && data.operacion.ajustesNoEfectivo.length > 0 ? `<tr class="bg-base-200"><td colspan="3" class="px-4 py-2 font-semibold">Ajustes:</td></tr>${renderDet(data.operacion.ajustesNoEfectivo, 'text-primary')}<tr class="font-semibold"><td colspan="2" class="px-4 py-2 text-right">Total Ajustes:</td><td class="px-4 py-2 text-right font-mono text-primary">${fmtMoneda(data.operacion.totalAjustesNoEfectivo)}</td></tr>` : ''}${data.operacion.cambiosCapitalTrabajo && data.operacion.cambiosCapitalTrabajo.length > 0 ? `<tr class="bg-base-200"><td colspan="3" class="px-4 py-2 font-semibold">Cambios en Capital:</td></tr>${renderDet(data.operacion.cambiosCapitalTrabajo, 'text-secondary')}<tr class="font-semibold"><td colspan="2" class="px-4 py-2 text-right">Total Cambios:</td><td class="px-4 py-2 text-right font-mono text-secondary">${fmtMoneda(data.operacion.totalCambiosCapitalTrabajo)}</td></tr>` : ''}<tr class="font-bold bg-primary text-primary-content border-t-2"><td colspan="2" class="px-4 py-3 text-right">FLUJO NETO OPERACIÓN:</td><td class="px-4 py-3 text-right font-mono text-lg">${fmtMoneda(data.operacion.flujoNetoOperacion)}</td></tr></tbody></table></div></div></div>`;
  const secInv = `<div class="card bg-base-100 shadow mb-6"><div class="card-body"><h3 class="card-title">Actividades de Inversión</h3><div class="overflow-x-auto"><table class="table w-full"><tbody>${data.inversion.ventas && data.inversion.ventas.length > 0 ? `<tr class="bg-base-200"><td colspan="3" class="px-4 py-2 font-semibold">Entradas:</td></tr>${renderDet(data.inversion.ventas, 'text-success')}<tr class="font-semibold"><td colspan="2" class="px-4 py-2 text-right">Total Ventas:</td><td class="px-4 py-2 text-right font-mono text-success">${fmtMoneda(data.inversion.totalVentas)}</td></tr>` : '<tr><td colspan="3" class="px-4 py-2 opacity-70">Sin ventas</td></tr>'}${data.inversion.adquisiciones && data.inversion.adquisiciones.length > 0 ? `<tr class="bg-base-200"><td colspan="3" class="px-4 py-2 font-semibold">Salidas:</td></tr>${renderDet(data.inversion.adquisiciones, 'text-error')}<tr class="font-semibold"><td colspan="2" class="px-4 py-2 text-right">Total Compras:</td><td class="px-4 py-2 text-right font-mono text-error">${fmtMoneda(data.inversion.totalAdquisiciones)}</td></tr>` : '<tr><td colspan="3" class="px-4 py-2 opacity-70">Sin compras</td></tr>'}<tr class="font-bold bg-secondary text-secondary-content border-t-2"><td colspan="2" class="px-4 py-3 text-right">FLUJO NETO INVERSIÓN:</td><td class="px-4 py-3 text-right font-mono text-lg">${fmtMoneda(data.inversion.flujoNetoInversion)}</td></tr></tbody></table></div></div></div>`;
  const secFin = `<div class="card bg-base-100 shadow mb-6"><div class="card-body"><h3 class="card-title">Actividades de Financiamiento</h3><div class="overflow-x-auto"><table class="table w-full"><tbody>${data.financiamiento.entradas && data.financiamiento.entradas.length > 0 ? `<tr class="bg-base-200"><td colspan="3" class="px-4 py-2 font-semibold">Entradas:</td></tr>${renderDet(data.financiamiento.entradas, 'text-success')}<tr class="font-semibold"><td colspan="2" class="px-4 py-2 text-right">Total Entradas:</td><td class="px-4 py-2 text-right font-mono text-success">${fmtMoneda(data.financiamiento.totalEntradas)}</td></tr>` : '<tr><td colspan="3" class="px-4 py-2 opacity-70">Sin entradas</td></tr>'}${data.financiamiento.salidas && data.financiamiento.salidas.length > 0 ? `<tr class="bg-base-200"><td colspan="3" class="px-4 py-2 font-semibold">Salidas:</td></tr>${renderDet(data.financiamiento.salidas, 'text-error')}<tr class="font-semibold"><td colspan="2" class="px-4 py-2 text-right">Total Salidas:</td><td class="px-4 py-2 text-right font-mono text-error">${fmtMoneda(data.financiamiento.totalSalidas)}</td></tr>` : '<tr><td colspan="3" class="px-4 py-2 opacity-70">Sin salidas</td></tr>'}<tr class="font-bold bg-accent text-accent-content border-t-2"><td colspan="2" class="px-4 py-3 text-right">FLUJO NETO FINANCIAMIENTO:</td><td class="px-4 py-3 text-right font-mono text-lg">${fmtMoneda(data.financiamiento.flujoNetoFinanciamiento)}</td></tr></tbody></table></div></div></div>`;
  const totales = `<div class="card bg-base-100 shadow"><div class="card-body"><h3 class="card-title">Resumen</h3><div class="overflow-x-auto"><table class="table w-full"><tbody><tr class="border-t border-base-200"><td class="px-4 py-2">Flujo Operación</td><td class="px-4 py-2 text-right font-mono">${fmtMoneda(data.operacion.flujoNetoOperacion)}</td></tr><tr class="border-t border-base-200"><td class="px-4 py-2">Flujo Inversión</td><td class="px-4 py-2 text-right font-mono">${fmtMoneda(data.inversion.flujoNetoInversion)}</td></tr><tr class="border-t border-base-200"><td class="px-4 py-2">Flujo Financiamiento</td><td class="px-4 py-2 text-right font-mono">${fmtMoneda(data.financiamiento.flujoNetoFinanciamiento)}</td></tr><tr class="font-bold bg-info text-info-content border-t-2"><td class="px-4 py-3">Aumento Neto</td><td class="px-4 py-3 text-right font-mono text-lg">${fmtMoneda(data.aumentoNetoEfectivo)}</td></tr><tr class="border-t border-base-200"><td class="px-4 py-2">Efectivo Inicial</td><td class="px-4 py-2 text-right font-mono">${fmtMoneda(data.saldoInicial)}</td></tr><tr class="font-bold bg-success text-success-content border-t-2"><td class="px-4 py-3">Efectivo Final</td><td class="px-4 py-3 text-right font-mono text-lg">${fmtMoneda(data.saldoFinal)}</td></tr></tbody></table></div></div></div>`;
  cont.innerHTML = `${header}${resumen}${secOp}${secInv}${secFin}${totales}`;
}


// Estado de Cambios en el Patrimonio
async function renderCambiosPatrimonio() {
 
}


