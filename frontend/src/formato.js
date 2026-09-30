const euros = new Intl.NumberFormat('es-ES', { style: 'currency', currency: 'EUR' })
const fechaHora = new Intl.DateTimeFormat('es-ES', {
  day: 'numeric',
  month: 'short',
  hour: '2-digit',
  minute: '2-digit',
})

export const formatoEuros = (numero) => euros.format(Number(numero))
export const formatoFecha = (iso) => fechaHora.format(new Date(iso))
