package com.informales.pena.config;

import java.io.IOException;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

/**
 * Sirve la web de React (compilada en /static). Cualquier ruta que no sea un archivo ni de la API
 * (por ejemplo /resumen o /admin/productos) devuelve index.html para que React Router la muestre.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(String ruta, Resource ubicacion) throws IOException {
                        Resource archivo = ubicacion.createRelative(ruta);
                        if (archivo.exists() && archivo.isReadable()) {
                            return archivo;
                        }
                        if (ruta.startsWith("api/") || ruta.contains(".")) {
                            return null;
                        }
                        Resource index = ubicacion.createRelative("index.html");
                        return index.exists() ? index : null;
                    }
                });
    }
}
