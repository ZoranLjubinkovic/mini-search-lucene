package com.gd.lucene.config;


import jakarta.ws.rs.core.Application;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Contact;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;


@OpenAPIDefinition(
        tags = {
                @Tag(name = "Mini search", description = "Lucene mini - search operations."),
        },
        info = @Info(
                title = "'Mini search' API with Quarkus and Lucene",
                version = "0.0.1",
                contact = @Contact(
                        name = "Zoran Ljubinkovic",
                        email = "zljubinkovic@griddynamics.com")
        )
)
public class SwaggerConfig extends Application {

}
