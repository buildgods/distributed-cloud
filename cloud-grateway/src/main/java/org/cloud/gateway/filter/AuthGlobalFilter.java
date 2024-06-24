package org.cloud.gateway.filter;

import cn.hutool.core.collection.CollUtil;

import lombok.RequiredArgsConstructor;
import org.cloud.gateway.config.AuthProperties;
import org.cloud.gateway.utils.JwtUtil;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AuthGlobalFilter implements GlobalFilter, Ordered {
    private final AuthProperties authProperties;
    private final AntPathMatcher antPathMatcher = new AntPathMatcher(); // spring 专用于匹配通配符路径的
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 获取Request
        ServerHttpRequest request = exchange.getRequest();
        // 判断是否需要拦截
        if(isExclude(request.getPath().toString())){
            // 放行
            return chain.filter(exchange);
        }
        // 获取请求头中的token
        String token;
        HttpHeaders headers = request.getHeaders();
        List<String> authorization = headers.get("Authorization");
        if(!CollUtil.isEmpty(authorization))
            token = authorization.get(0);
        else{
            token = null;
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }
        // 传递用户信息
        // 下游的过滤器设置用户信息的请求头
        ServerWebExchange swe = exchange.mutate().request(builder -> builder.header("user-info", token)).build();

        // 放行
        // 设置完的用户信息请求头传递下去
        return chain.filter(swe);
    }

    private boolean isExclude(String path) {
        for (String excludePath : authProperties.getExcludePaths()) {
            if (antPathMatcher.match(excludePath, path)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
