import { Navigate, NavLink, Route, Routes } from 'react-router-dom'
import AdminAvisos from './AdminAvisos.jsx'
import AdminCuentas from './AdminCuentas.jsx'
import AdminProductos from './AdminProductos.jsx'
import AdminUsuarios from './AdminUsuarios.jsx'

export default function Admin() {
  return (
    <section>
      <h1 className="titulo-pagina">Administración</h1>
      <nav className="pestanas">
        <NavLink to="productos">Productos</NavLink>
        <NavLink to="cuentas">Cuentas</NavLink>
        <NavLink to="usuarios">Usuarios</NavLink>
        <NavLink to="avisos">Avisos</NavLink>
      </nav>
      <Routes>
        <Route path="productos" element={<AdminProductos />} />
        <Route path="cuentas" element={<AdminCuentas />} />
        <Route path="usuarios" element={<AdminUsuarios />} />
        <Route path="avisos" element={<AdminAvisos />} />
        <Route path="*" element={<Navigate to="productos" replace />} />
      </Routes>
    </section>
  )
}
