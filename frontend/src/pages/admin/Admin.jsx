import { Navigate, NavLink, Route, Routes } from 'react-router-dom'
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
      </nav>
      <Routes>
        <Route path="productos" element={<AdminProductos />} />
        <Route path="cuentas" element={<AdminCuentas />} />
        <Route path="usuarios" element={<AdminUsuarios />} />
        <Route path="*" element={<Navigate to="productos" replace />} />
      </Routes>
    </section>
  )
}
