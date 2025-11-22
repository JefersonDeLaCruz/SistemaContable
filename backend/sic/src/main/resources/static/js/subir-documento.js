async function cargarTiposDocumento() {
  const select = document.getElementById("tipoDocumento");

  try {
    const response = await fetch("/api/tipos-documento");
    if (!response.ok) {
      throw new Error("Error al obtener los tipos de documento");
    }

    const tipos = await response.json();

    tipos.forEach((td) => {
      const option = document.createElement("option");
      option.value = td.idTipo; // ID del tipo de documento
      option.textContent = td.nombre; // Nombre que se muestra
      select.appendChild(option);
    });
  } catch (error) {
    console.error("Error cargando tipos de documento:", error);
  }
}

// Para cuando se redireccione desde crear partida
const urlParams = new URLSearchParams(window.location.search);
const partidaId = urlParams.get("partidaId");
function mensajePartidaCreada() {
   
    if (partidaId) {
       mostrarToast("Partida creada exitosamente. Id de partida-> " + partidaId, "success");
    }
}
document.addEventListener("DOMContentLoaded", mensajePartidaCreada);


// Llamar automáticamente al cargar la página
document.addEventListener("DOMContentLoaded", cargarTiposDocumento);

async function cargarPartidas() {
  const select = document.getElementById("idPartida");

  // Limpiar select manteniendo la opción inicial
  select.innerHTML = `
        <option value="" disabled selected>ID PARTIDA</option>
    `;

  try {
    // === Obtener periodos ===
    const responsePeriodos = await fetch("/api/periodos");
    if (!responsePeriodos.ok) throw new Error("Error obteniendo periodos");

    const periodos = await responsePeriodos.json();

    // Filtrar solo los periodos abiertos (cerrado = false)
    const periodosActivos = periodos
      .filter((p) => p.cerrado === false)
      .map((p) => p.idPeriodo); // obtenemos solo sus IDs

    console.log("Periodos activos:", periodosActivos);

    // === Obtener partidas ===
    const responsePartidas = await fetch("/api/partidas");
    if (!responsePartidas.ok) throw new Error("Error obteniendo partidas");

    const partidas = await responsePartidas.json();

    // Filtrar solo las partidas cuyo idPeriodo esté en los activos
    const partidasFiltradas = partidas.filter((p) =>
      periodosActivos.includes(Number(p.idPeriodo))
    );

    // Insertar al select
    partidasFiltradas.forEach((p) => {
      const option = document.createElement("option");
      option.value = p.id;
      option.textContent = `${p.id} - Descripción: ${p.descripcion}`;
      select.appendChild(option);
      if(p.id == partidaId){
        select.value = p.id;
      }
    });
  } catch (error) {
    console.error("Error cargando partidas con filtro:", error);
  }
}

// Ejecutar al cargar la página
document.addEventListener("DOMContentLoaded", cargarPartidas);

const btnAgregar = document.getElementById("btnAgregar");
const fileInput = document.getElementById("fileInput");
const listaPDF = document.getElementById("listaPDF");

const modal = document.getElementById("modal");
const visorModal = document.getElementById("visorModal");
const cerrarModal = document.getElementById("cerrarModal");

let archivos = [];

// --- Mostrar/Ocultar botón agregar ---
function actualizarBotonAgregar() {
  if (archivos.length >= 1) {
    btnAgregar.disabled = true;
    btnAgregar.classList.add("opacity-200", "cursor-not-allowed");
  } else {
    btnAgregar.disabled = false;
    btnAgregar.classList.remove("opacity-50", "cursor-not-allowed");
  }
}

// Abrir selector de archivo
btnAgregar.addEventListener("click", () => {
  fileInput.click();
});

// Cuando se elige un PDF
fileInput.addEventListener("change", () => {
  const file = fileInput.files[0];
  if (!file) return;

  if (file.type !== "application/pdf") {
    mostrarToast("Debe seleccionar un archivo PDF.", "warning");

    return;
  }

  const url = URL.createObjectURL(file);

  // Solo permitir 1 PDF
  archivos = [{ nombre: file.name, url }];

  actualizarLista();
  actualizarBotonAgregar(); // Ocultar botón al agregar
});

// Actualiza la lista de PDFs
function actualizarLista() {
  listaPDF.innerHTML = "";

  archivos.forEach((pdf, index) => {
    const item = document.createElement("div");
    item.className =
      "flex justify-between items-center p-4 rounded-lg border-2 border-gray-300 bg-gray-100 rounded-xl shadow";

    item.innerHTML = `
      <span class="text-gray-800 font-medium">${pdf.nombre}</span>

      <div class="space-x-2">
        <button onclick="verPDF(${index})" 
            class="btn btn-sm btn-ghost btn-square text-green-600" 
            title="Ver">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" 
                fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                        d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                        d="M2.458 12C3.732 7.943 7.523 5 12 5c4.477 0 8.268 2.943 9.542 7-1.274 4.057-5.065 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
            </svg>
        </button>

        <button onclick="eliminarPDF(${index})" 
            class="btn btn-sm btn-ghost btn-square text-error" 
            title="Eliminar">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                    d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
            </svg>
        </button>
      </div>
    `;

    listaPDF.appendChild(item);
  });
}

// Mostrar modal con PDF
function verPDF(indice) {
  visorModal.src = archivos[indice].url;
  modal.classList.remove("hidden");
  modal.classList.add("flex");
}

// Eliminar PDF
function eliminarPDF(indice) {
  archivos.splice(indice, 1);
  actualizarLista();
  actualizarBotonAgregar(); // Mostrar botón al eliminar
  fileInput.value = "";
}

// Cerrar modal
cerrarModal.addEventListener("click", () => {
  modal.classList.add("hidden");
  modal.classList.remove("flex");
  visorModal.src = "";
});

// Cerrar modal haciendo clic afuera
modal.addEventListener("click", (e) => {
  if (e.target === modal) {
    modal.classList.add("hidden");
    modal.classList.remove("flex");
    visorModal.src = "";
  }
});

// Guardar en backend
btnGuardar.addEventListener("click", async () => {
  const idTipo = document.getElementById("tipoDocumento").value;
  const idPartida = document.getElementById("idPartida").value;
  const userId = sessionStorage.getItem("userId");

  const fileInput = document.getElementById("fileInput");
  const archivoSeleccionado = fileInput.files[0];

  const selectTipo = document.getElementById("tipoDocumento");
  const selectPartida = document.getElementById("idPartida");
  const listaPDF = document.getElementById("listaPDF"); // si tienes este contenedor en el HTML

  if (!idTipo) {
    mostrarToast("Selecciona el TIPO DE DOCUMENTO.", "warning");
    return;
  }

  if (!idPartida) {
    mostrarToast("Selecciona el ID PARTIDA.", "warning");
    return;
  }

  if (!archivoSeleccionado) {
    mostrarToast("Selecciona un PDF.", "warning");
    return;
  }

  if (!userId) {
    mostrarToast(
      "No se encontró el usuario en sesión (sessionStorage.userId).",
      "warning"
    );
    return;
  }

  // Esta es la información EXACTA que el backend espera
  const data = {
    idTipo: parseInt(idTipo),
    idPartida: parseInt(idPartida),
    subidoPor: parseInt(userId),
  };

  const formData = new FormData();

  // Parte 1: JSON con los datos
  formData.append(
    "data",
    new Blob([JSON.stringify(data)], { type: "application/json" })
  );

  // Parte 2: archivo PDF
  formData.append("archivo", archivoSeleccionado);

  try {
    btnGuardar.disabled = true;
    btnGuardar.textContent = "GUARDANDO...";

    const resp = await fetch("/api/documentos", {
      method: "POST",
      body: formData,
    });

    if (!resp.ok) {
      const text = await resp.text();
      console.error("Error respuesta servidor:", text);
      throw new Error("Error al guardar el documento");
    }

    const json = await resp.json();
    console.log("Documento guardado:", json);

    mostrarToast("Documento guardado correctamente.", "success");
    await renderizarTabla();

    // Reset (NO reasignamos la const, solo limpiamos los campos)
    btnAgregar.disabled = false;
    btnAgregar.classList.remove("opacity-50", "cursor-not-allowed");
    fileInput.value = "";
    if (listaPDF) listaPDF.innerHTML = "";
    if (selectTipo) selectTipo.value = "";
    if (selectPartida) selectPartida.value = "";
  } catch (error) {
    console.error(error);
    mostrarToast("Ocurrió un error al guardar el documento.", "warning");
  } finally {
    btnGuardar.disabled = false;
    btnGuardar.textContent = "GUARDAR";
  }
});

async function renderizarTabla() {
  const tbody = document.getElementById("tablaDocumentos");

  // Mostrar loading
  tbody.innerHTML = `
            <tr>
                <td colspan="6" class="text-center py-8">
                    <span class="loading loading-spinner loading-lg"></span>
                    <p class="mt-2">Cargando documentos...</p>
                </td>
            </tr>
        `;

  try {
    // Pedimos todo en paralelo
    const [respDocs, respTipos, respUsuarios] = await Promise.all([
      fetch("/api/documentos"),
      fetch("/api/tipos-documento"),
      fetch("/api/usuarios"),
    ]);

    if (!respDocs.ok || !respTipos.ok || !respUsuarios.ok) {
      throw new Error("Error al cargar datos");
    }

    const documentos = await respDocs.json(); // [{ id, fechaSubida, idTipo, idPartida, subidoPor }, ...]
    const tipos = await respTipos.json(); // [{ id, nombre }, ...]
    const usuarios = await respUsuarios.json(); // [{ id, username }, ...]

    // Mapear tipos y usuarios por id para acceso rápido
    const tiposMap = {};
    tipos.forEach((t) => {
      const id = t.id ?? t.idTipo ?? t.id_tipo; // por si tu campo se llama distinto
      if (id != null) {
        tiposMap[id] = t.nombre;
      }
    });

    const usuariosMap = {};
    usuarios.forEach((u) => {
      const id = u.id ?? u.idUsuario ?? u.id_usuario;
      if (id != null) {
        usuariosMap[id] = u.username ?? u.userName ?? u.nombreUsuario;
      }
    });

    // Si no hay documentos
    if (!documentos || documentos.length === 0) {
      tbody.innerHTML = `
                    <tr>
                        <td colspan="6" class="text-center py-6">
                            No hay documentos registrados.
                        </td>
                    </tr>
                `;
      return;
    }

    // Limpiamos tbody
    tbody.innerHTML = "";

    documentos.forEach((doc, index) => {
      const docId = doc.id ?? doc.idDocumento ?? doc.id_documento ?? index;
      const fechaSubida = doc.fechaSubida ?? doc.fecha_subida ?? "";
      const idPartida = doc.idPartida ?? doc.id_partida ?? "";
      const idTipo = doc.idTipo ?? doc.id_tipo ?? "";
      const idUsuario = doc.subidoPor ?? doc.idUsuario ?? doc.id_usuario ?? "";

      // RUTA
      const ruta = "/uploads/" + doc.archivo;

      const nombreTipo = tiposMap[idTipo] ?? `Tipo #${idTipo}`;
      const nombreUsuario = usuariosMap[idUsuario] ?? `Usuario #${idUsuario}`;

      const tr = document.createElement("tr");

      tr.innerHTML = `
                    <td>${fechaSubida}</td>
                    <td>${nombreTipo}</td>
                    <td>${doc.archivo}</td>
                    <td>${idPartida}</td>
                    <td class="text-center">${nombreUsuario}</td>
                    <td class="text-center">
                        <div class="flex items-center justify-center gap-2">
                            <!-- Botón Ver -->
                            <button 
                                class="btn btn-sm btn-ghost btn-square text-info"
                                title="Ver documento"
                                onclick="verPDFTabla('${ruta}')">
                                <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none"
                                    viewBox="0 0 24 24" stroke="currentColor">
                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                        d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                        d="M2.458 12C3.732 7.943 7.523 5 12 5c4.477 0 8.268 2.943 9.542 7-1.274 4.057-5.065 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                                </svg>
                            </button>

                            <!-- Botón Eliminar -->
                            <button 
                                class="btn btn-sm btn-ghost btn-square text-error"
                                title="Eliminar documento"
                                onclick="eliminarDocumento(${docId})">
                                <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none"
                                    viewBox="0 0 24 24" stroke="currentColor">
                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                        d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6
                                           m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                                </svg>
                            </button>
                        </div>
                    </td>
                `;

      tbody.appendChild(tr);
    });
  } catch (error) {
    console.error(error);
    tbody.innerHTML = `
                <tr>
                    <td colspan="6" class="text-center py-6 text-error">
                        Ocurrió un error al cargar los documentos.
                    </td>
                </tr>
            `;
  }
}

// Mostrar modal con PDF
function verPDFTabla(ruta) {
  visorModal.src = ruta;
  modal.classList.remove("hidden");
  modal.classList.add("flex");
}

function eliminarDocumento(id) {
  if (!confirm("¿Seguro que deseas eliminar este documento?")) return;

  fetch(`/api/documentos/${id}`, {
    method: "DELETE",
  })
    .then((resp) => {
      if (!resp.ok) throw new Error("Error al eliminar");
      // Volver a cargar la tabla
      renderizarTabla();
    })
    .catch((err) => {
      console.error(err);
      alert("No se pudo eliminar el documento.");
    });
}

// Llamar al cargar la página
document.addEventListener("DOMContentLoaded", renderizarTabla);
