package com.smd.inventario.presentation.controller;

import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Solo para la demo: devuelve las filas crudas de las dos tablas para que el
 * front muestre cómo cambia la base. No forma parte del flujo de negocio.
 */
@RestController
@RequestMapping("/api/demo/tablas")
public class DemoBaseDatosController {

    private final JdbcTemplate jdbc;

    public DemoBaseDatosController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping
    public Map<String, List<Map<String, Object>>> tablas() {
        return Map.of(
                "item_inventario", jdbc.queryForList("select * from item_inventario order by id desc"),
                "movimiento", jdbc.queryForList("select * from movimiento order by id desc limit 30"));
    }
}
