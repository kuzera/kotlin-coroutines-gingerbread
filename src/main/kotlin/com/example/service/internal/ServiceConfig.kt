package com.example.service.internal

import io.netty.channel.ChannelOption
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.web.client.RestTemplate
import org.springframework.web.reactive.function.client.WebClient
import java.util.concurrent.TimeUnit

@Configuration
class ServiceConfig {

    @Bean
    fun getRestTemplate(): RestTemplate {
        val threadPoolTaskExecutor = ThreadPoolTaskExecutor()
        threadPoolTaskExecutor.corePoolSize = 10
        threadPoolTaskExecutor.maxPoolSize = 100

        val clientHttpRequestFactory = SimpleClientHttpRequestFactory()
        clientHttpRequestFactory.setConnectTimeout(50)
        clientHttpRequestFactory.setReadTimeout(80)
        clientHttpRequestFactory.setTaskExecutor(threadPoolTaskExecutor)

        return RestTemplate(clientHttpRequestFactory)
    }

    @Bean
    fun webClient(): WebClient = WebClientBuilder(50, 80)
            .build()
}

class WebClientBuilder(private val connectionTimeout: Int, private val readTimeout: Int) {
    private val reactorConnector: ReactorClientHttpConnector
        get() {
            val client = reactor.netty.http.client.HttpClient.create()
                .tcpConfiguration { c ->
                    c.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectionTimeout)
                        .doOnConnected { conn ->
                            conn.addHandlerLast(ReadTimeoutHandler(readTimeout.toLong(), TimeUnit.MILLISECONDS))
                                .addHandlerLast(WriteTimeoutHandler(readTimeout.toLong(), TimeUnit.MILLISECONDS))
                        }
                }
            return ReactorClientHttpConnector(client)
        }

    fun build(): WebClient {
        return WebClient.builder()
                .clientConnector(reactorConnector)
                .build()
    }
}
