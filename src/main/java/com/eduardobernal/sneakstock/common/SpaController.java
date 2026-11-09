package com.eduardobernal.sneakstock.common;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * En la imagen de producción el frontend se sirve desde Spring Boot. Si se recarga la página en
 * /vender o /traspasos, esas rutas no son archivos: se devuelve index.html y el router de Vue
 * decide qué pantalla pintar. Solo rutas de un nivel y sin punto, así no toca /api ni /assets/*.js.
 */
@Controller
public class SpaController {

    @GetMapping("/{path:[^.]*}")
    public String forwardToIndex() {
        return "forward:/index.html";
    }
}
