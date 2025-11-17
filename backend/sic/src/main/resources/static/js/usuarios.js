// Gestión de Usuarios
document.addEventListener('DOMContentLoaded', () => {
  let usuarios = [];
  let usuariosFiltrados = [];
  let editando = false;

  // DOM
  const tablaUsuarios = document.getElementById('tablaUsuarios');
  const buscarUsuario = document.getElementById('buscarUsuario');
  const filtroRol = document.getElementById('filtroRol');
  const filtroEstado = document.getElementById('filtroEstado');
  const btnNuevoUsuario = document.getElementById('btnNuevoUsuario');
  const modalUsuario = document.getElementById('modalUsuario');
  const modalEliminarUsuario = document.getElementById('modalEliminarUsuario');
  const formUsuario = document.getElementById('formUsuario');
  const tituloModalUsuario = document.getElementById('tituloModalUsuario');
  const usuarioId = document.getElementById('usuarioId');
  const username = document.getElementById('username');
  const email = document.getElementById('email');
  const role = document.getElementById('role');
  const password = document.getElementById('password');
  const active = document.getElementById('active');
  const usuarioEliminarId = document.getElementById('usuarioEliminarId');
  const usuarioEliminarNombre = document.getElementById('usuarioEliminarNombre');
  const btnConfirmarEliminarUsuario = document.getElementById('btnConfirmarEliminarUsuario');

  const totalUsuarios = document.getElementById('totalUsuarios');
  const totalActivos = document.getElementById('totalActivos');
  const totalAdmins = document.getElementById('totalAdmins');

  // expose for inline handlers
  window.editarUsuario = (id) => abrirModalEditar(id);
  window.eliminarUsuario = (id) => abrirModalEliminar(id);
  window.toggleActivo = (id, checked) => actualizarEstado(id, checked);

  // Listeners
  btnNuevoUsuario.addEventListener('click', abrirModalNuevo);
  formUsuario.addEventListener('submit', guardarUsuario);
  buscarUsuario.addEventListener('input', aplicarFiltros);
  filtroRol.addEventListener('change', aplicarFiltros);
  filtroEstado.addEventListener('change', aplicarFiltros);
  btnConfirmarEliminarUsuario.addEventListener('click', confirmarEliminar);

  cargarUsuarios();

  function cargarUsuarios() {
    fetch('/api/usuarios')
      .then(r => {
        if (!r.ok) throw new Error('HTTP ' + r.status);
        return r.json();
      })
      .then(data => {
        usuarios = data;
        usuariosFiltrados = [...usuarios];
        renderizarTabla();
        actualizarEstadisticas();
      })
      .catch(err => {
        console.error('Error cargando usuarios', err);
        mostrarToast('Error al cargar usuarios', 'error');
        tablaUsuarios.innerHTML = `<tr><td colspan="6" class="text-center py-8 text-error">No fue posible cargar usuarios.</td></tr>`;
      });
  }

  function renderizarTabla() {
    if (!usuariosFiltrados.length) {
      tablaUsuarios.innerHTML = `
        <tr>
          <td colspan="6" class="text-center py-8">
            <div class="flex flex-col items-center gap-2">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-12 w-12 text-base-content/50" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5.121 17.804A13.937 13.937 0 0112 16c2.5 0 4.847.655 6.879 1.804M15 10a3 3 0 11-6 0 3 3 0 016 0z" />
              </svg>
              <p class="text-base-content/70">No se encontraron usuarios</p>
            </div>
          </td>
        </tr>`;
      return;
    }

    tablaUsuarios.innerHTML = usuariosFiltrados.map(u => {
      const badgeRol = u.role === 'ADMIN' ? 'badge-info' : (u.role === 'AUDITOR' ? 'badge-warning' : 'badge-secondary');
      const estado = u.active ? 'checked' : '';
      return `
        <tr class="hover">
          <td class="font-semibold">${escapeHtml(u.username)}</td>
          <td>${escapeHtml(u.email)}</td>
          <td><span class="badge ${badgeRol}">${u.role}</span></td>
          <td class="text-center">
            <input type="checkbox" class="toggle toggle-primary" ${estado} onchange="toggleActivo(${u.id}, this.checked)" />
          </td>
          <td class="text-sm text-base-content/70">${formatDate(u.createdAt)}</td>
          <td>
            <div class="flex gap-2 justify-center">
              <button class="btn btn-sm btn-ghost btn-square" title="Editar" onclick="editarUsuario(${u.id})">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5" />
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M18.5 2.5a2.121 2.121 0 113 3L12 15l-4 1 1-4 9.5-9.5z" />
                </svg>
              </button>
              <button class="btn btn-sm btn-ghost btn-square text-error" title="Eliminar" onclick="eliminarUsuario(${u.id})">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6M1 7h22M9 3h6a2 2 0 012 2v2H7V5a2 2 0 012-2z" />
                </svg>
              </button>
            </div>
          </td>
        </tr>`;
    }).join('');
  }

  function actualizarEstadisticas() {
    totalUsuarios.textContent = usuarios.length;
    totalActivos.textContent = usuarios.filter(u => u.active).length;
    totalAdmins.textContent = usuarios.filter(u => u.role === 'ADMIN').length;
  }

  function abrirModalNuevo() {
    editando = false;
    tituloModalUsuario.textContent = 'Nuevo Usuario';
    usuarioId.value = '';
    username.value = '';
    email.value = '';
    role.value = '';
    password.value = '';
    active.checked = true;
    modalUsuario.showModal();
  }

  function abrirModalEditar(id) {
    const u = usuarios.find(x => x.id === id);
    if (!u) return;
    editando = true;
    tituloModalUsuario.textContent = 'Editar Usuario';
    usuarioId.value = u.id;
    username.value = u.username;
    email.value = u.email;
    role.value = u.role;
    password.value = '';
    active.checked = !!u.active;
    modalUsuario.showModal();
  }

  function abrirModalEliminar(id) {
    const u = usuarios.find(x => x.id === id);
    if (!u) return;
    usuarioEliminarId.value = id;
    usuarioEliminarNombre.textContent = u.username;
    modalEliminarUsuario.showModal();
  }

  function guardarUsuario(e) {
    e.preventDefault();
    if (!username.value.trim() || !email.value.trim() || !role.value.trim()) {
      mostrarToast('Completa los campos obligatorios', 'warning');
      return;
    }

    const payload = {
      username: username.value.trim(),
      email: email.value.trim(),
      role: role.value,
      active: !!active.checked
    };
    if (password.value && password.value.trim().length >= 6) {
      payload.password = password.value.trim();
    }

    if (!editando) {
      if (!payload.password) {
        mostrarToast('La contraseña es obligatoria para crear', 'warning');
        return;
      }
      fetch('/api/usuarios', {
        method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(payload)
      })
        .then(r => r.ok ? r.json() : r.text().then(t => Promise.reject({ status: r.status, msg: t })))
        .then(nuevo => {
          usuarios.push(nuevo);
          aplicarFiltros();
          actualizarEstadisticas();
          modalUsuario.close();
          mostrarToast('Usuario creado', 'success');
        })
        .catch(err => mostrarError(err));
    } else {
      const id = Number(usuarioId.value);
      fetch(`/api/usuarios/${id}`, {
        method: 'PUT', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(payload)
      })
        .then(r => r.ok ? r.json() : r.text().then(t => Promise.reject({ status: r.status, msg: t })))
        .then(resp => {
          const actualizado = resp.usuario || resp;
          const idx = usuarios.findIndex(u => u.id === id);
          if (idx >= 0) usuarios[idx] = actualizado;
          aplicarFiltros();
          actualizarEstadisticas();
          modalUsuario.close();
          mostrarToast('Usuario actualizado', 'success');
          if (resp.mustLogout) {
            mostrarToast('Se actualizaron tus permisos. Cerrando sesión...', 'warning', 2000);
            setTimeout(() => { window.location.href = '/logout'; }, 1500);
          }
        })
        .catch(err => mostrarError(err));
    }
  }

  function confirmarEliminar() {
    const id = Number(usuarioEliminarId.value);
    fetch(`/api/usuarios/${id}`, { method: 'DELETE' })
      .then(r => r.ok ? r.json() : r.text().then(t => Promise.reject({ status: r.status, msg: t })))
      .then(resp => {
        usuarios = usuarios.filter(u => u.id !== id);
        aplicarFiltros();
        actualizarEstadisticas();
        modalEliminarUsuario.close();
        mostrarToast('Usuario eliminado', 'success');
        if (resp.mustLogout) {
          mostrarToast('Tu usuario fue eliminado. Cerrando sesión...', 'warning', 2000);
          setTimeout(() => { window.location.href = '/logout'; }, 1500);
        }
      })
      .catch(err => mostrarError(err));
  }

  function actualizarEstado(id, estado) {
    const u = usuarios.find(x => x.id === id);
    if (!u) return;

    // Pre-chequeo de restricciones en cliente para mejor UX
    if (!estado) { // intento de desactivación
      const isSelf = (typeof window.currentUserId !== 'undefined') && Number(window.currentUserId) === id;
      if (u.role === 'ADMIN' || (isSelf && window.currentUserRole === 'ADMIN')) {
        mostrarToast('No puedes desactivar a usuarios ADMIN ni a ti mismo.', 'warning');
        // revertir UI
        cargarUsuarios();
        return;
      }
    }
    const payload = { username: u.username, email: u.email, role: u.role, active: !!estado };
    fetch(`/api/usuarios/${id}`, {
      method: 'PUT', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(payload)
    })
      .then(r => r.ok ? r.json() : r.text().then(t => Promise.reject({ status: r.status, msg: t })))
      .then(resp => {
        const actualizado = resp.usuario || resp;
        const idx = usuarios.findIndex(x => x.id === id);
        if (idx >= 0) usuarios[idx] = actualizado;
        aplicarFiltros();
        actualizarEstadisticas();
        if (resp.mustLogout) {
          mostrarToast('Tu usuario fue desactivado. Cerrando sesión...', 'warning', 2000);
          setTimeout(() => { window.location.href = '/logout'; }, 1500);
        }
      })
      .catch(err => {
        mostrarError(err);
        // revertir UI on fail
        cargarUsuarios();
      });
  }

  function aplicarFiltros() {
    const q = (buscarUsuario.value || '').toLowerCase();
    const fr = filtroRol.value;
    const fe = filtroEstado.value; // 'activo' | 'inactivo' | ''
    usuariosFiltrados = usuarios.filter(u => {
      const matchQ = !q || u.username.toLowerCase().includes(q) || u.email.toLowerCase().includes(q);
      const matchR = !fr || u.role === fr;
      const matchE = !fe || (fe === 'activo' ? u.active : !u.active);
      return matchQ && matchR && matchE;
    });
    renderizarTabla();
  }

  function mostrarError(err) {
    const msg = err && err.msg ? err.msg : 'Ocurrió un error';
    mostrarToast(msg, 'error');
    console.error(err);
  }

  function escapeHtml(str) {
    return String(str).replace(/[&<>"]/g, s => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;' }[s]));
  }

  function formatDate(iso) {
    if (!iso) return '-';
    try {
      const [fecha] = iso.split('T');
      const [y, m, d] = fecha.split('-');
      const meses = ['Ene', 'Feb', 'Mar', 'Abr', 'May', 'Jun', 'Jul', 'Ago', 'Sep', 'Oct', 'Nov', 'Dic'];
      return `${d} ${meses[parseInt(m) - 1]} ${y}`;
    } catch {
      return iso;
    }
  }
});
