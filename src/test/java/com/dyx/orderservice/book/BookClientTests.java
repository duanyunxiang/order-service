package com.dyx.orderservice.book;

import lombok.extern.slf4j.Slf4j;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;

@Slf4j
public class BookClientTests {
    private MockWebServer mockWebServer;
    private BookClient bookClient;

    @BeforeEach
    public void setup() throws IOException {
        this.mockWebServer=new MockWebServer();
        //启动mock服务
        this.mockWebServer.start();

        var webClient= WebClient.builder()
                // 使用mock服务的url作为webClient的基础url
                .baseUrl(mockWebServer.url("/").uri().toString())
                .build();
        this.bookClient=new BookClient(webClient);
    }

    @AfterEach
    public void clean() throws IOException{
        //关闭mock服务
        this.mockWebServer.shutdown();
    }

    //直接运行报错时，设置：File -> Settings -> 检索Gradle -> Run tests using 改为 Intellij IDEA
    @Test
    public void whenBookExistsThenReturnBook(){
        var bookIsbn="1234567890";

        var mockResponse=new MockResponse()
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        {
                            "isbn":%s,
                            "title":"Title",
                            "author":"Author",
                            "price":9.90,
                            "publisher":"Polarsophia"
                        }
                        """.formatted(bookIsbn));

        //添加mock响应到服务器处理队列中
        mockWebServer.enqueue(mockResponse);

        Mono<BookDto> book=bookClient.getBookByIsbn(bookIsbn);

        //StepVerifier用于验证反应式流结果
        StepVerifier.create(book)
                //断言返回的isbn与请求相等
                .expectNextMatches(b->{
                    log.info("{}",b);
                    return b.isbn().equals(bookIsbn);
                })
                .verifyComplete();
    }

    @Test
    void whenBookNotExistsThenReturnEmpty() {
        var bookIsbn = "1234567891";

        var mockResponse = new MockResponse()
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setResponseCode(404);

        mockWebServer.enqueue(mockResponse);

        StepVerifier.create(bookClient.getBookByIsbn(bookIsbn))
                .expectNextCount(0)
                .verifyComplete();
    }
}
