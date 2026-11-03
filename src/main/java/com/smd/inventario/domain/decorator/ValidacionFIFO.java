package com.smd.inventario.domain.decorator;

import com.smd.inventario.domain.dto.EscaneoDTO;
import com.smd.inventario.domain.exception.RecursoNoEncontradoException;
import com.smd.inventario.domain.exception.ReglaDeNegocioException;
import com.smd.inventario.domain.model.EstadoProducto;
import com.smd.inventario.domain.model.ItemInventario;
import com.smd.inventario.domain.model.Movimiento;
import com.smd.inventario.domain.port.RepositorioItems;
import java.util.Comparator;

/**
 * Rechaza un despacho si hay otro ítem {@code RESERVADO} más antiguo (FIFO).
 *
 * <p>Reservar es libre; lo que se ordena es la salida: entre los ítems ya
 * reservados se despacha primero el de fecha de producción más antigua. Los que
 * solo están {@code EN_DEPOSITO} no cuentan. En empate de fechas se acepta
 * cualquiera.</p>
 */
public final class ValidacionFIFO extends DecoradorRegistro {

    private final RepositorioItems repositorioItems;

    public ValidacionFIFO(IRegistroMovimiento siguiente, RepositorioItems repositorioItems) {
        super(siguiente);
        this.repositorioItems = repositorioItems;
    }

    @Override
    public void registrar(EscaneoDTO solicitud, Movimiento movimiento) {
        ItemInventario item = repositorioItems.buscarPorCodigo(movimiento.codigoItem())
                .orElseThrow(() -> new RecursoNoEncontradoException(movimiento.codigoItem()));
        repositorioItems.listarPorEstado(EstadoProducto.RESERVADO).stream()
                .filter(otro -> otro.getFechaProduccion().isBefore(item.getFechaProduccion()))
                .min(Comparator.comparing(ItemInventario::getFechaProduccion))
                .ifPresent(masAntiguo -> {
                    throw new ReglaDeNegocioException("FIFO: antes de despachar " + item.getCodigo()
                            + " hay que despachar " + masAntiguo.getCodigo()
                            + " (producido el " + masAntiguo.getFechaProduccion() + ")");
                });
        siguiente.registrar(solicitud, movimiento);
    }
}
