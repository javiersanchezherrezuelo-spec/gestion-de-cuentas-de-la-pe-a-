import { useState } from 'react'
import { api } from '../api/cliente.js'
import { useDatos } from '../api/useDatos.js'
import Estado from '../components/Estado.jsx'
import Notificaciones from '../components/Notificaciones.jsx'
import { useAuth } from '../context/AuthContext.jsx'
import { useToast } from '../context/ToastContext.jsx'
import { formatoEuros, formatoFecha } from '../formato.js'
import { Importe } from './Resumen.jsx'

export default function MiCuenta() {
  const { usuario, logout } = useAuth()
  const toast = useToast()
  const { datos: cuenta, error, cargando, recargar } = useDatos('/yo/cuenta')

  async function deshacer(consumo) {
    try {
      await api(`/consumos/${consumo.id}`, { metodo: 'DELETE' })
      toast('Apunte deshecho')
      recargar()
    } catch (e) {
      toast(e.message, { tipo: 'error' })
    }
  }

  return (
    <section>
      <h1 className="titulo-pagina">Mi cuenta</h1>

      <Estado cargando={cargando && !cuenta} error={error} onReintentar={recargar} />

      {cuenta && (
        <>
          <div className="tarjeta tarjeta-deuda">
            <span className="texto-suave">Lo que debes ahora</span>
            <span className="deuda-grande">
              <Importe deuda={cuenta.deuda} />
            </span>
            <span className="texto-suave texto-pequeno">
              Consumido {formatoEuros(cuenta.consumido)} · Pagado {formatoEuros(cuenta.pagado)}
            </span>
          </div>

          <div className="tarjeta">
            <h2>Mis apuntes</h2>
            <ListaConsumos consumos={cuenta.consumos} onAnular={deshacer} soloDeshacibles />
          </div>

          <div className="tarjeta">
            <h2>Mis pagos</h2>
            <ListaPagos pagos={cuenta.pagos} />
          </div>
        </>
      )}

      <Notificaciones />

      <div className="tarjeta">
        <h2>Mis datos</h2>
        <p className="texto-suave">
          Usuario: <strong>{usuario.username}</strong>
        </p>
        <FormEmail />
        <FormPassword />
      </div>

      <button className="boton boton-secundario boton-ancho" onClick={logout}>
        Cerrar sesión
      </button>
    </section>
  )
}

/**
 * Lista de apuntes. Con "soloDeshacibles", el botón solo aparece durante los minutos en que
 * el miembro puede deshacer el apunte; si no, aparece siempre (para admins).
 */
export function ListaConsumos({ consumos, onAnular, soloDeshacibles = false }) {
  if (consumos.length === 0) return <p className="vacio">Sin apuntes todavía.</p>
  const ahora = Date.now()
  return (
    <ul className="lista">
      {consumos.map((c) => {
        const puedeAnular = !c.anulado && (!soloDeshacibles || new Date(c.deshacibleHasta).getTime() > ahora)
        return (
          <li key={c.id} className={`fila${c.anulado ? ' anulado' : ''}`}>
            <span>
              {c.cantidad} × {c.emoji} {c.producto}
              <span className="texto-suave texto-pequeno bloque">{formatoFecha(c.fecha)}</span>
            </span>
            <span className="fila-derecha">
              {formatoEuros(c.total)}
              {c.anulado && <span className="etiqueta">anulado</span>}
              {puedeAnular && (
                <button className="boton-enlace" onClick={() => onAnular(c)}>
                  {soloDeshacibles ? 'Deshacer' : 'Anular'}
                </button>
              )}
            </span>
          </li>
        )
      })}
    </ul>
  )
}

export function ListaPagos({ pagos, onBorrar }) {
  if (pagos.length === 0) return <p className="vacio">Sin pagos todavía.</p>
  return (
    <ul className="lista">
      {pagos.map((p) => (
        <li key={p.id} className="fila">
          <span>
            {formatoFecha(p.fecha)}
            <span className="texto-suave texto-pequeno bloque">
              Apuntado por {p.registradoPor}
              {p.nota && ` · ${p.nota}`}
            </span>
          </span>
          <span className="fila-derecha">
            <span className="importe-favor">{formatoEuros(p.importe)}</span>
            {onBorrar && (
              <button className="boton-enlace" onClick={() => onBorrar(p)}>
                Borrar
              </button>
            )}
          </span>
        </li>
      ))}
    </ul>
  )
}

function FormEmail() {
  const { usuario, actualizarUsuario } = useAuth()
  const toast = useToast()
  const [email, setEmail] = useState(usuario.email ?? '')
  const [error, setError] = useState(null)

  async function guardar(e) {
    e.preventDefault()
    setError(null)
    try {
      actualizarUsuario(await api('/yo/email', { metodo: 'PUT', cuerpo: { email } }))
      toast('Email guardado')
    } catch (err) {
      setError(err.message)
    }
  }

  return (
    <form className="formulario" onSubmit={guardar}>
      <label className="campo">
        <span>Email (para recibir avisos)</span>
        <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} autoComplete="email" />
      </label>
      {error && <p className="error-formulario">{error}</p>}
      <button className="boton boton-secundario">Guardar email</button>
    </form>
  )
}

function FormPassword() {
  const toast = useToast()
  const [actual, setActual] = useState('')
  const [nueva, setNueva] = useState('')
  const [error, setError] = useState(null)

  async function cambiar(e) {
    e.preventDefault()
    setError(null)
    try {
      await api('/yo/password', { metodo: 'PUT', cuerpo: { actual, nueva } })
      setActual('')
      setNueva('')
      toast('Contraseña cambiada')
    } catch (err) {
      setError(err.message)
    }
  }

  return (
    <form className="formulario" onSubmit={cambiar}>
      <label className="campo">
        <span>Contraseña actual</span>
        <input
          type="password"
          value={actual}
          onChange={(e) => setActual(e.target.value)}
          autoComplete="current-password"
          required
        />
      </label>
      <label className="campo">
        <span>Contraseña nueva</span>
        <input
          type="password"
          value={nueva}
          onChange={(e) => setNueva(e.target.value)}
          autoComplete="new-password"
          minLength={6}
          required
        />
      </label>
      {error && <p className="error-formulario">{error}</p>}
      <button className="boton boton-secundario">Cambiar contraseña</button>
    </form>
  )
}
