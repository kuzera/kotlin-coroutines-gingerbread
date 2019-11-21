package com.example.clients

import com.github.kittinunf.fuel.core.FuelManager
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
class ClientsConfig {
    private final val CONNECTION_TIMEOUT = 50
    private final val READ_TIMEOUT = 80

    init {
        FuelManager.instance.timeoutInMillisecond = CONNECTION_TIMEOUT
        FuelManager.instance.timeoutReadInMillisecond = READ_TIMEOUT
    }

    @Bean
    fun getRestTemplate(): RestTemplate {
        val threadPoolTaskExecutor = ThreadPoolTaskExecutor()
        threadPoolTaskExecutor.corePoolSize = 50
        threadPoolTaskExecutor.maxPoolSize = 100
        threadPoolTaskExecutor.setQueueCapacity(100)

        val clientHttpRequestFactory = SimpleClientHttpRequestFactory()
        clientHttpRequestFactory.setConnectTimeout(CONNECTION_TIMEOUT)
        clientHttpRequestFactory.setReadTimeout(READ_TIMEOUT)
        clientHttpRequestFactory.setTaskExecutor(threadPoolTaskExecutor)

        return RestTemplate(clientHttpRequestFactory)
    }

    @Bean
    fun webClient(): WebClient {
        val httpClient = reactor.netty.http.client.HttpClient.create()
            .tcpConfiguration { c ->
                c.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, CONNECTION_TIMEOUT)
                    .doOnConnected { conn ->
                        conn.addHandlerLast(ReadTimeoutHandler(READ_TIMEOUT.toLong(), TimeUnit.MILLISECONDS))
                            .addHandlerLast(WriteTimeoutHandler(READ_TIMEOUT.toLong(), TimeUnit.MILLISECONDS))
                    }
            }
        val connector = ReactorClientHttpConnector(httpClient)

        return WebClient.builder().clientConnector(connector).build()
    }
}
