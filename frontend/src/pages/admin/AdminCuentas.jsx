import { useState } from 'react'
import { api } from '../../api/cliente.js'
import { useDatos } from '../../api/useDatos.js'
import Estado from '../../components/Estado.jsx'
import Modal from '../../components/Modal.jsx'
import { useToast } from '../../context/ToastContext.jsx'
import { formatoEuros } from '../../formato.js'
import { ListaConsumos, ListaPagos } from '../MiCuenta.jsx'
import { Importe } from '../Resumen.jsx'

export default function AdminCuentas() {
  const { datos, error, cargando, recargar } = useDatos('/resumen')
  const [abierta, setAbierta] = useState(null)

  return (
    <>
      <Estado cargando={cargando && !datos} error={error} onReintentar={recargar} />
      {datos && (
        <div className="tarjeta">
          <div className="tarjeta-cabecera">
            <h2>Quién debe qué</h2>
            <span className="total">Total {formatoEuros(datos.totalDeuda)}</span>
          </div>
          <p className="texto-suave texto-pequeno">Toca a alguien para apuntar un pago o corregir sus apuntes.</p>
          <ul className="lista">
            {datos.deudas.map((d) => (
              <li key={d.usuarioId}>
                <button className="fila fila-boton" onClick={() => setAbierta(d.usuarioId)}>
                  <span>{d.nombre}</span>
                  <Importe deuda={d.deuda} />
                </button>
              </li>
            ))}
          </ul>
        </div>
      )}
      {abierta && (
        <CuentaDeUsuario
          usuarioId={abierta}
          onCambio={recargar}
          onCerrar={() => setAbierta(null)}
        />
      )}
    </>
  )
}

function CuentaDeUsuario({ usuarioId, onCambio, onCerrar }) {
  const toast = useToast()
  const { datos: cuenta, error, cargando, recargar } = useDatos(`/admin/usuarios/${usuarioId}/cuenta`)
  const [importe, setImporte] = useState('')
  const [nota, setNota] = useState('')
  const [errorPago, setErrorPago] = useState(null)

  function actualizar() {
    recargar()
    onCambio()
  }

  async function registrarPago(e) {
    e.preventDefault()
    setErrorPago(null)
    try {
      await api('/admin/pagos', {
        metodo: 'POST',
        cuerpo: { usuarioId, importe: importe.replace(',', '.'), nota },
      })
      toast(`Pago de ${formatoEuros(importe.replace(',', '.'))} apuntado`)
      setImporte('')
      setNota('')
      actualizar()
    } catch (err) {
      setErrorPago(err.message)
    }
  }

  async function anular(consumo) {
    if (!confirm(`¿Anular ${consumo.cantidad} × ${consumo.producto}? Las unidades vuelven al stock.`)) return
    try {
      await api(`/consumos/${consumo.id}`, { metodo: 'DELETE' })
      toast('Apunte anulado')
      actualizar()
    } catch (err) {
      toast(err.message, { tipo: 'error' })
    }
  }

  async function borrarPago(pago) {
    if (!confirm(`¿Borrar el pago de ${formatoEuros(pago.importe)}?`)) return
    try {
      await api(`/admin/pagos/${pago.id}`, { metodo: 'DELETE' })
      toast('Pago borrado')
      actualizar()
    } catch (err) {
      toast(err.message, { tipo: 'error' })
    }
  }

  return (
    <Modal titulo={cuenta ? cuenta.usuario.nombre : 'Cuenta'} onCerrar={onCerrar}>
      <Estado cargando={cargando && !cuenta} error={error} onReintentar={recargar} />
      {cuenta && (
        <>
          <div className="tarjeta-deuda">
            <span className="texto-suave">Debe ahora</span>
            <span className="deuda-grande">
              <Importe deuda={cuenta.deuda} />
            </span>
          </div>

          <form className="formulario" onSubmit={registrarPago}>
            <h3>Apuntar un pago</h3>
            <div className="campos-en-fila">
              <label className="campo">
                <span>Importe (€)</span>
                <input
                  value={importe}
                  onChange={(e) => setImporte(e.target.value)}
                  inputMode="decimal"
                  placeholder={Number(cuenta.deuda) > 0 ? String(cuenta.deuda).replace('.', ',') : '0,00'}
                  required
                />
              </label>
              <label className="campo">
                <span>Nota (opcional)</span>
                <input value={nota} onChange={(e) => setNota(e.target.value)} placeholder="Bizum" maxLength={200} />
              </label>
            </div>
            {Number(cuenta.deuda) > 0 && (
              <button
                type="button"
                className="boton-enlace"
                onClick={() => setImporte(String(cuenta.deuda).replace('.', ','))}
              >
                Paga todo ({formatoEuros(cuenta.deuda)})
              </button>
            )}
            {errorPago && <p className="error-formulario">{errorPago}</p>}
            <button className="boton boton-principal boton-ancho">Apuntar pago</button>
          </form>

          <h3>Apuntes</h3>
          <ListaConsumos consumos={cuenta.consumos} onAnular={anular} />
          <h3>Pagos</h3>
          <ListaPagos pagos={cuenta.pagos} onBorrar={borrarPago} />
        </>
      )}
    </Modal>
  )
}
