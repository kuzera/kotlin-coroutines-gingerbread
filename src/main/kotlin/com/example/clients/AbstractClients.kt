package com.example.clients

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.coroutines.awaitStringResponseResult
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.client.ResourceAccessException
import org.springframework.web.client.RestTemplate
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

enum class MethodName {
    ingredients, heat, dough, tray, icing
}

interface AbstractClient {
    val logger: Logger
        get() = LoggerFactory.getLogger("external")
    val baseUrl: String
        get() = "http://localhost:8087/"

    fun getUrl(methodName: String, paramValue: String?): String
            = "${baseUrl}${methodName}" + if (paramValue != null) "?value=${paramValue}" else ""
}

interface AbstractSuspendClient: AbstractClient {
    suspend fun suspendClientCall(methodName: MethodName, paramValue: String? = null): String
}
interface AbstractReactiveClient: AbstractClient {
    fun webClientCall(methodName: MethodName, paramValue: String? = null): Mono<String?>?
}

@Component
class RestTemplateClient(val restTemplate: RestTemplate): AbstractSuspendClient {
    override suspend fun suspendClientCall(methodName: MethodName, paramValue: String?): String {
        val url = getUrl(methodName.name, paramValue)
        logger.debug("restTemplateCall to ${url}")

        return try {
            restTemplate.getForEntity(url, String::class.java).body.toString()
        } catch (ex: ResourceAccessException) {
            ""
        }
    }
}

@Component
class FuelClient: AbstractSuspendClient {
    override suspend fun suspendClientCall(methodName: MethodName, paramValue: String?): String {
        val url = getUrl(methodName.name, paramValue)
        logger.debug("fuelClientCall to ${url}")

        val (_, _, result) = Fuel.get(url).awaitStringResponseResult()

        return result.fold(
                { success -> success },
                { error -> logger.info("Response error for $url: ${error.exception}")
                    ""}
        )
    }
}

@Component
class ReactiveWebClient(val webClient: WebClient): AbstractReactiveClient {
    override fun webClientCall(methodName: MethodName,
                               paramValue: String?): Mono<String?>? {
        val url = getUrl(methodName.name, paramValue)
        logger.debug("webClientCall to ${url}")
        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String::class.java)
                .doOnSuccess { logger.debug("Response success for $methodName and $paramValue is $it") }
                .doOnError { logger.info("Response error for $methodName and $paramValue is $it") }
                .onErrorReturn("")
    }
}