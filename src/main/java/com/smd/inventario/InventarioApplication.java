package com.smd.inventario;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada del sistema de control de stock de SMD Colchon Modular S.A.
 *
 * <p>Arquitectura monolítica en capas:</p>
 * <ul>
 *   <li>{@code presentation}: controladores REST, DTOs y adaptadores de dispositivos de lectura.</li>
 *   <li>{@code domain}: modelo, reglas de negocio y patrones de diseño del TP.</li>
 *   <li>{@code persistence}: entidades JPA, repositorios y mapeo objeto-relacional.</li>
 * </ul>
 */
@SpringBootApplication
public class InventarioApplication {

    public static void main(String[] args) {
        SpringApplication.run(InventarioApplication.class, args);
    }
}
