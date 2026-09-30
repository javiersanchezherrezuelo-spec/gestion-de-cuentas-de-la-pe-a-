import { useState } from 'react'
import { api } from '../api/cliente.js'
import { useDatos } from '../api/useDatos.js'
import Estado from '../components/Estado.jsx'
import Modal from '../components/Modal.jsx'
import { useAuth } from '../context/AuthContext.jsx'
import { useToast } from '../context/ToastContext.jsx'
import { formatoEuros } from '../formato.js'

export default function Apuntar() {
  const { usuario, esAdmin } = useAuth()
  const toast = useToast()
  const { datos: productos, error, cargando, recargar } = useDatos('/productos')
  const { datos: usuarios } = useDatos(esAdmin ? '/admin/usuarios' : null)
  const [elegido, setElegido] = useState(null)
  const [paraQuien, setParaQuien] = useState('')

  async function apuntar(cantidad) {
    const consumo = await api('/consumos', {
      metodo: 'POST',
      cuerpo: { productoId: elegido.id, cantidad, usuarioId: paraQuien ? Number(paraQuien) : null },
    })
    setElegido(null)
    recargar()
    toast(`Apuntado: ${cantidad} × ${consumo.producto} (${formatoEuros(consumo.total)})`, {
      segundos: 8,
      accion: {
        texto: 'Deshacer',
        alPulsar: async () => {
          try {
            await api(`/consumos/${consumo.id}`, { metodo: 'DELETE' })
            recargar()
            toast('Apunte deshecho')
          } catch (e) {
            toast(e.message, { tipo: 'error' })
          }
        },
      },
    })
  }

  const otros = usuarios?.filter((u) => u.activo && u.id !== usuario.id) ?? []

  return (
    <section>
      <h1 className="titulo-pagina">¿Qué te tomas?</h1>

      {esAdmin && otros.length > 0 && (
        <label className="campo campo-en-linea">
          <span>Apuntar a</span>
          <select value={paraQuien} onChange={(e) => setParaQuien(e.target.value)}>
            <option value="">Mí mismo</option>
            {otros.map((u) => (
              <option key={u.id} value={u.id}>
                {u.nombre}
              </option>
            ))}
          </select>
        </label>
      )}

      <Estado cargando={cargando && !productos} error={error} onReintentar={recargar} />

      {productos?.length === 0 && (
        <p className="vacio">Todavía no hay productos. {esAdmin ? 'Créalos en Admin → Productos.' : ''}</p>
      )}

      <div className="rejilla-productos">
        {productos?.map((p) => (
          <button
            key={p.id}
            className={`tarjeta-producto${p.stock === 0 ? ' agotado' : ''}`}
            onClick={() => setElegido(p)}
            disabled={p.stock === 0}
          >
            <span className="producto-emoji" aria-hidden="true">
              {p.emoji || '🥤'}
            </span>
            <span className="producto-nombre">{p.nombre}</span>
            <span className="producto-precio">{formatoEuros(p.precio)}</span>
            <span className={`producto-stock${p.stockBajo ? ' bajo' : ''}`}>
              {p.stock === 0 ? 'Agotado' : `Quedan ${p.stock}`}
            </span>
          </button>
        ))}
      </div>

      {elegido && <ElegirCantidad producto={elegido} onApuntar={apuntar} onCerrar={() => setElegido(null)} />}
    </section>
  )
}

function ElegirCantidad({ producto, onApuntar, onCerrar }) {
  const [cantidad, setCantidad] = useState(1)
  const [error, setError] = useState(null)
  const [enviando, setEnviando] = useState(false)
  const maximo = Math.min(producto.stock, 50)

  async function confirmar() {
    setEnviando(true)
    setError(null)
    try {
      await onApuntar(cantidad)
    } catch (e) {
      setError(e.message)
      setEnviando(false)
    }
  }

  return (
    <Modal titulo={`${producto.emoji || ''} ${producto.nombre}`.trim()} onCerrar={onCerrar}>
      <div className="selector-cantidad">
        <button
          className="boton-redondo"
          onClick={() => setCantidad((c) => c - 1)}
          disabled={cantidad <= 1}
          aria-label="Una menos"
        >
          −
        </button>
        <span className="cantidad" aria-live="polite">
          {cantidad}
        </span>
        <button
          className="boton-redondo"
          onClick={() => setCantidad((c) => c + 1)}
          disabled={cantidad >= maximo}
          aria-label="Una más"
        >
          +
        </button>
      </div>
      <p className="texto-centrado texto-suave">
        {formatoEuros(producto.precio)} cada una · quedan {producto.stock}
      </p>
      {error && <p className="error-formulario">{error}</p>}
      <button className="boton boton-principal boton-ancho boton-grande" onClick={confirmar} disabled={enviando}>
        Apuntar {cantidad} · {formatoEuros(producto.precio * cantidad)}
      </button>
    </Modal>
  )
}
