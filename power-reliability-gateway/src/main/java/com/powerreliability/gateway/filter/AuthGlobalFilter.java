package com.powerreliability.gateway.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.powerreliability.common.entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import com.powerreliability.common.auth.JwtUtil;
import io.jsonwebtoken.Claims;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * JWT 认证全局过滤器
 * 白名单路径直接放行，其他请求校验 Authorization: Bearer {token}
 */
@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    @Autowired
    private JwtUtil jwtUtil;

    private final List<String> whiteList = Arrays.asList(
            "/api/system/login",
            "/api/system/register",
            "/api/system/captcha",
            "/api/dashboard/**"
    );

    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // 白名单路径直接放行
        for (String pattern : whiteList) {
            if (pathMatcher.match(pattern, path)) {
                return chain.filter(exchange);
            }
        }

        // 获取 Authorization header
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(exchange, "未授权或token已过期");
        }

        String token = authHeader.substring(7);

        try {
            // 校验 JWT
            if (!jwtUtil.isTokenValid(token)) {
                return unauthorized(exchange, "未授权或token已过期");
            }

            Claims claims = jwtUtil.parseToken(token);
            String userId = claims.getSubject();
            String username = claims.get("username", String.class);

            // 将用户信息设置到请求头，传递给下游微服务
            ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                    .header("X-User-Id", userId)
                    .header("X-User-Name", username)
                    .build();

            ServerWebExchange mutatedExchange = exchange.mutate()
                    .request(mutatedRequest)
                    .build();

            return chain.filter(mutatedExchange);
        } catch (Exception e) {
            return unauthorized(exchange, "未授权或token已过期");
        }
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String msg) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        Result<Void> result = Result.error(401, msg);
        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsString(result).getBytes(StandardCharsets.UTF_8);
        } catch (Exception e) {
            bytes = ("{\"code\":401,\"msg\":\"" + msg + "\"}").getBytes(StandardCharsets.UTF_8);
        }

        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
