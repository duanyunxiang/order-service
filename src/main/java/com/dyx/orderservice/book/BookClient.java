package com.dyx.orderservice.book;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Slf4j
@Component
@AllArgsConstructor
public class BookClient {
    private static final String BOOKS_ROOT_API = "/books/";
    private final WebClient webClient;

    public Mono<BookDto> getBookByIsbn(String isbn){
        return webClient
                .get().uri(BOOKS_ROOT_API+isbn)
                //发送请求并获取相应
                .retrieve()
                .bodyToMono(BookDto.class)
                // 定义超时行为
                .timeout(Duration.ofSeconds(3),Mono.empty())
                // 收到404响应时，返回空对象
                .onErrorResume(WebClientResponseException.NotFound.class,exception -> Mono.empty())
                // 定义重试行为，retryWhen放到timeout之后表示每次重试可等3秒；若放到timeout之前，表示所有重试动作需在3秒内完成
                // 使用指数回退重试策略，重试3次，初始延迟为100ms
                .retryWhen(Retry.backoff(3, Duration.ofMillis(100)))
                // 3次重试后还是收到错误，捕获异常并返回空对象
                .onErrorResume(Exception.class, exception-> {
                    log.warn("getBookByIsbn请求异常",exception);
                    return Mono.empty();
                });
    }
}
