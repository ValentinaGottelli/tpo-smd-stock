package com.smd.inventario.presentation.dto;

import com.smd.inventario.domain.model.EstadoProducto;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

/**
 * Alta de un lote y de sus colchones hijos.
 *
 * @param cantidad número de colchones; si se omite se usan 5
 */
public record RegistrarLoteRequest(
        @NotBlank(message = "El codigo es obligatorio") String codigo,
        EstadoProducto estado,
        LocalDate fechaProduccion,
        @Min(value = 1, message = "La cantidad debe ser mayor a cero") Integer cantidad
) { }
