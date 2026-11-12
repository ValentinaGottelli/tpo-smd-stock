package com.smd.inventario.config;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Abre el front en el navegador por defecto cuando la app termina de arrancar.
 * Se desactiva con {@code app.abrir-navegador=false}.
 */
@Component
@ConditionalOnProperty(name = "app.abrir-navegador", havingValue = "true", matchIfMissing = true)
public class AbrirNavegador {

    private static final Logger log = LoggerFactory.getLogger(AbrirNavegador.class);

    private final String url;

    public AbrirNavegador(@Value("${server.port:8080}") String puerto) {
        this.url = "http://localhost:" + puerto;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void abrir() {
        String os = System.getProperty("os.name").toLowerCase();
        String[] comando = os.contains("win") ? new String[] {"rundll32", "url.dll,FileProtocolHandler", url}
                : os.contains("mac") ? new String[] {"open", url}
                : new String[] {"xdg-open", url};
        try {
            new ProcessBuilder(comando).start();
            log.info("Navegador abierto en {}", url);
        } catch (IOException e) {
            log.warn("No se pudo abrir el navegador ({}). Abrir {} a mano.", e.getMessage(), url);
        }
    }
}
