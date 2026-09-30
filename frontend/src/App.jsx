import { Navigate, Route, Routes } from 'react-router-dom'
import { Cabecera, NavInferior } from './components/Navegacion.jsx'
import { useAuth } from './context/AuthContext.jsx'
import Admin from './pages/admin/Admin.jsx'
import Apuntar from './pages/Apuntar.jsx'
import Login from './pages/Login.jsx'
import MiCuenta from './pages/MiCuenta.jsx'
import Resumen from './pages/Resumen.jsx'

export default function App() {
  const { usuario, esAdmin } = useAuth()

  if (!usuario) return <Login />

  return (
    <div className="app">
      <Cabecera />
      <main className="contenido">
        <Routes>
          <Route path="/" element={<Apuntar />} />
          <Route path="/resumen" element={<Resumen />} />
          <Route path="/cuenta" element={<MiCuenta />} />
          {esAdmin && <Route path="/admin/*" element={<Admin />} />}
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </main>
      <NavInferior />
    </div>
  )
}
