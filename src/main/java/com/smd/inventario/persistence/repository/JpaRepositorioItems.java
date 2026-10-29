package com.smd.inventario.persistence.repository;

import com.smd.inventario.domain.composite.Lote;
import com.smd.inventario.domain.model.EstadoProducto;
import com.smd.inventario.domain.model.ItemInventario;
import com.smd.inventario.domain.model.TipoItem;
import com.smd.inventario.domain.port.RepositorioItems;
import com.smd.inventario.persistence.entity.ItemInventarioEntity;
import com.smd.inventario.persistence.mapper.ItemInventarioMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Adaptador JPA del puerto {@link RepositorioItems}.
 *
 * <p>Reconstruye los lotes leyendo recursivamente las filas cuyo
 * {@code codigo_padre} apunta al lote. El dominio nunca ve una entidad.</p>
 */
@Component
public class JpaRepositorioItems implements RepositorioItems {

    private final ItemInventarioRepository repository;
    private final ItemInventarioMapper mapper;

    public JpaRepositorioItems(ItemInventarioRepository repository,
                               ItemInventarioMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Optional<ItemInventario> buscarPorCodigo(String codigo) {
        return repository.findByCodigo(codigo).map(this::armar);
    }

    @Override
    public List<ItemInventario> listarPorEstado(EstadoProducto estado) {
        return repository.findByCodigoPadreIsNullAndEstadoOrderByCodigoAsc(estado).stream()
                .map(this::armar)
                .toList();
    }

    @Override
    public Optional<ItemInventario> buscarMasAntiguoEnDeposito() {
        return repository
                .findFirstByCodigoPadreIsNullAndEstadoOrderByFechaProduccionAscCodigoAsc(
                        EstadoProducto.EN_DEPOSITO)
                .map(this::armar);
    }

    @Override
    public List<ItemInventario> listarTodos() {
        return repository.findByCodigoPadreIsNullOrderByCodigoAsc().stream()
                .map(this::armar)
                .toList();
    }

    @Override
    @Transactional
    public void guardar(ItemInventario item) {
        guardar(item, null);
    }

    private void guardar(ItemInventario item, String codigoPadre) {
        ItemInventarioEntity entidad = repository.findByCodigo(item.getCodigo())
                .orElseGet(() -> new ItemInventarioEntity(
                        item.getCodigo(),
                        tipoDe(item),
                        item.getEstado(),
                        item.getCantidad(),
                        item.getFechaProduccion(),
                        codigoPadre));

        String padre = codigoPadre != null ? codigoPadre : entidad.getCodigoPadre();
        entidad.actualizar(item.getEstado(), item.getCantidad(), item.getFechaProduccion(), padre);
        repository.save(entidad);

        if (item instanceof Lote lote) {
            for (ItemInventario hijo : lote.getItems()) {
                guardar(hijo, lote.getCodigo());
            }
        }
    }

    private ItemInventario armar(ItemInventarioEntity entidad) {
        if (entidad.getTipo() == TipoItem.COLCHON) {
            return mapper.aColchon(entidad);
        }

        List<ItemInventario> hijos = repository
                .findByCodigoPadreOrderByCodigoAsc(entidad.getCodigo()).stream()
                .map(this::armar)
                .toList();
        return mapper.aLote(entidad, hijos);
    }

    private TipoItem tipoDe(ItemInventario item) {
        return item instanceof Lote ? TipoItem.LOTE : TipoItem.COLCHON;
    }
}
