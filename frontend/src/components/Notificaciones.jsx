import { useEffect, useState } from 'react'
import { activarPush, desactivarPush, esIOS, estadoPush, estaInstalada } from '../api/push.js'
import { useToast } from '../context/ToastContext.jsx'

/** Tarjeta de "Mi cuenta" para activar las notificaciones en este móvil. */
export default function Notificaciones() {
  const toast = useToast()
  const [estado, setEstado] = useState('cargando')
  const [ocupado, setOcupado] = useState(false)

  useEffect(() => {
    estadoPush().then(setEstado).catch(() => setEstado('no-soportado'))
  }, [])

  async function cambiar(activar) {
    setOcupado(true)
    try {
      if (activar) await activarPush()
      else await desactivarPush()
      setEstado(await estadoPush())
      toast(activar ? 'Notificaciones activadas en este dispositivo' : 'Notificaciones desactivadas')
    } catch (e) {
      toast(e.message, { tipo: 'error' })
      setEstado(await estadoPush().catch(() => 'no-soportado'))
    } finally {
      setOcupado(false)
    }
  }

  let contenido
  if (estado === 'cargando') {
    contenido = null
  } else if (!window.isSecureContext) {
    contenido = (
      <p className="texto-suave">
        Las notificaciones solo funcionan si la web se abre con <strong>https://</strong>. Pide a un admin la dirección
        segura.
      </p>
    )
  } else if (estado === 'no-soportado' && esIOS() && !estaInstalada()) {
    contenido = (
      <p className="texto-suave">
        En iPhone, primero añade la web a la pantalla de inicio: toca <strong>Compartir</strong> →{' '}
        <strong>Añadir a pantalla de inicio</strong>, ábrela desde ese icono y vuelve aquí.
      </p>
    )
  } else if (estado === 'no-soportado') {
    contenido = <p className="texto-suave">Este navegador no permite notificaciones.</p>
  } else if (estado === 'bloqueadas') {
    contenido = (
      <p className="texto-suave">
        Has bloqueado las notificaciones para esta web. Actívalas en los ajustes del navegador y recarga la página.
      </p>
    )
  } else if (estado === 'activas') {
    contenido = (
      <>
        <p className="texto-suave">✅ Este dispositivo recibe los avisos de la peña.</p>
        <button className="boton boton-secundario" onClick={() => cambiar(false)} disabled={ocupado}>
          Desactivar en este dispositivo
        </button>
      </>
    )
  } else {
    contenido = (
      <>
        <p className="texto-suave">Recibe en el móvil el resumen de tu cuenta cada mes y otros avisos de la peña.</p>
        <button className="boton boton-principal" onClick={() => cambiar(true)} disabled={ocupado}>
          🔔 Activar notificaciones
        </button>
      </>
    )
  }

  return (
    <div className="tarjeta">
      <h2>Notificaciones</h2>
      {contenido}
    </div>
  )
}
