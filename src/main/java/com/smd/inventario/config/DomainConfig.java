package com.smd.inventario.config;

import com.smd.inventario.domain.facade.FachadaEscaneo;
import com.smd.inventario.domain.facade.FachadaEscaneoImpl;
import com.smd.inventario.domain.factory.FabricaMovimiento;
import com.smd.inventario.domain.observer.DashboardAuditoria;
import com.smd.inventario.domain.observer.ModuloVentas;
import com.smd.inventario.domain.observer.ObservadorStock;
import com.smd.inventario.domain.port.RepositorioItems;
import com.smd.inventario.domain.port.RepositorioMovimientos;
import com.smd.inventario.domain.service.GestorDeStock;
import com.smd.inventario.domain.service.ServicioConsultas;
import com.smd.inventario.domain.strategy.SelectorEstrategia;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Cablea los servicios del dominio e inicializa el Singleton puro de GestorDeStock.
 */
@Configuration
public class DomainConfig {

    @Bean
    public FabricaMovimiento fabricaMovimiento(RepositorioItems repositorioItems,
                                               RepositorioMovimientos repositorioMovimientos,
                                               @Value("${app.stock.umbral-minimo:10}")
                                               int umbralMinimo) {
        return new FabricaMovimiento(repositorioMovimientos, repositorioItems, umbralMinimo);
    }

    @Bean
    public SelectorEstrategia selectorEstrategia() {
        return new SelectorEstrategia();
    }

    @Bean
    public ModuloVentas moduloVentas() {
        return new ModuloVentas();
    }

    @Bean
    public DashboardAuditoria dashboardAuditoria() {
        return new DashboardAuditoria();
    }

    @Bean
    public GestorDeStock gestorDeStock(RepositorioItems repositorioItems,
                                       FabricaMovimiento fabricaMovimiento,
                                       SelectorEstrategia selectorEstrategia,
                                       List<ObservadorStock> observadores) {
        // Inicializa el Singleton tradicional GoF (constructor privado + getInstance)
        return GestorDeStock.inicializar(repositorioItems, fabricaMovimiento, selectorEstrategia, observadores);
    }

    @Bean
    public ServicioConsultas servicioConsultas(RepositorioItems repositorioItems,
                                               RepositorioMovimientos repositorioMovimientos) {
        return new ServicioConsultas(repositorioItems, repositorioMovimientos);
    }

    @Bean
    public FachadaEscaneo fachadaEscaneo(GestorDeStock gestorDeStock,
                                         @Value("${app.escaneo.usuario-default:operario01}")
                                         String usuarioDefault) {
        return new FachadaEscaneoImpl(gestorDeStock, usuarioDefault);
    }
}
