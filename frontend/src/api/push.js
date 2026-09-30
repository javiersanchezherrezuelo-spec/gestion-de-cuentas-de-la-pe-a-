// Activar y desactivar las notificaciones en este dispositivo.
import { api } from './cliente.js'

export const esIOS = () => /iPad|iPhone|iPod/.test(navigator.userAgent)

export const estaInstalada = () =>
  window.matchMedia('(display-mode: standalone)').matches || window.navigator.standalone === true

export function soportaPush() {
  return 'serviceWorker' in navigator && 'PushManager' in window && 'Notification' in window
}

export function registrarServiceWorker() {
  if ('serviceWorker' in navigator) {
    navigator.serviceWorker.register('/sw.js').catch(() => {})
  }
}

/** Devuelve 'activas', 'inactivas', 'bloqueadas' o 'no-soportado'. */
export async function estadoPush() {
  if (!soportaPush()) return 'no-soportado'
  if (Notification.permission === 'denied') return 'bloqueadas'
  const registro = await navigator.serviceWorker.getRegistration()
  const suscripcion = await registro?.pushManager.getSubscription()
  return suscripcion ? 'activas' : 'inactivas'
}

export async function activarPush() {
  const permiso = await Notification.requestPermission()
  if (permiso !== 'granted') throw new Error('No has dado permiso para las notificaciones')

  const registro = await navigator.serviceWorker.register('/sw.js')
  await navigator.serviceWorker.ready
  const clavePublica = await api('/push/clave-publica')
  const suscripcion = await registro.pushManager.subscribe({
    userVisibleOnly: true,
    applicationServerKey: base64UrlABytes(clavePublica),
  })
  await api('/push/suscripciones', { metodo: 'POST', cuerpo: suscripcion.toJSON() })
}

export async function desactivarPush() {
  const registro = await navigator.serviceWorker.getRegistration()
  const suscripcion = await registro?.pushManager.getSubscription()
  if (!suscripcion) return
  await api('/push/baja', { metodo: 'POST', cuerpo: { endpoint: suscripcion.endpoint } })
  await suscripcion.unsubscribe()
}

function base64UrlABytes(texto) {
  const base64 = (texto + '='.repeat((4 - (texto.length % 4)) % 4)).replace(/-/g, '+').replace(/_/g, '/')
  return Uint8Array.from(atob(base64), (c) => c.charCodeAt(0))
}
