package com.example

import com.example.services.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.ResourceAccessException
import reactor.core.publisher.Mono
import java.lang.RuntimeException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeoutException

@Controller
@RequestMapping("/gingerbread", produces = [MediaType.APPLICATION_JSON_VALUE])
@ResponseBody
@SpringBootApplication
class CoroutinesController(val handlerBlocking: FoodMakerRestTemplateService,
                           val handlerSuspending: FoodMakerSuspendingService,
                           val foodMakerFuel: FoodMakerFuelService,
                           val foodMakerWebclientCoroutines: WebfluxHandlerCoroutines,
                           val foodMakerWebflux: WebfluxHandler) {

	private val logger: Logger = LoggerFactory.getLogger("controller")
	private val existingIngredients = setOf(Ingredient.butter, Ingredient.honey, Ingredient.flour)

	@GetMapping("/blockingRestTemplate")
	suspend fun blockingGingerbread(): Gingerbread? = handlerBlocking.prepareGingerbread(existingIngredients)

	@GetMapping("/suspendingPureCoroutines")
	suspend fun suspendingGingerbread(): Gingerbread? = handlerSuspending.prepareGingerbread(existingIngredients)

	@GetMapping("/suspendingFuelCoroutines")
	suspend fun suspendingFuel(): Gingerbread? = foodMakerFuel.prepareGingerbread(existingIngredients)

	@GetMapping("/webfluxPureReactive")
	fun suspendingGingerbreadWebfluxEndpoint(): Mono<Gingerbread>? = foodMakerWebflux.prepareGingerbread(existingIngredients)

	@GetMapping("/webfluxReactiveCoroutines")
	fun webfluxGingerbreadMono(): Mono<Gingerbread> = foodMakerWebclientCoroutines.prepareGingerbread(existingIngredients)

	@ExceptionHandler(ResourceAccessException::class, SocketTimeoutException::class, TimeoutException::class, RuntimeException::class)
	@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
	fun handleException(ex: Throwable): Map<String, String> {
		logger.warn("Got $ex")
		return mapOf("error" to "cannot prepare gingerbread")
	}
}

enum class Ingredient {
	butter, honey, flour, soda, cinammon, sugar, egg
}

val requiredIngredients = setOf(Ingredient.butter, Ingredient.honey, Ingredient.flour, Ingredient.soda, Ingredient.cinammon, Ingredient.sugar, Ingredient.egg)

data class Gingerbread(val baked: Boolean, val icing: Boolean)

fun main(args: Array<String>) {
	runApplication<CoroutinesController>(*args)
}

