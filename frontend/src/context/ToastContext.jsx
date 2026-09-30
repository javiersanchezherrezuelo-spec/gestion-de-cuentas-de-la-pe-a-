import { createContext, useCallback, useContext, useEffect, useRef, useState } from 'react'

const ToastContext = createContext(null)

/** Mensajes cortos abajo de la pantalla, opcionalmente con un botón (por ejemplo "Deshacer"). */
export function ToastProvider({ children }) {
  const [toast, setToast] = useState(null)
  const temporizador = useRef(null)

  const cerrar = useCallback(() => setToast(null), [])

  const mostrar = useCallback((texto, { accion, tipo = 'ok', segundos = 5 } = {}) => {
    clearTimeout(temporizador.current)
    setToast({ texto, accion, tipo, id: Date.now() })
    temporizador.current = setTimeout(() => setToast(null), segundos * 1000)
  }, [])

  useEffect(() => () => clearTimeout(temporizador.current), [])

  return (
    <ToastContext.Provider value={mostrar}>
      {children}
      {toast && (
        <div className={`toast toast-${toast.tipo}`} role="status" key={toast.id}>
          <span>{toast.texto}</span>
          {toast.accion && (
            <button
              className="toast-accion"
              onClick={() => {
                cerrar()
                toast.accion.alPulsar()
              }}
            >
              {toast.accion.texto}
            </button>
          )}
        </div>
      )}
    </ToastContext.Provider>
  )
}

// eslint-disable-next-line react/only-export-components
export const useToast = () => useContext(ToastContext)
