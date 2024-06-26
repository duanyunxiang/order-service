package com.dyx.orderservice.order.web;

import com.dyx.orderservice.config.SecurityConfig;
import com.dyx.orderservice.order.domain.Order;
import com.dyx.orderservice.order.domain.OrderService;
import com.dyx.orderservice.order.domain.OrderStatus;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@WebFluxTest(OrderController.class)
//导入应用的安全配置
@Import(SecurityConfig.class)
class OrderControllerWebFluxTests {
	@Autowired
	WebTestClient webClient;
	@MockBean
	OrderService orderService;

	// 解码访问令牌用
	@MockBean
	ReactiveJwtDecoder reactiveJwtDecoder;

	@Test
	void whenBookNotAvailableThenRejectOrder() {
		var orderRequest = new OrderRequest("1234567890", 3);
		var expectedOrder = OrderService.buildRejectedOrder(orderRequest.isbn(), orderRequest.quantity());
		given(orderService.submitOrder(orderRequest.isbn(), orderRequest.quantity()))
				.willReturn(Mono.just(expectedOrder));

		webClient
				.mutateWith(SecurityMockServerConfigurers
						//mock访问令牌
						.mockJwt().authorities(new SimpleGrantedAuthority("ROLE_customer")))
				.post()
				.uri("/orders")
				.bodyValue(orderRequest)
				.exchange()
				.expectStatus().is2xxSuccessful()
				.expectBody(Order.class).value(actualOrder -> {
					assertThat(actualOrder).isNotNull();
					assertThat(actualOrder.status()).isEqualTo(OrderStatus.REJECTED);
				});
	}

}
