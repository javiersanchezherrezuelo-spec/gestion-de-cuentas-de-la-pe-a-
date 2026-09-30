import { useState } from 'react'
import { api } from '../../api/cliente.js'
import { useDatos } from '../../api/useDatos.js'
import Estado from '../../components/Estado.jsx'
import Modal from '../../components/Modal.jsx'
import { useToast } from '../../context/ToastContext.jsx'

export default function AdminUsuarios() {
  const { datos: usuarios, error, cargando, recargar } = useDatos('/admin/usuarios')
  const [editando, setEditando] = useState(null)

  return (
    <>
      <button className="boton boton-principal boton-ancho" onClick={() => setEditando({})}>
        + Nuevo miembro
      </button>

      <Estado cargando={cargando && !usuarios} error={error} onReintentar={recargar} />

      <ul className="lista lista-tarjetas">
        {usuarios?.map((u) => (
          <li key={u.id} className={`tarjeta fila${u.activo ? '' : ' inactivo'}`}>
            <span>
              <strong>{u.nombre}</strong>
              {u.rol === 'ADMIN' && <span className="etiqueta">admin</span>}
              {!u.activo && <span className="etiqueta">desactivado</span>}
              <span className="texto-suave texto-pequeno bloque">
                @{u.username}
                {u.email && ` · ${u.email}`}
              </span>
            </span>
            <button className="boton-enlace" onClick={() => setEditando(u)}>
              Editar
            </button>
          </li>
        ))}
      </ul>

      {editando && (
        <FormUsuario
          usuario={editando}
          onGuardado={() => {
            setEditando(null)
            recargar()
          }}
          onCerrar={() => setEditando(null)}
        />
      )}
    </>
  )
}

function FormUsuario({ usuario, onGuardado, onCerrar }) {
  const toast = useToast()
  const esNuevo = !usuario.id
  const [form, setForm] = useState({
    nombre: usuario.nombre ?? '',
    username: usuario.username ?? '',
    email: usuario.email ?? '',
    password: '',
    rol: usuario.rol ?? 'MIEMBRO',
    activo: usuario.activo ?? true,
  })
  const [error, setError] = useState(null)
  const cambiar = (campo) => (e) =>
    setForm({ ...form, [campo]: e.target.type === 'checkbox' ? e.target.checked : e.target.value })

  async function guardar(e) {
    e.preventDefault()
    setError(null)
    try {
      await api(esNuevo ? '/admin/usuarios' : `/admin/usuarios/${usuario.id}`, {
        metodo: esNuevo ? 'POST' : 'PUT',
        cuerpo: { ...form, email: form.email || null, password: form.password || null },
      })
      toast(esNuevo ? `${form.nombre} ya puede entrar` : 'Usuario guardado')
      onGuardado()
    } catch (err) {
      setError(err.message)
    }
  }

  return (
    <Modal titulo={esNuevo ? 'Nuevo miembro' : `Editar a ${usuario.nombre}`} onCerrar={onCerrar}>
      <form className="formulario" onSubmit={guardar}>
        <label className="campo">
          <span>Nombre</span>
          <input value={form.nombre} onChange={cambiar('nombre')} required maxLength={60} />
        </label>
        <label className="campo">
          <span>Usuario (para entrar)</span>
          <input
            value={form.username}
            onChange={cambiar('username')}
            required
            autoCapitalize="none"
            autoCorrect="off"
            pattern="[a-zA-Z0-9._\-]{3,40}"
            title="Entre 3 y 40 letras, números, puntos o guiones"
          />
        </label>
        <label className="campo">
          <span>Email (opcional)</span>
          <input type="email" value={form.email} onChange={cambiar('email')} />
        </label>
        <label className="campo">
          <span>{esNuevo ? 'Contraseña inicial' : 'Nueva contraseña'}</span>
          <input
            type="text"
            value={form.password}
            onChange={cambiar('password')}
            minLength={6}
            required={esNuevo}
            placeholder={esNuevo ? '' : 'Déjala vacía para no cambiarla'}
            autoComplete="off"
          />
        </label>
        <div className="campos-en-fila">
          <label className="campo">
            <span>Rol</span>
            <select value={form.rol} onChange={cambiar('rol')}>
              <option value="MIEMBRO">Miembro</option>
              <option value="ADMIN">Administrador</option>
            </select>
          </label>
          {!esNuevo && (
            <label className="casilla">
              <input type="checkbox" checked={form.activo} onChange={cambiar('activo')} />
              Activo
            </label>
          )}
        </div>
        {error && <p className="error-formulario">{error}</p>}
        <button className="boton boton-principal boton-ancho">Guardar</button>
      </form>
    </Modal>
  )
}
