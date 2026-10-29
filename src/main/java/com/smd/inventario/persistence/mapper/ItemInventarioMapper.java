package com.smd.inventario.persistence.mapper;

import com.smd.inventario.domain.composite.Colchon;
import com.smd.inventario.domain.composite.Lote;
import com.smd.inventario.domain.model.ItemInventario;
import com.smd.inventario.persistence.entity.ItemInventarioEntity;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Traduce entre la entidad JPA y el modelo de dominio.
 */
@Component
public class ItemInventarioMapper {

    public Colchon aColchon(ItemInventarioEntity entidad) {
        return new Colchon(
                entidad.getCodigo(),
                entidad.getEstado(),
                entidad.getFechaProduccion());
    }

    public Lote aLote(ItemInventarioEntity entidad, List<ItemInventario> hijos) {
        return Lote.reconstruir(
                entidad.getCodigo(),
                entidad.getEstado(),
                entidad.getFechaProduccion(),
                hijos);
    }
}
