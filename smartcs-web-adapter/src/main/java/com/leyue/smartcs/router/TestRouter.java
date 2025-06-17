package com.leyue.smartcs.router;

import com.alibaba.fastjson2.JSONObject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.RouterFunctions;
import org.springframework.web.servlet.function.ServerResponse;

import java.io.IOException;
import java.net.URI;

@Configuration
public class TestRouter {
    @Bean
    public RouterFunction<ServerResponse> testHttpRouter() {
        return RouterFunctions.route()
                .GET("/router/test1", (ServerRequest) -> {
                    return ServerResponse.ok().body("test1");
                })
                .POST("/router/test2", (ServerRequest) -> {
                    return ServerResponse.ok().body("test1");
                })
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> testSSERouter() throws IOException {
        return RouterFunctions.route()
                .GET("/hello",
                        req -> ServerResponse.ok()
                                .contentType(MediaType.TEXT_PLAIN)
                                .body("Hello, world!"))
                .POST("/todos",
                        req -> {
                            JSONObject todo = req.body(JSONObject.class);
                            URI location = URI.create("/todos/" + "1");
                            return ServerResponse.created(location).body(todo);
                        })
                .GET("/events",
                        req -> ServerResponse.sse(sse ->
                        {
                            sse.onComplete(() -> {
                                System.out.println("onComplete");
                            });
                            sse.onError((error) -> {
                                System.out.println("onError");
                            });
                            sse.onTimeout(() -> {
                                System.out.println("timeout");
                            });
                            try {
                                sse.event("tick").id("42").send("pong");
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            sse.complete();
                        }))
                .build();
    }
}
