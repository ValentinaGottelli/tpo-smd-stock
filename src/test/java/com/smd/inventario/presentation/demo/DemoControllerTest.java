package com.smd.inventario.presentation.demo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifica el flujo HTTP de la demo: ciclo de vida, cascada del lote y
 * traducción de las excepciones de dominio a códigos HTTP.
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("DemoController: flujo por HTTP")
class DemoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private InventarioDemo inventarioDemo;

    @BeforeEach
    void limpiarInventario() {
        inventarioDemo.reiniciar();
    }

    @Test
    void elCicloDeVidaCompletoCambiaElEstadoPasoAPaso() throws Exception {
        crearColchon("COLCHON-1");

        mockMvc.perform(post("/api/demo/items/COLCHON-1/ingresar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("EN_DEPOSITO"));

        mockMvc.perform(post("/api/demo/items/COLCHON-1/reservar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("RESERVADO"));

        mockMvc.perform(post("/api/demo/items/COLCHON-1/despachar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("DESPACHADO"));
    }

    @Test
    void reservarUnItemDespachadoDevuelve409() throws Exception {
        crearColchon("COLCHON-1");
        mockMvc.perform(post("/api/demo/items/COLCHON-1/ingresar"));
        mockMvc.perform(post("/api/demo/items/COLCHON-1/reservar"));
        mockMvc.perform(post("/api/demo/items/COLCHON-1/despachar"));

        mockMvc.perform(post("/api/demo/items/COLCHON-1/reservar"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("TRANSICION_INVALIDA"));
    }

    @Test
    void despacharElLoteCambiaElEstadoDeTodosLosHijos() throws Exception {
        mockMvc.perform(post("/api/demo/lotes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"codigo":"LOTE-1","estado":"PRODUCIDO","cantidad":5}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("LOTE"))
                .andExpect(jsonPath("$.cantidad").value(5))
                .andExpect(jsonPath("$.hijos.length()").value(5));

        mockMvc.perform(post("/api/demo/items/LOTE-1/ingresar"))
                .andExpect(jsonPath("$.estado").value("EN_DEPOSITO"));
        mockMvc.perform(post("/api/demo/items/LOTE-1/reservar"))
                .andExpect(jsonPath("$.estado").value("RESERVADO"));
        mockMvc.perform(post("/api/demo/items/LOTE-1/despachar"))
                .andExpect(jsonPath("$.estado").value("DESPACHADO"));

        mockMvc.perform(get("/api/demo/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].estado").value("DESPACHADO"))
                .andExpect(jsonPath("$[0].hijos[*].estado")
                        .value(org.hamcrest.Matchers.everyItem(
                                org.hamcrest.Matchers.is("DESPACHADO"))));
    }

    @Test
    void unItemInexistenteDevuelve404() throws Exception {
        mockMvc.perform(post("/api/demo/items/NO-EXISTE/reservar"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("RECURSO_NO_ENCONTRADO"));
    }

    @Test
    void unCodigoDuplicadoDevuelve400() throws Exception {
        crearColchon("COLCHON-1");

        mockMvc.perform(post("/api/demo/colchones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"codigo":"COLCHON-1","estado":"PRODUCIDO"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("SOLICITUD_INVALIDA"));
    }

    private void crearColchon(String codigo) throws Exception {
        mockMvc.perform(post("/api/demo/colchones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"codigo":"%s","estado":"PRODUCIDO"}
                                """.formatted(codigo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cantidad").value(1));
    }
}
