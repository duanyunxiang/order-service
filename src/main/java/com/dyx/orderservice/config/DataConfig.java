package com.dyx.orderservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.ReactiveAuditorAware;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;

@Configuration
// 启用r2dbc审计
@EnableR2dbcAuditing
public class DataConfig {
    //返回当前认证用户
    @Bean
    ReactiveAuditorAware<String> auditorAware(){
        return ()->
                ReactiveSecurityContextHolder.getContext()
                        .map(SecurityContext::getAuthentication)
                        .filter(Authentication::isAuthenticated)
                        //subject-用户加密ID
                        .map(Authentication::getName);
    }
}
