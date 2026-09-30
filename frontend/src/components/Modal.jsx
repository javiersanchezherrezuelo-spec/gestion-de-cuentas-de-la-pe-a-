import { useEffect } from 'react'

/** Ventana que en el móvil sube desde abajo y en escritorio aparece centrada. */
export default function Modal({ titulo, onCerrar, children }) {
  useEffect(() => {
    const alPulsarTecla = (e) => e.key === 'Escape' && onCerrar()
    document.addEventListener('keydown', alPulsarTecla)
    return () => document.removeEventListener('keydown', alPulsarTecla)
  }, [onCerrar])

  return (
    <div className="modal-fondo" onClick={onCerrar}>
      <div className="modal" role="dialog" aria-modal="true" aria-label={titulo} onClick={(e) => e.stopPropagation()}>
        <div className="modal-cabecera">
          <h2>{titulo}</h2>
          <button className="boton-icono" onClick={onCerrar} aria-label="Cerrar">
            ✕
          </button>
        </div>
        {children}
      </div>
    </div>
  )
}
