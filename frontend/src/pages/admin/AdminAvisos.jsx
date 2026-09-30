import { useState } from 'react'
import { api } from '../../api/cliente.js'
import { useDatos } from '../../api/useDatos.js'
import Estado from '../../components/Estado.jsx'
import { useToast } from '../../context/ToastContext.jsx'

const CANALES = [
  {
    campo: 'canalPush',
    titulo: '🔔 Notificaciones en el móvil',
    texto: 'Llegan a quien las haya activado en "Mi cuenta".',
  },
  {
    campo: 'canalEmail',
    titulo: '✉️ Email',
    texto: 'Llega a quien tenga el email puesto en su cuenta.',
  },
  {
    campo: 'canalWhatsapp',
    titulo: '💬 Botón de WhatsApp',
    texto: 'Muestra en "Resumen" un botón para mandar las deudas y el stock al grupo con un toque.',
  },
]

export default function AdminAvisos() {
  const toast = useToast()
  const { datos: ajustes, error, cargando, recargar } = useDatos('/admin/ajustes')
  const [guardando, setGuardando] = useState(false)

  async function cambiar(campo, valor) {
    setGuardando(true)
    try {
      const cuerpo = {
        canalPush: ajustes.canalPush,
        canalEmail: ajustes.canalEmail,
        canalWhatsapp: ajustes.canalWhatsapp,
        [campo]: valor,
      }
      await api('/admin/ajustes', { metodo: 'PUT', cuerpo })
      await recargar()
    } catch (e) {
      toast(e.message, { tipo: 'error' })
    } finally {
      setGuardando(false)
    }
  }

  async function lanzar(ruta, mensaje) {
    try {
      await api(ruta, { metodo: 'POST' })
      toast(mensaje)
    } catch (e) {
      toast(e.message, { tipo: 'error' })
    }
  }

  if (!ajustes) return <Estado cargando={cargando} error={error} onReintentar={recargar} />

  return (
    <>
      <div className="tarjeta">
        <h2>¿Por dónde avisamos?</h2>
        <p className="texto-suave texto-pequeno">
          Se avisa a los admins cuando un producto llega a su mínimo, y a cada miembro con su deuda el día 1 de cada
          mes. Puedes tener varios canales activos a la vez.
        </p>
        <ul className="lista">
          {CANALES.map((c) => (
            <li key={c.campo} className="fila">
              <span>
                <strong>{c.titulo}</strong>
                <span className="texto-suave texto-pequeno bloque">{c.texto}</span>
                {c.campo === 'canalEmail' && !ajustes.emailConfigurado && (
                  <span className="stock-bajo texto-pequeno bloque">
                    Falta configurar el servidor de correo (mira el README).
                  </span>
                )}
              </span>
              <label className="interruptor">
                <input
                  type="checkbox"
                  checked={ajustes[c.campo]}
                  onChange={(e) => cambiar(c.campo, e.target.checked)}
                  disabled={guardando}
                />
                <span aria-hidden="true" />
                <span className="solo-lector">{c.titulo}</span>
              </label>
            </li>
          ))}
        </ul>
      </div>

      <div className="tarjeta">
        <h2>Probar</h2>
        <div className="botones-apilados">
          <button
            className="boton boton-secundario"
            onClick={() => lanzar('/admin/avisos/prueba', 'Aviso de prueba enviado a ti')}
          >
            Enviarme un aviso de prueba
          </button>
          <button
            className="boton boton-secundario"
            onClick={() => lanzar('/admin/avisos/resumen', 'Resumen de deudas enviado a todos')}
          >
            Enviar el resumen de deudas ahora
          </button>
        </div>
      </div>
    </>
  )
}
