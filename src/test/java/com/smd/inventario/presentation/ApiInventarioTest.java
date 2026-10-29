package com.smd.inventario.presentation;

import com.smd.inventario.persistence.repository.ItemInventarioRepository;
import com.smd.inventario.persistence.repository.MovimientoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prueba de integración de la API: alta de inventario, operaciones de escaneo,
 * cascada del lote, auditoría y traducción de errores a HTTP.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("API de inventario")
class ApiInventarioTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ItemInventarioRepository itemInventarioRepository;

    @Autowired
    private MovimientoRepository movimientoRepository;

    @BeforeEach
    void limpiarBase() {
        movimientoRepository.deleteAll();
        itemInventarioRepository.deleteAll();
    }

    @Test
    void recorreElCicloDeVidaDeUnColchon() throws Exception {
        crearColchon("COLCHON-API-1");

        mockMvc.perform(escaneo("/ingreso", "COLCHON-API-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estadoAnterior").value("PRODUCIDO"))
                .andExpect(jsonPath("$.estadoNuevo").value("EN_DEPOSITO"))
                .andExpect(jsonPath("$.cantidad").value(1))
                .andExpect(jsonPath("$.mensaje").value("Ingreso registrado para COLCHON-API-1"));

        mockMvc.perform(escaneo("/reserva", "COLCHON-API-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estadoNuevo").value("RESERVADO"));

        mockMvc.perform(escaneo("/despacho", "COLCHON-API-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estadoNuevo").value("DESPACHADO"));

        mockMvc.perform(get("/api/inventario/COLCHON-API-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("COLCHON"))
                .andExpect(jsonPath("$.estado").value("DESPACHADO"));
    }

    @Test
    void rechazaUnaTransicionInvalidaCon409() throws Exception {
        crearColchon("COLCHON-API-2");
        mockMvc.perform(escaneo("/ingreso", "COLCHON-API-2"));
        mockMvc.perform(escaneo("/reserva", "COLCHON-API-2"));
        mockMvc.perform(escaneo("/despacho", "COLCHON-API-2"));

        mockMvc.perform(escaneo("/reserva", "COLCHON-API-2"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("TRANSICION_INVALIDA"))
                .andExpect(jsonPath("$.path").value("/api/escaneos/reserva"));

        mockMvc.perform(get("/api/inventario/COLCHON-API-2"))
                .andExpect(jsonPath("$.estado").value("DESPACHADO"));
    }

    @Test
    void despacharUnLoteCambiaElEstadoDeTodosSusHijos() throws Exception {
        mockMvc.perform(post("/api/inventario/lotes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"codigo":"LOTE-API-1","estado":"PRODUCIDO","cantidad":5}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipo").value("LOTE"))
                .andExpect(jsonPath("$.cantidad").value(5))
                .andExpect(jsonPath("$.hijos.length()").value(5));

        mockMvc.perform(escaneo("/ingreso", "LOTE-API-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cantidad").value(5));
        mockMvc.perform(escaneo("/reserva", "LOTE-API-1"));
        mockMvc.perform(escaneo("/despacho", "LOTE-API-1"));

        mockMvc.perform(get("/api/inventario/LOTE-API-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("DESPACHADO"))
                .andExpect(jsonPath("$.cantidad").value(5))
                .andExpect(jsonPath("$.hijos.length()").value(5))
                .andExpect(jsonPath("$.hijos[*].estado", everyItem(is("DESPACHADO"))));
    }

    @Test
    void registraLaAuditoriaDeCadaOperacion() throws Exception {
        crearColchon("COLCHON-API-3");
        mockMvc.perform(escaneo("/ingreso", "COLCHON-API-3"));
        mockMvc.perform(escaneo("/reserva", "COLCHON-API-3"));
        mockMvc.perform(escaneo("/despacho", "COLCHON-API-3"));

        mockMvc.perform(get("/api/auditoria").param("limite", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].tipo").value("DESPACHO"))
                .andExpect(jsonPath("$[0].codigoItem").value("COLCHON-API-3"))
                .andExpect(jsonPath("$[0].usuario").value("tester"))
                .andExpect(jsonPath("$[0].estadoAnterior").value("RESERVADO"))
                .andExpect(jsonPath("$[0].estadoNuevo").value("DESPACHADO"));
    }

    @Test
    void devuelve404SiElItemNoExiste() throws Exception {
        mockMvc.perform(escaneo("/reserva", "NO-EXISTE"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("RECURSO_NO_ENCONTRADO"));
    }

    @Test
    void rechazaUnBodySinCodigoCon400() throws Exception {
        mockMvc.perform(post("/api/escaneos/ingreso")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"usuario":"tester"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("SOLICITUD_INVALIDA"))
                .andExpect(jsonPath("$.mensaje").value("codigo: El codigo es obligatorio"));
    }

    @Test
    void rechazaUnCodigoDuplicadoCon409() throws Exception {
        crearColchon("COLCHON-API-4");

        mockMvc.perform(post("/api/inventario/colchones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"codigo":"COLCHON-API-4"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("REGLA_DE_NEGOCIO"));
    }

    @Test
    void listaElInventarioFiltrandoPorEstado() throws Exception {
        crearColchon("COLCHON-API-5");
        mockMvc.perform(post("/api/inventario/lotes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"codigo":"LOTE-API-5","cantidad":3}
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/inventario").param("estado", "PRODUCIDO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].codigo").value("COLCHON-API-5"))
                .andExpect(jsonPath("$[1].codigo").value("LOTE-API-5"))
                .andExpect(jsonPath("$[1].cantidad").value(3));
    }

    private void crearColchon(String codigo) throws Exception {
        mockMvc.perform(post("/api/inventario/colchones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"codigo":"%s","estado":"PRODUCIDO"}
                                """.formatted(codigo)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipo").value("COLCHON"))
                .andExpect(jsonPath("$.cantidad").value(1));
    }

    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder escaneo(
            String operacion, String codigo) {
        return post("/api/escaneos" + operacion)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"codigo":"%s","usuario":"tester"}
                        """.formatted(codigo));
    }
}
