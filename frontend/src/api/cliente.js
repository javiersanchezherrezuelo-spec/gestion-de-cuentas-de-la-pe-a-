// Pequeño cliente para hablar con el backend. Añade el token y convierte los errores en mensajes legibles.

const CLAVE_TOKEN = 'pena.token'

export function getToken() {
  try {
    return localStorage.getItem(CLAVE_TOKEN)
  } catch {
    return null
  }
}

export function setToken(token) {
  try {
    if (token) localStorage.setItem(CLAVE_TOKEN, token)
    else localStorage.removeItem(CLAVE_TOKEN)
  } catch {
    // Sin almacenamiento (modo privado): la sesión solo dura mientras la pestaña está abierta
  }
}

let alCaducarSesion = () => {}

export function onSesionCaducada(funcion) {
  alCaducarSesion = funcion
}

export async function api(ruta, { metodo = 'GET', cuerpo } = {}) {
  const cabeceras = {}
  const token = getToken()
  if (token) cabeceras.Authorization = `Bearer ${token}`
  if (cuerpo !== undefined) cabeceras['Content-Type'] = 'application/json'

  let respuesta
  try {
    respuesta = await fetch(`/api${ruta}`, {
      method: metodo,
      headers: cabeceras,
      body: cuerpo !== undefined ? JSON.stringify(cuerpo) : undefined,
    })
  } catch {
    throw new Error('No hay conexión con el servidor')
  }

  if (respuesta.status === 401 && token) {
    alCaducarSesion()
    throw new Error('Tu sesión ha caducado. Vuelve a entrar.')
  }
  if (respuesta.status === 204) return null

  const datos = await respuesta.json().catch(() => null)
  if (!respuesta.ok) {
    if (datos?.error) throw new Error(datos.error)
    if (respuesta.status === 403) throw new Error('No tienes permiso para hacer esto')
    throw new Error('Ha ocurrido un error. Inténtalo de nuevo.')
  }
  return datos
}
