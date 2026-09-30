package com.informales.pena.cuenta;

import java.math.BigDecimal;
import java.util.List;

import com.informales.pena.consumo.ConsumoDto;
import com.informales.pena.pago.PagoDto;
import com.informales.pena.usuario.UsuarioDto;

public record CuentaDto(UsuarioDto usuario, BigDecimal consumido, BigDecimal pagado, BigDecimal deuda,
                        List<ConsumoDto> consumos, List<PagoDto> pagos) {
}
