// Service worker: recibe las notificaciones push y abre la web al tocarlas.

self.addEventListener('install', () => self.skipWaiting())
self.addEventListener('activate', (event) => event.waitUntil(self.clients.claim()))

self.addEventListener('push', (event) => {
  let datos = { titulo: 'Los Informales', texto: '', url: '/' }
  try {
    datos = { ...datos, ...event.data.json() }
  } catch {
    if (event.data) datos.texto = event.data.text()
  }
  event.waitUntil(
    self.registration.showNotification(datos.titulo, {
      body: datos.texto,
      icon: '/icono-192.png',
      badge: '/icono-192.png',
      data: { url: datos.url },
    }),
  )
})

self.addEventListener('notificationclick', (event) => {
  event.notification.close()
  const url = new URL(event.notification.data?.url || '/', self.location.origin).href
  event.waitUntil(
    self.clients.matchAll({ type: 'window', includeUncontrolled: true }).then((ventanas) => {
      for (const ventana of ventanas) {
        if (ventana.url.startsWith(self.location.origin) && 'focus' in ventana) {
          ventana.navigate(url)
          return ventana.focus()
        }
      }
      return self.clients.openWindow(url)
    }),
  )
})
