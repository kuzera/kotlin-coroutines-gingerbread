package com.example.service.internal

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.coroutines.awaitStringResponseResult
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.client.ResourceAccessException
import org.springframework.web.client.RestTemplate
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@Component
class ExternalServicesCaller(val restTemplate: RestTemplate,
                             val webClient: WebClient) {
    private val logger: Logger = LoggerFactory.getLogger("external")
    private val baseUrl = "http://localhost:8087/"

    init {
        FuelManager.instance.timeoutInMillisecond = 50
        FuelManager.instance.timeoutReadInMillisecond = 80
    }

    fun restTemplateCall(methodName: MethodName, paramValue: String? = null): String {
        val url = getUrl(methodName.name, paramValue)
        logger.debug("restTemplateCall to ${url}")

        return try {
            restTemplate.getForEntity(url, String::class.java).body.toString()
        } catch (ex: ResourceAccessException) {
            ""
        }
    }

    suspend fun fuelClientCall(methodName: MethodName, paramValue: String? = null): String {
        val url = getUrl(methodName.name, paramValue)
        logger.debug("fuelClientCall to ${url}")

        val (_, _, result) = Fuel.get(url).awaitStringResponseResult()

        return result.fold(
                { success -> success },
                { error -> logger.info("Response error for $url: ${error.exception}")
                           ""}
        )
    }

    fun webClientCall(methodName: MethodName,
                      paramValue: String? = null): Mono<String?>? {
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

    private fun getUrl(methodName: String, paramValue: String?): String
            = "${baseUrl}${methodName}" + if (paramValue != null) "?value=${paramValue}" else ""

}

enum class MethodName {
    ingredients, heat, dough, tray, icing
}