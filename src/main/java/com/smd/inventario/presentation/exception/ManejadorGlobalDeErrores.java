package com.smd.inventario.presentation.exception;

import com.smd.inventario.domain.exception.ExcepcionDeNegocio;
import com.smd.inventario.domain.exception.RecursoNoEncontradoException;
import com.smd.inventario.domain.exception.ReglaDeNegocioException;
import com.smd.inventario.domain.exception.TransicionInvalidaException;
import com.smd.inventario.presentation.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * Traduce las excepciones del dominio y de validación a respuestas HTTP.
 *
 * <p>Es el único lugar de la API que conoce los códigos de estado: los
 * controllers y el dominio no repiten esa lógica.</p>
 */
@RestControllerAdvice
public class ManejadorGlobalDeErrores {

    @ExceptionHandler(RecursoNoEncontradoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse noEncontrado(RecursoNoEncontradoException e,
                                      HttpServletRequest request) {
        return error("RECURSO_NO_ENCONTRADO", e.getMessage(), request);
    }

    @ExceptionHandler(ExcepcionDeNegocio.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse negocio(ExcepcionDeNegocio e, HttpServletRequest request) {
        return error(codigoDeNegocio(e), e.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse validacion(MethodArgumentNotValidException e,
                                    HttpServletRequest request) {
        String mensaje = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return error("SOLICITUD_INVALIDA", mensaje, request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse tipoInvalido(MethodArgumentTypeMismatchException e,
                                      HttpServletRequest request) {
        String mensaje = "El valor '" + e.getValue()
                + "' no es valido para '" + e.getName() + "'";
        return error("SOLICITUD_INVALIDA", mensaje, request);
    }

    @ExceptionHandler({IllegalArgumentException.class, HttpMessageNotReadableException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse solicitudInvalida(Exception e, HttpServletRequest request) {
        String mensaje = e instanceof HttpMessageNotReadableException
                ? "El cuerpo de la solicitud no es valido"
                : e.getMessage();
        return error("SOLICITUD_INVALIDA", mensaje, request);
    }

    private String codigoDeNegocio(ExcepcionDeNegocio e) {
        if (e instanceof TransicionInvalidaException) {
            return "TRANSICION_INVALIDA";
        }
        if (e instanceof ReglaDeNegocioException) {
            return "REGLA_DE_NEGOCIO";
        }
        return "ERROR_DE_NEGOCIO";
    }

    private ErrorResponse error(String codigo, String mensaje, HttpServletRequest request) {
        return new ErrorResponse(codigo, mensaje, LocalDateTime.now(), request.getRequestURI());
    }
}
