package com.example.service

import com.example.Ingredient
import com.example.service.internal.ExternalServicesCaller
import com.example.service.internal.MethodName
import reactor.core.publisher.Mono

abstract class AbstractHandlerWebflux(private val externalServicesCaller: ExternalServicesCaller) {
    protected fun heatOven(): Mono<Boolean> {
        return Mono.just(true)
    }

    protected fun buyMissingIngredients(requiredIngredients: Set<Ingredient>, existingIngredients: Set<Ingredient>): Mono<Boolean>? {
        val ingredientsToBuy = requiredIngredients.minus(existingIngredients)
        return externalServicesCaller.webClientCall(MethodName.ingredients, ingredientsToBuy.joinToString(","))
                ?.map { it == "ok" }
    }

    protected fun heatButterWithHoney(): Mono<Boolean>? {
        return externalServicesCaller.webClientCall(MethodName.heat, "butter,honey")
                ?.map { it == "ok" }
    }

    protected fun prepareDough(): Mono<Boolean> {
        return externalServicesCaller.webClientCall(MethodName.dough, "flour,cinammon,soda,sugar")
                ?.map { it == "ok" }
                ?: Mono.just(false)
    }

    protected fun mixDoughWithButter(heatButter: Boolean?, dough: Boolean) = heatButter == true && dough

    protected fun prepareCakeTray(): Mono<Boolean> {
        return externalServicesCaller.webClientCall(MethodName.tray)
                ?.map { it == "ok" }
                ?: Mono.just(false)
    }

    protected fun bake(ovenHeated: Boolean, content: Boolean, vessel: Boolean): Boolean {
        if (ovenHeated && content && vessel) {
            return true
        }
        return false
    }

    protected fun prepareIcing() : Mono<Boolean> {
        return externalServicesCaller.webClientCall(MethodName.icing)
                ?.map { it == "ok" }
                ?: Mono.just(false)
    }
}