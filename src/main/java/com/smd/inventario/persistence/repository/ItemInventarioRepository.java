package com.smd.inventario.persistence.repository;

import com.smd.inventario.domain.model.EstadoProducto;
import com.smd.inventario.domain.model.TipoItem;
import com.smd.inventario.persistence.entity.ItemInventarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio de ítems de inventario.
 *
 * <p>En la versión completa, la capa de dominio accede a la persistencia a
 * través de una interfaz propia (por ejemplo {@code RepositorioItems}) y esta
 * interfaz queda como implementación concreta con Spring Data JPA.</p>
 */
public interface ItemInventarioRepository extends JpaRepository<ItemInventarioEntity, Long> {

    Optional<ItemInventarioEntity> findByCodigo(String codigo);

    List<ItemInventarioEntity> findByEstado(EstadoProducto estado);

    List<ItemInventarioEntity> findByTipo(TipoItem tipo);

    long countByEstado(EstadoProducto estado);
}
