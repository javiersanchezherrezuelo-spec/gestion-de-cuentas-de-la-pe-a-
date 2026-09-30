import { NavLink } from 'react-router-dom'
import { useAuth } from '../context/AuthContext.jsx'

export function Cabecera() {
  const { usuario } = useAuth()
  return (
    <header className="cabecera">
      <span className="marca">
        <span aria-hidden="true">🍻</span> Los Informales
      </span>
      <span className="cabecera-usuario">{usuario.nombre}</span>
    </header>
  )
}

export function NavInferior() {
  const { esAdmin } = useAuth()
  const enlaces = [
    { a: '/', icono: '🍺', texto: 'Apuntar' },
    { a: '/resumen', icono: '📊', texto: 'Resumen' },
    { a: '/cuenta', icono: '👤', texto: 'Mi cuenta' },
  ]
  if (esAdmin) enlaces.push({ a: '/admin', icono: '⚙️', texto: 'Admin' })

  return (
    <nav className="nav-inferior">
      {enlaces.map((e) => (
        <NavLink key={e.a} to={e.a} end={e.a === '/'} className="nav-enlace">
          <span className="nav-icono" aria-hidden="true">
            {e.icono}
          </span>
          {e.texto}
        </NavLink>
      ))}
    </nav>
  )
}
