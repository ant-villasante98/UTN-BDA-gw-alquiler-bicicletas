package com.utn.bda.gwalquilerbicicletas.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtGrantedAuthoritiesConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
public class GWConfig {
    @Bean
    public RouteLocator configurarRutas(
            RouteLocatorBuilder builder,
            @Value("${demo-api-gw.url-microservice-estaciones}") String uriEstacionService,
            @Value("${demo-api-gw.url-microservice-alquileres}") String uriAlquilerService
    ){

        return builder.routes()
                .route(
                        p-> p.path("/api/v1/estaciones/**")
                                .uri(uriEstacionService)
                ).route(
                        p-> p.path("/api/v1/alquileres/**")
                                .uri(uriAlquilerService)
                )
                .build();
    }
    @Bean
    public SecurityWebFilterChain filterChain(ServerHttpSecurity http) throws Exception {
        http.authorizeExchange(exchanges -> exchanges
                // Permisos por roles
                // Para administradores
                        .pathMatchers(HttpMethod.POST,"/api/v1/estaciones")
                        .hasRole("ADMINISTRADOR")
                        .pathMatchers(HttpMethod.GET,"/api/v1/alquileres")
                        .hasRole("ADMINISTRADOR")
                //Para clientes
                        .pathMatchers(HttpMethod.GET,"/api/v1/estaciones")
                        .hasRole("CLIENTE")
                        .pathMatchers(HttpMethod.POST,"/api/v1/alquileres")
                        .hasRole("CLIENTE")
                        .pathMatchers(HttpMethod.PATCH,"/api/v1/alquileres/finalizar-alquiler")
                        .hasRole("CLIENTE")
                //Para cualquier usuario Autenticado
                        .pathMatchers("/api/v1/estaciones/**")
                        .authenticated()
                        .pathMatchers("/api/v1/alquileres/**")
                        .authenticated()
                ).oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                .csrf(ServerHttpSecurity.CsrfSpec::disable);
        return http.build();
    }

    @Bean
    public ReactiveJwtAuthenticationConverter jwtAuthenticationConverter() {
        var jwtAuthenticationConverter = new ReactiveJwtAuthenticationConverter();
        var grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();

        grantedAuthoritiesConverter.setAuthoritiesClaimName("authorities");

        grantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");

        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(
                new ReactiveJwtGrantedAuthoritiesConverterAdapter(grantedAuthoritiesConverter));


        return jwtAuthenticationConverter;
    }

}
