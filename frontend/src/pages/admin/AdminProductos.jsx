import { useState } from 'react'
import { api } from '../../api/cliente.js'
import { useDatos } from '../../api/useDatos.js'
import Estado from '../../components/Estado.jsx'
import Modal from '../../components/Modal.jsx'
import { useToast } from '../../context/ToastContext.jsx'
import { formatoEuros, formatoFecha } from '../../formato.js'

export default function AdminProductos() {
  const { datos: productos, error, cargando, recargar } = useDatos('/admin/productos')
  const { datos: reposiciones, recargar: recargarReposiciones } = useDatos('/admin/reposiciones')
  const [editando, setEditando] = useState(null) // producto, o {} para uno nuevo
  const [reponiendo, setReponiendo] = useState(null)

  function alGuardar() {
    setEditando(null)
    setReponiendo(null)
    recargar()
    recargarReposiciones()
  }

  return (
    <>
      <button className="boton boton-principal boton-ancho" onClick={() => setEditando({})}>
        + Nuevo producto
      </button>

      <Estado cargando={cargando && !productos} error={error} onReintentar={recargar} />

      {productos?.length === 0 && <p className="vacio">Crea el primer producto (por ejemplo, Cerveza).</p>}

      <ul className="lista lista-tarjetas">
        {productos?.map((p) => (
          <li key={p.id} className={`tarjeta fila${p.activo ? '' : ' inactivo'}`}>
            <span>
              <strong>
                {p.emoji} {p.nombre}
              </strong>
              {!p.activo && <span className="etiqueta">oculto</span>}
              <span className="texto-suave texto-pequeno bloque">
                {formatoEuros(p.precio)} ·{' '}
                <span className={p.stockBajo ? 'stock-bajo' : ''}>
                  {p.stock} uds (mín. {p.stockMinimo})
                </span>
              </span>
            </span>
            <span className="fila-derecha">
              <button className="boton boton-secundario boton-pequeno" onClick={() => setReponiendo(p)}>
                Reponer
              </button>
              <button className="boton-enlace" onClick={() => setEditando(p)}>
                Editar
              </button>
            </span>
          </li>
        ))}
      </ul>

      {reposiciones?.length > 0 && (
        <details className="tarjeta">
          <summary>Últimas reposiciones</summary>
          <ul className="lista">
            {reposiciones.map((r) => (
              <li key={r.id} className="fila">
                <span>
                  {r.cantidad > 0 ? '+' : ''}
                  {r.cantidad} {r.producto}
                  <span className="texto-suave texto-pequeno bloque">
                    {formatoFecha(r.fecha)} · {r.admin}
                  </span>
                </span>
              </li>
            ))}
          </ul>
        </details>
      )}

      {editando && <FormProducto producto={editando} onGuardado={alGuardar} onCerrar={() => setEditando(null)} />}
      {reponiendo && <FormReponer producto={reponiendo} onGuardado={alGuardar} onCerrar={() => setReponiendo(null)} />}
    </>
  )
}

function FormProducto({ producto, onGuardado, onCerrar }) {
  const toast = useToast()
  const esNuevo = !producto.id
  const [form, setForm] = useState({
    nombre: producto.nombre ?? '',
    emoji: producto.emoji ?? '',
    precio: producto.precio ?? '',
    stock: '',
    stockMinimo: producto.stockMinimo ?? 5,
    activo: producto.activo ?? true,
  })
  const [error, setError] = useState(null)
  const cambiar = (campo) => (e) =>
    setForm({ ...form, [campo]: e.target.type === 'checkbox' ? e.target.checked : e.target.value })

  async function guardar(e) {
    e.preventDefault()
    setError(null)
    const cuerpo = {
      ...form,
      precio: String(form.precio).replace(',', '.'),
      stock: esNuevo ? Number(form.stock || 0) : null,
      stockMinimo: Number(form.stockMinimo),
    }
    try {
      await api(esNuevo ? '/admin/productos' : `/admin/productos/${producto.id}`, {
        metodo: esNuevo ? 'POST' : 'PUT',
        cuerpo,
      })
      toast(esNuevo ? 'Producto creado' : 'Producto guardado')
      onGuardado()
    } catch (err) {
      setError(err.message)
    }
  }

  return (
    <Modal titulo={esNuevo ? 'Nuevo producto' : 'Editar producto'} onCerrar={onCerrar}>
      <form className="formulario" onSubmit={guardar}>
        <div className="campos-en-fila">
          <label className="campo campo-emoji">
            <span>Icono</span>
            <input value={form.emoji} onChange={cambiar('emoji')} placeholder="🍺" maxLength={8} />
          </label>
          <label className="campo">
            <span>Nombre</span>
            <input value={form.nombre} onChange={cambiar('nombre')} required maxLength={60} />
          </label>
        </div>
        <div className="campos-en-fila">
          <label className="campo">
            <span>Precio (€)</span>
            <input value={form.precio} onChange={cambiar('precio')} inputMode="decimal" placeholder="1,00" required />
          </label>
          {esNuevo && (
            <label className="campo">
              <span>Unidades que hay</span>
              <input type="number" min="0" value={form.stock} onChange={cambiar('stock')} placeholder="0" />
            </label>
          )}
          <label className="campo">
            <span>Avisar al bajar de</span>
            <input type="number" min="0" value={form.stockMinimo} onChange={cambiar('stockMinimo')} required />
          </label>
        </div>
        {!esNuevo && (
          <label className="casilla">
            <input type="checkbox" checked={form.activo} onChange={cambiar('activo')} />
            Visible para apuntar
          </label>
        )}
        {error && <p className="error-formulario">{error}</p>}
        <button className="boton boton-principal boton-ancho">Guardar</button>
      </form>
    </Modal>
  )
}

function FormReponer({ producto, onGuardado, onCerrar }) {
  const toast = useToast()
  const [cantidad, setCantidad] = useState('')
  const [error, setError] = useState(null)

  async function reponer(e) {
    e.preventDefault()
    setError(null)
    try {
      const actualizado = await api(`/admin/productos/${producto.id}/reponer`, {
        metodo: 'POST',
        cuerpo: { cantidad: Number(cantidad) },
      })
      toast(`${producto.nombre}: ahora hay ${actualizado.stock}`)
      onGuardado()
    } catch (err) {
      setError(err.message)
    }
  }

  return (
    <Modal titulo={`Reponer ${producto.nombre}`} onCerrar={onCerrar}>
      <form className="formulario" onSubmit={reponer}>
        <p className="texto-suave">Ahora hay {producto.stock} unidades.</p>
        <label className="campo">
          <span>Unidades que entran</span>
          <input
            type="number"
            value={cantidad}
            onChange={(e) => setCantidad(e.target.value)}
            placeholder="24"
            required
            autoFocus
          />
        </label>
        <p className="texto-suave texto-pequeno">
          Para corregir un recuento, pon un número negativo (por ejemplo, −2 si faltan dos).
        </p>
        {error && <p className="error-formulario">{error}</p>}
        <button className="boton boton-principal boton-ancho">Reponer</button>
      </form>
    </Modal>
  )
}
