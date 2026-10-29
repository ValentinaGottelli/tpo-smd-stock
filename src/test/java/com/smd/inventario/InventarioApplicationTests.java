package com.smd.inventario;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifica que el contexto de Spring levante y que la aplicación pueda leer
 * desde la base de datos (perfil dev con H2 en memoria).
 */
@SpringBootTest
@AutoConfigureMockMvc
class InventarioApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void elContextoDeSpringCarga() {
        // Si falla la configuración de datasource o JPA, este test no pasa.
    }

    @Test
    void elHealthRespondeOkYLeeLosDatosDeEjemplo() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("OK"))
                .andExpect(jsonPath("$.baseDeDatos").value("H2 (memoria)"))
                .andExpect(jsonPath("$.itemsRegistrados").value(3));
    }
}
