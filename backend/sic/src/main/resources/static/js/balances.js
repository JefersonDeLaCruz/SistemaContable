﻿async function cargarPeriodosBalance() {
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
}function renderEstadoResultados(data) {
  const cont = document.getElementById('balanceContainer');
  const header = `
    <div class="alert">
      <span>Periodo: <b>${data.periodo.inicio}</b> a <b>${data.periodo.fin}</b></span>
    </div>
  `;

  const renderERSeccion = (titulo, dataSec) => {
    const filas = (dataSec.cuentas || []).map(c => `
      <tr class="border-t border-base-200">
        <td class="px-4 py-2 text-sm tabular-nums">${c.codigo}</td>
        <td class="px-4 py-2">${c.nombre}</td>
        <td class="px-4 py-2 text-right font-mono">${fmtMoneda(c.monto)}</td>
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
                  <th class="w-40 text-right">Monto</th>
                </tr>
              </thead>
              <tbody>
                ${filas || '<tr><td colspan="3" class="px-4 py-2 opacity-70">Sin datos</td></tr>'}
              </tbody>
              <tfoot>
                <tr class="font-bold border-t-2 border-primary">
                  <td colspan="2" class="text-right">TOTAL ${titulo.toUpperCase()}:</td>
                  <td class="text-right font-mono text-primary">${fmtMoneda(dataSec.total)}</td>
                </tr>
              </tfoot>
            </table>
          </div>
        </div>
      </div>
    `;
  };

  cont.innerHTML = `
    ${header}
    <div class="space-y-6">
      ${renderERSeccion('Ingresos', data.ingresos)}
      ${renderERSeccion('Gastos', data.gastos)}
      <div class="card bg-base-100 shadow">
        <div class="card-body">
          <div class="text-xl">Resultado del período: <b>${data.resultado}</b></div>
          <div class="text-3xl font-bold">${fmtMoneda(data.utilidadNeta)}</div>
        </div>
      </div>
    </div>
  `;
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
      const url = tipo === 'estado' ? `/api/balances/estado?periodo=${encodeURIComponent(periodo)}` : `/api/balances/general?periodo=${encodeURIComponent(periodo)}`;
      const res = await fetch(url);
      if (!res.ok) throw new Error('Error al calcular');
      const data = await res.json();
      if (tipo === 'estado') renderEstadoResultados(data); else if (tipo === 'comprobacion') renderBalanceComprobacion(data); else renderBalance(data);
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

