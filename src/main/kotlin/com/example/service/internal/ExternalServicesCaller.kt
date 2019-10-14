package com.example.service.internal

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.coroutines.awaitStringResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@Component
class ExternalServicesCaller(val restTemplate: RestTemplate,
                             val webClient: WebClient) {
    private val logger: Logger = LoggerFactory.getLogger("external")
    private val baseUrl = "http://localhost:8087/"

//     fun clientCall(clientType: ClientType, methodName: MethodName, paramValue: String? = null): Any {
//        val url = getUrl(methodName.name, paramValue)
//        logger.debug("${clientType} call to ${url}")
//
//        return when(clientType) {
//            ClientType.restTemplate -> restTemplateCall(url)
//            ClientType.webClient -> webClientCall(url)
//            ClientType.fuel -> fuelClientCall(url)
//        }
//    }
//
//    fun restTemplateCall(url: String): String {
//        val responseEntity = restTemplate.getForEntity(url, String::class.java)
//        return responseEntity.body.toString()
//    }
//
//    suspend fun fuelClientCall(url: String): String {
//        return Fuel.get(url).awaitStringResponse().third
//    }

//    fun webClientCall(url: String): Mono<String> {
//        return webClient.get()
//                .uri(url)
//                .retrieve()
//                .bodyToMono(String::class.java)
//                .onErrorReturn("")
//                .doOnSuccess { logger.debug("Response success for $url is $it") }
//                .doOnError { logger.info("Response error for $url is $it") }
//                .onErrorResume { _ -> Mono.just("")  }
//    }

    fun restTemplateCall(methodName: MethodName, paramValue: String? = null): String {
        val url = getUrl(methodName.name, paramValue)
        logger.debug("restTemplateCall to ${url}")
        val responseEntity = restTemplate.getForEntity(url, String::class.java)
        return responseEntity.body.toString()
    }

    suspend fun fuelClientCall(methodName: MethodName, paramValue: String? = null): String {
        val url = getUrl(methodName.name, paramValue)
        logger.debug("fuelClientCall to ${url}")
        return Fuel.get(url).awaitStringResponse().third
    }

    fun webClientCall(methodName: MethodName,
                      paramValue: String? = null): Mono<String?>? {
        logger.debug("webclient for $methodName and $paramValue")
        return webClient.get()
                .uri(getUrl(methodName.name, paramValue))
                .retrieve()
                .bodyToMono(String::class.java)
                .onErrorReturn("")
                .doOnSuccess { logger.debug("Response success for $methodName and $paramValue is $it") }
                .doOnError { logger.info("Response error for $methodName and $paramValue is $it") }
                .onErrorResume { _ -> Mono.just("")  }
    }

    private fun getUrl(methodName: String, paramValue: String?): String
            = "${baseUrl}${methodName}" + if (paramValue != null) "?value=${paramValue}" else ""

}

enum class ClientType {
    restTemplate, webClient, fuel
}

enum class MethodName {
    ingredients, heat, dough, tray, icing
}