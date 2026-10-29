package com.smd.inventario.config;

import com.smd.inventario.domain.facade.FachadaEscaneo;
import com.smd.inventario.domain.facade.FachadaEscaneoImpl;
import com.smd.inventario.domain.port.RepositorioItems;
import com.smd.inventario.domain.port.RepositorioMovimientos;
import com.smd.inventario.domain.service.GestorDeStock;
import com.smd.inventario.domain.service.ServicioConsultas;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cablea los servicios puros del dominio como beans de Spring.
 *
 * <p>El dominio no lleva anotaciones: esta clase es la que decide cómo se
 * construyen sus objetos y garantiza una única instancia de cada servicio
 * (Singleton) durante la vida de la aplicación.</p>
 */
@Configuration
public class DomainConfig {

    @Bean
    public GestorDeStock gestorDeStock(RepositorioItems repositorioItems,
                                       RepositorioMovimientos repositorioMovimientos) {
        return new GestorDeStock(repositorioItems, repositorioMovimientos);
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
