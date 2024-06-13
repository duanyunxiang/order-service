package com.dyx.orderservice.order.domain;

import com.dyx.orderservice.order.persistence.DataConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import reactor.test.StepVerifier;

@Slf4j
// 切片测试r2dbc组件
@DataR2dbcTest
// 启用审计
@Import(DataConfig.class)
// 激活测试容器数据库的自动启动和清理
@Testcontainers
public class OrderRepositoryR2dbcTests {
    //自定义容器数据库
    @Container
    static PostgreSQLContainer<?> postgresql=new PostgreSQLContainer<>(DockerImageName.parse("postgres:14.4"));

    //重写r2dbc和flyway配置，以指向测试数据库
    @DynamicPropertySource
    static void postgresqlProperties(DynamicPropertyRegistry registry){
        registry.add("spring.r2dbc.url",OrderRepositoryR2dbcTests::r2dbcUrl);
        registry.add("spring.r2dbc.username",postgresql::getUsername);
        registry.add("spring.r2dbc.password",postgresql::getPassword);
        registry.add("spring.flyway.url",postgresql::getJdbcUrl);
    }

    private static String r2dbcUrl(){
        return String.format("r2dbc:postgresql://%s:%s/%s",
                postgresql.getHost(),
                postgresql.getMappedPort(PostgreSQLContainer.POSTGRESQL_PORT),
                postgresql.getDatabaseName());
    }

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void createRejectedOrder(){
        var rejectedOrder=OrderService.buildRejectedOrder("1234567890",3);

        StepVerifier
                //验证save方法
                .create(orderRepository.save(rejectedOrder))
                //断言返回的Order具有正确的状态
                .expectNextMatches(entity -> {
                    log.info("{}",entity);
                    return entity.status().equals(OrderStatus.REJECTED);
                })
                .verifyComplete();
    }

    @Test
    void findOrderByIdWhenNotExisting() {
        StepVerifier.create(orderRepository.findById(394L))
                .expectNextCount(0)
                .verifyComplete();
    }
}
