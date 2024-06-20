package com.dyx.orderservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.savedrequest.NoOpServerRequestCache;

//启用反应式安全配置
@EnableWebFluxSecurity
public class SecurityConfig {
    @Bean
    SecurityWebFilterChain filterChain(ServerHttpSecurity http){
        return http
                //所有请求都验证
                .authorizeExchange(exchange->exchange
                        .pathMatchers("/actuator/**").permitAll()
                        .anyExchange().authenticated())
                //基于jwt的默认配置启用OAuth2资源服务器
                .oauth2ResourceServer(ServerHttpSecurity.OAuth2ResourceServerSpec::jwt)
                //因为每个请求都包含访问令牌，所以不在请求间保持用户会话
                .requestCache(requestCacheSpec -> requestCacheSpec.requestCache(NoOpServerRequestCache.getInstance()))
                //禁用CSRF防护
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .build();
    }
}
