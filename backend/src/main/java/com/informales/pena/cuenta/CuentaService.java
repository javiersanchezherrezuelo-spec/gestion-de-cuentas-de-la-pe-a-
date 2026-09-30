package com.informales.pena.cuenta;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.informales.pena.consumo.ConsumoDto;
import com.informales.pena.consumo.ConsumoRepository;
import com.informales.pena.consumo.ConsumoService;
import com.informales.pena.pago.PagoDto;
import com.informales.pena.pago.PagoRepository;
import com.informales.pena.usuario.Usuario;
import com.informales.pena.usuario.UsuarioDto;
import com.informales.pena.usuario.UsuarioRepository;
import com.informales.pena.usuario.UsuarioService;

/** Calcula deudas: lo consumido (sin anulados) menos lo pagado. */
@Service
@Transactional(readOnly = true)
public class CuentaService {

    private final ConsumoRepository consumos;
    private final PagoRepository pagos;
    private final UsuarioRepository usuarios;
    private final UsuarioService usuarioService;
    private final ConsumoService consumoService;

    public CuentaService(ConsumoRepository consumos, PagoRepository pagos, UsuarioRepository usuarios,
                         UsuarioService usuarioService, ConsumoService consumoService) {
        this.consumos = consumos;
        this.pagos = pagos;
        this.usuarios = usuarios;
        this.usuarioService = usuarioService;
        this.consumoService = consumoService;
    }

    public BigDecimal deudaDe(Long usuarioId) {
        return cero(consumos.totalDe(usuarioId)).subtract(cero(pagos.totalDe(usuarioId)));
    }

    public CuentaDto cuentaDe(Long usuarioId) {
        Usuario u = usuarioService.buscar(usuarioId);
        BigDecimal consumido = cero(consumos.totalDe(usuarioId));
        BigDecimal pagado = cero(pagos.totalDe(usuarioId));
        long minutos = consumoService.getMinutosDeshacer();
        List<ConsumoDto> ultimosConsumos = consumos.findTop100ByUsuarioIdOrderByFechaDesc(usuarioId).stream()
                .map(c -> ConsumoDto.de(c, minutos)).toList();
        List<PagoDto> ultimosPagos = pagos.findTop50ByUsuarioIdOrderByFechaDesc(usuarioId).stream()
                .map(PagoDto::de).toList();
        return new CuentaDto(UsuarioDto.de(u), consumido, pagado, consumido.subtract(pagado), ultimosConsumos, ultimosPagos);
    }

    /** Deudas de todos los usuarios activos (y de los desactivados que aún deban algo), de mayor a menor. */
    public List<DeudaDto> deudas() {
        Map<Long, BigDecimal> consumido = aMapa(consumos.totalesPorUsuario());
        Map<Long, BigDecimal> pagado = aMapa(pagos.totalesPorUsuario());
        return usuarios.findAllByOrderByNombreAsc().stream()
                .map(u -> {
                    BigDecimal deuda = consumido.getOrDefault(u.getId(), BigDecimal.ZERO)
                            .subtract(pagado.getOrDefault(u.getId(), BigDecimal.ZERO));
                    return u.isActivo() || deuda.signum() != 0 ? new DeudaDto(u.getId(), u.getNombre(), deuda) : null;
                })
                .filter(d -> d != null)
                .sorted(Comparator.comparing(DeudaDto::deuda).reversed())
                .toList();
    }

    private static Map<Long, BigDecimal> aMapa(List<TotalPorUsuario> totales) {
        return totales.stream().collect(Collectors.toMap(TotalPorUsuario::usuarioId, TotalPorUsuario::total));
    }

    private static BigDecimal cero(BigDecimal valor) {
        return valor != null ? valor : BigDecimal.ZERO;
    }
}
