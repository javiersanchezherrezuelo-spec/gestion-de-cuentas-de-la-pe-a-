import { createContext, useCallback, useContext, useEffect, useState } from 'react'
import { api, getToken, onSesionCaducada, setToken } from '../api/cliente.js'

const CLAVE_USUARIO = 'pena.usuario'
const AuthContext = createContext(null)

function leerUsuarioGuardado() {
  if (!getToken()) return null
  try {
    return JSON.parse(localStorage.getItem(CLAVE_USUARIO))
  } catch {
    return null
  }
}

function guardarUsuario(usuario) {
  try {
    if (usuario) localStorage.setItem(CLAVE_USUARIO, JSON.stringify(usuario))
    else localStorage.removeItem(CLAVE_USUARIO)
  } catch {
    // Sin almacenamiento: no pasa nada
  }
}

export function AuthProvider({ children }) {
  const [usuario, setUsuario] = useState(leerUsuarioGuardado)

  const actualizarUsuario = useCallback((nuevo) => {
    guardarUsuario(nuevo)
    setUsuario(nuevo)
  }, [])

  const logout = useCallback(() => {
    setToken(null)
    actualizarUsuario(null)
  }, [actualizarUsuario])

  useEffect(() => {
    onSesionCaducada(logout)
  }, [logout])

  // Al abrir la web, refresca los datos del usuario por si un admin le ha cambiado el rol
  useEffect(() => {
    if (getToken()) {
      api('/yo').then(actualizarUsuario).catch(() => {})
    }
  }, [actualizarUsuario])

  const login = useCallback(
    async (username, password) => {
      const respuesta = await api('/auth/login', { metodo: 'POST', cuerpo: { username, password } })
      setToken(respuesta.token)
      actualizarUsuario(respuesta.usuario)
    },
    [actualizarUsuario],
  )

  return (
    <AuthContext.Provider value={{ usuario, esAdmin: usuario?.rol === 'ADMIN', login, logout, actualizarUsuario }}>
      {children}
    </AuthContext.Provider>
  )
}

// eslint-disable-next-line react/only-export-components
export const useAuth = () => useContext(AuthContext)
