package akkimi_BE.aja.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Value("${app.server-uri}")
    private String serverUri;

    private static final String BEARER_SCHEME = "bearerAuth"; // 컨트롤러 @SecurityRequirement에서 사용하는 이름

    @Bean
    public OpenAPI openAPI() {

        return new OpenAPI()
                .info(new Info()
                        .title("Akkimi API")
                        .description("아끼미 API 명세서")
                        .version("v1.0.0"))
                .servers(List.of(
                        new Server()
                                .url(serverUri).description("기본 서버")
                ))
                .components(new Components()
                        .addSecuritySchemes(BEARER_SCHEME,
                                new SecurityScheme()
                                        .name(BEARER_SCHEME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")))
                // 모든 엔드포인트에 Bearer <token> 기본 적용
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME));
    }
}
