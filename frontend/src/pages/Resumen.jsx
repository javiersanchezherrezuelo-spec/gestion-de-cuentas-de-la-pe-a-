import { useDatos } from '../api/useDatos.js'
import Estado from '../components/Estado.jsx'
import { useAuth } from '../context/AuthContext.jsx'
import { formatoEuros } from '../formato.js'

export default function Resumen() {
  const { usuario } = useAuth()
  const { datos, error, cargando, recargar } = useDatos('/resumen')

  if (!datos) return <Estado cargando={cargando} error={error} onReintentar={recargar} />

  return (
    <section>
      <h1 className="titulo-pagina">Resumen</h1>

      {datos.whatsapp && (
        <a
          className="boton boton-whatsapp boton-ancho"
          href={`https://wa.me/?text=${encodeURIComponent(textoWhatsapp(datos))}`}
          target="_blank"
          rel="noreferrer"
        >
          💬 Compartir por WhatsApp
        </a>
      )}

      <div className="tarjeta">
        <div className="tarjeta-cabecera">
          <h2>Deudas</h2>
          <span className="total">Total {formatoEuros(datos.totalDeuda)}</span>
        </div>
        {datos.deudas.length === 0 ? (
          <p className="vacio">No hay miembros todavía.</p>
        ) : (
          <ul className="lista">
            {datos.deudas.map((d) => (
              <li key={d.usuarioId} className={`fila${d.usuarioId === usuario.id ? ' fila-yo' : ''}`}>
                <span>
                  {d.nombre}
                  {d.usuarioId === usuario.id && <span className="etiqueta">tú</span>}
                </span>
                <Importe deuda={d.deuda} />
              </li>
            ))}
          </ul>
        )}
      </div>

      <div className="tarjeta">
        <div className="tarjeta-cabecera">
          <h2>Stock</h2>
        </div>
        {datos.productos.length === 0 ? (
          <p className="vacio">No hay productos.</p>
        ) : (
          <ul className="lista">
            {datos.productos.map((p) => (
              <li key={p.id} className="fila">
                <span>
                  <span aria-hidden="true">{p.emoji || '🥤'}</span> {p.nombre}
                </span>
                <span className={p.stockBajo ? 'stock-bajo' : 'texto-suave'}>
                  {p.stock} uds{p.stockBajo && ' · ¡quedan pocas!'}
                </span>
              </li>
            ))}
          </ul>
        )}
      </div>
    </section>
  )
}

export function Importe({ deuda }) {
  const valor = Number(deuda)
  if (valor > 0) return <span className="importe-debe">{formatoEuros(valor)}</span>
  if (valor < 0) return <span className="importe-favor">{formatoEuros(-valor)} a favor</span>
  return <span className="importe-cero">Al día</span>
}

/** Mensaje para pegar en el grupo de WhatsApp con las deudas y lo que se está acabando. */
function textoWhatsapp({ deudas, totalDeuda, productos }) {
  const lineas = ['🍻 *Peña Los Informales*', '', '*Deudas*']
  const conDeuda = deudas.filter((d) => Number(d.deuda) > 0)
  if (conDeuda.length === 0) lineas.push('¡Nadie debe nada! 🎉')
  else {
    conDeuda.forEach((d) => lineas.push(`• ${d.nombre}: ${formatoEuros(d.deuda)}`))
    lineas.push(`Total: ${formatoEuros(totalDeuda)}`)
  }
  const bajos = productos.filter((p) => p.stockBajo)
  if (bajos.length > 0) {
    lineas.push('', '*Hay que reponer*')
    bajos.forEach((p) => lineas.push(`• ${p.emoji ? `${p.emoji} ` : ''}${p.nombre}: quedan ${p.stock}`))
  }
  return lineas.join('\n')
}
