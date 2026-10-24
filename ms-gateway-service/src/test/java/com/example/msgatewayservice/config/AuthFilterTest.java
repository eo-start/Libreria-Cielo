/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
package com.example.msgatewayservice.config;

import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpResponse;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthFilterTest {

    @Test
    void debePermitirPeticionOptionsSinValidarToken() {
        WebClient.Builder webClientBuilder = WebClient.builder();

        AuthFilter authFilter = new AuthFilter(webClientBuilder);
        GatewayFilter gatewayFilter = authFilter.apply(new AuthFilter.Config());

        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.options("/productos").build()
        );

        GatewayFilterChain chain = mock(GatewayFilterChain.class);
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        gatewayFilter.filter(exchange, chain).block();

        assertNull(exchange.getResponse().getStatusCode());
        verify(chain, times(1)).filter(exchange);
    }

    @Test
    void debeRetornarUnauthorizedCuandoNoTieneAuthorization() {
        WebClient.Builder webClientBuilder = WebClient.builder();

        AuthFilter authFilter = new AuthFilter(webClientBuilder);
        GatewayFilter gatewayFilter = authFilter.apply(new AuthFilter.Config());

        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/libros").build()
        );

        GatewayFilterChain chain = mock(GatewayFilterChain.class);

        gatewayFilter.filter(exchange, chain).block();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        verify(chain, never()).filter(any(ServerWebExchange.class));
    }

    @Test
    void debeRetornarUnauthorizedCuandoAuthorizationNoTieneBearer() {
        WebClient.Builder webClientBuilder = WebClient.builder();

        AuthFilter authFilter = new AuthFilter(webClientBuilder);
        GatewayFilter gatewayFilter = authFilter.apply(new AuthFilter.Config());

        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/libros")
                        .header(HttpHeaders.AUTHORIZATION, "token-invalido")
                        .build()
        );

        GatewayFilterChain chain = mock(GatewayFilterChain.class);

        gatewayFilter.filter(exchange, chain).block();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        verify(chain, never()).filter(any(ServerWebExchange.class));
    }

    @Test
    void debeRetornarUnauthorizedCuandoAuthorizationExistePeroEsNull() {
        WebClient.Builder webClientBuilder = WebClient.builder();

        AuthFilter authFilter = new AuthFilter(webClientBuilder);
        GatewayFilter gatewayFilter = authFilter.apply(new AuthFilter.Config());

        HttpHeaders headers = new HttpHeaders();
        headers.put(HttpHeaders.AUTHORIZATION, Collections.emptyList());

        ServerHttpRequest request = mock(ServerHttpRequest.class);
        when(request.getMethod()).thenReturn(HttpMethod.GET);
        when(request.getHeaders()).thenReturn(headers);

        MockServerHttpResponse response = new MockServerHttpResponse();

        ServerWebExchange exchange = mock(ServerWebExchange.class);
        when(exchange.getRequest()).thenReturn(request);
        when(exchange.getResponse()).thenReturn(response);

        GatewayFilterChain chain = mock(GatewayFilterChain.class);

        gatewayFilter.filter(exchange, chain).block();

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(chain, never()).filter(any(ServerWebExchange.class));
    }

    @Test
    void debePermitirPeticionCuandoTokenEsValido() {
        AtomicReference<String> urlSolicitada = new AtomicReference<>();

        WebClient.Builder webClientBuilder = WebClient.builder()
                .exchangeFunction(request -> {
                    urlSolicitada.set(request.url().toString());

                    assertEquals(HttpMethod.POST, request.method());

                    return Mono.just(
                            ClientResponse.create(HttpStatus.OK)
                                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                                    .body("{\"token\":\"token-valido\"}")
                                    .build()
                    );
                });

        AuthFilter authFilter = new AuthFilter(webClientBuilder);
        GatewayFilter gatewayFilter = authFilter.apply(new AuthFilter.Config());

        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/ventas")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer token-valido")
                        .build()
        );

        GatewayFilterChain chain = mock(GatewayFilterChain.class);
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        gatewayFilter.filter(exchange, chain).block();

        assertTrue(urlSolicitada.get().contains("http://ms-auth-service/auth/validate?token=token-valido"));
        assertNull(exchange.getResponse().getStatusCode());

        verify(chain, times(1)).filter(exchange);
    }

    @Test
    void debeRetornarUnauthorizedCuandoServicioAuthFalla() {
        WebClient.Builder webClientBuilder = WebClient.builder()
                .exchangeFunction(request -> Mono.error(new RuntimeException("Error al validar token")));

        AuthFilter authFilter = new AuthFilter(webClientBuilder);
        GatewayFilter gatewayFilter = authFilter.apply(new AuthFilter.Config());

        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/ventas")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer token-invalido")
                        .build()
        );

        GatewayFilterChain chain = mock(GatewayFilterChain.class);

        gatewayFilter.filter(exchange, chain).block();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        verify(chain, never()).filter(any(ServerWebExchange.class));
    }
}