package com.smd.inventario.presentation.dto;

import com.smd.inventario.domain.composite.Lote;
import com.smd.inventario.domain.model.EstadoProducto;
import com.smd.inventario.domain.model.ItemInventario;
import com.smd.inventario.domain.model.TipoItem;

import java.time.LocalDate;
import java.util.List;

/**
 * Representación HTTP de un ítem. Los lotes incluyen el árbol de hijos.
 */
public record ItemInventarioResponse(
        String codigo,
        TipoItem tipo,
        EstadoProducto estado,
        int cantidad,
        LocalDate fechaProduccion,
        List<ItemInventarioResponse> hijos
) {

    public static ItemInventarioResponse desde(ItemInventario item) {
        List<ItemInventarioResponse> hijos = item instanceof Lote lote
                ? lote.getItems().stream().map(ItemInventarioResponse::desde).toList()
                : List.of();

        return new ItemInventarioResponse(
                item.getCodigo(),
                item instanceof Lote ? TipoItem.LOTE : TipoItem.COLCHON,
                item.getEstado(),
                item.getCantidad(),
                item.getFechaProduccion(),
                hijos);
    }
}
