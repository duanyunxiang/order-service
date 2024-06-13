package com.dyx.orderservice.book;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class ClientConfig {
    // 配置webClient
    @Bean
    public WebClient webClient(ClientProperties clientProperties,WebClient.Builder webClientBuilder){
        return webClientBuilder
                //连接catalog-service
                .baseUrl(clientProperties.catalogServiceUri().toString())
                .build();
    }
}
