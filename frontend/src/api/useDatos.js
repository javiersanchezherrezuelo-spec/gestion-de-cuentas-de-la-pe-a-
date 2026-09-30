import { useCallback, useEffect, useState } from 'react'
import { api } from './cliente.js'

/** Carga datos de la API. Si la ruta es null, no carga nada. */
export function useDatos(ruta) {
  const [estado, setEstado] = useState({ datos: null, error: null, cargando: ruta !== null })

  const recargar = useCallback(async () => {
    if (ruta === null) return
    try {
      const datos = await api(ruta)
      setEstado({ datos, error: null, cargando: false })
    } catch (e) {
      setEstado((anterior) => ({ ...anterior, error: e.message, cargando: false }))
    }
  }, [ruta])

  useEffect(() => {
    recargar()
  }, [recargar])

  return { ...estado, recargar }
}
