package ec.juanperez.test.technique.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private int serverPort;

    @Value("${spring.webflux.base-path:/api/v1}")
    private String basePath;

    @Value("${springdoc.server.url:}")
    private String serverUrl;

    @Bean
    public OpenAPI customOpenAPI() {
        Server server = new Server();
        
        if (serverUrl != null && !serverUrl.isEmpty()) {
            // Si se proporciona una URL completa, usarla
            server.setUrl(serverUrl);
        } else {
            // Construir URL relativa (Swagger usará la URL actual del navegador)
            server.setUrl(basePath);
        }
        
        server.setDescription("API Server");

        return new OpenAPI()
                .servers(List.of(server))
                .info(new Info()
                        .title("Technical Test API")
                        .version("1.0.0")
                        .description("API for Technical Test by Juan Jose Perez")
                        .contact(new Contact()
                                .name("Juan Jose Perez")
                                .email("juanjoperez090@gmail.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")));
    }
}

