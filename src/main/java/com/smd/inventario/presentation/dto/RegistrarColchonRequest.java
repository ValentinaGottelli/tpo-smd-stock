package com.smd.inventario.presentation.dto;

import com.smd.inventario.domain.model.EstadoProducto;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

/**
 * Alta de un colchón suelto en el inventario.
 */
public record RegistrarColchonRequest(
        @NotBlank(message = "El codigo es obligatorio") String codigo,
        EstadoProducto estado,
        LocalDate fechaProduccion
) { }
