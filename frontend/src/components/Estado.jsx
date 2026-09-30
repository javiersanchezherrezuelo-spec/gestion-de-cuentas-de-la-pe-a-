/** Muestra "Cargando…" o el error de una carga de datos. Devuelve null si ya hay datos. */
export default function Estado({ cargando, error, onReintentar }) {
  if (error) {
    return (
      <div className="aviso aviso-error">
        <p>{error}</p>
        {onReintentar && (
          <button className="boton boton-secundario" onClick={onReintentar}>
            Reintentar
          </button>
        )}
      </div>
    )
  }
  if (cargando) return <p className="cargando">Cargando…</p>
  return null
}
