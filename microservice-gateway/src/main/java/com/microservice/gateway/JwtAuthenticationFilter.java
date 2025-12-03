package com.microservice.gateway;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    @Value("${jwt.secret}" )
    private String jwtSecret;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        if (request.getMethod().equals(org.springframework.http.HttpMethod.OPTIONS)) {
            return chain.filter(exchange);
        }

        String path = request.getPath().toString();

        if (path.startsWith("/api/usuarios/login")
                || path.startsWith("/api/usuarios/register")) {
            return chain.filter(exchange);
        }

        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return this.onError(exchange, "Token JWT faltante o inválido", HttpStatus.UNAUTHORIZED);
        }

        String token = authHeader.substring(7);
        Claims claims;

        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            return this.onError(exchange, "Token JWT inválido", HttpStatus.UNAUTHORIZED);
        }

        String userCorreo = claims.getSubject();

        Long idUsuario;
        Object idRaw = claims.get("idUsuario");
        if (idRaw instanceof Integer) {
            idUsuario = ((Integer) idRaw).longValue();
        } else if (idRaw instanceof Long) {
            idUsuario = (Long) idRaw;
        } else if (idRaw instanceof String) {
            try {
                idUsuario = Long.parseLong((String) idRaw);
            } catch (NumberFormatException e) {
                return this.onError(exchange, "ID de usuario no válido en token", HttpStatus.UNAUTHORIZED);
            }
        } else {
            return this.onError(exchange, "ID de usuario no válido en token", HttpStatus.UNAUTHORIZED);
        }

        ServerHttpRequest modifiedRequest = request.mutate()
                .header("X-User-Correo", userCorreo)
                .header("X-User-Id", String.valueOf(idUsuario))
                .build();

        return chain.filter(exchange.mutate().request(modifiedRequest).build());
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus ) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus );
        return response.setComplete();
    }

    @Override
    public int getOrder() {
        return -1;
    }
}