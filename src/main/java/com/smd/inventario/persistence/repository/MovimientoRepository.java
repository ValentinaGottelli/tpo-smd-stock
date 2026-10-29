package com.smd.inventario.persistence.repository;

import com.smd.inventario.persistence.entity.MovimientoEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repositorio Spring Data de movimientos de auditoría.
 */
public interface MovimientoRepository extends JpaRepository<MovimientoEntity, Long> {

    List<MovimientoEntity> findAllByOrderByFechaHoraDesc(Pageable pageable);
}
