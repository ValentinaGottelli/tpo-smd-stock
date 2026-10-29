package com.smd.inventario.persistence.repository;

import com.smd.inventario.domain.model.Movimiento;
import com.smd.inventario.domain.port.RepositorioMovimientos;
import com.smd.inventario.persistence.mapper.MovimientoMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Adaptador JPA del puerto {@link RepositorioMovimientos}.
 */
@Component
public class JpaRepositorioMovimientos implements RepositorioMovimientos {

    private final MovimientoRepository repository;

    public JpaRepositorioMovimientos(MovimientoRepository repository) {
        this.repository = repository;
    }

    @Override
    public void guardar(Movimiento movimiento) {
        repository.save(MovimientoMapper.aEntidad(movimiento));
    }

    @Override
    public List<Movimiento> ultimos(int limite) {
        int cantidad = Math.max(1, limite);
        return repository.findAllByOrderByFechaHoraDesc(PageRequest.of(0, cantidad)).stream()
                .map(MovimientoMapper::aDominio)
                .toList();
    }
}
