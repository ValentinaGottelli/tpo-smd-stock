package com.smd.inventario.persistence.repository;

import com.smd.inventario.domain.model.EstadoProducto;
import com.smd.inventario.domain.model.TipoItem;
import com.smd.inventario.persistence.entity.ItemInventarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio Spring Data de ítems de inventario.
 *
 * <p>Es la implementación concreta que usa {@link JpaRepositorioItems} para
 * cumplir el puerto {@code domain.port.RepositorioItems}.</p>
 */
public interface ItemInventarioRepository extends JpaRepository<ItemInventarioEntity, Long> {

    Optional<ItemInventarioEntity> findByCodigo(String codigo);

    List<ItemInventarioEntity> findByEstado(EstadoProducto estado);

    List<ItemInventarioEntity> findByTipo(TipoItem tipo);

    long countByEstado(EstadoProducto estado);

    List<ItemInventarioEntity> findByCodigoPadreIsNullOrderByCodigoAsc();

    List<ItemInventarioEntity> findByCodigoPadreIsNullAndEstadoOrderByCodigoAsc(EstadoProducto estado);

    List<ItemInventarioEntity> findByCodigoPadreOrderByCodigoAsc(String codigoPadre);

    Optional<ItemInventarioEntity> findFirstByCodigoPadreIsNullAndEstadoOrderByFechaProduccionAscCodigoAsc(
            EstadoProducto estado);

    long countByCodigoPadreIsNull();
}
