package com.example.services

import com.example.Ingredient
import com.example.clients.AbstractReactiveClient
import com.example.clients.MethodName
import reactor.core.publisher.Mono

abstract class AbstractWebfluxHandler(private val abstractReactiveClient: AbstractReactiveClient) {
    protected fun heatOven(): Mono<Boolean> {
        return Mono.just(true)
    }

    protected fun buyMissingIngredients(requiredIngredients: Set<Ingredient>, existingIngredients: Set<Ingredient>): Mono<Boolean>? {
        val ingredientsToBuy = requiredIngredients.minus(existingIngredients)
        return abstractReactiveClient.webClientCall(MethodName.ingredients, ingredientsToBuy.joinToString(","))
                ?.map { if (it == "ok") true else throw RuntimeException("cannot get missing ingredients") }
    }

    protected fun heatButterWithHoney(): Mono<Boolean>? {
        return abstractReactiveClient.webClientCall(MethodName.heat, "butter,honey")
                ?.map { it == "ok" }
    }

    protected fun prepareDough(): Mono<Boolean> {
        return abstractReactiveClient.webClientCall(MethodName.dough, "flour,cinammon,soda,sugar")
                ?.map { it == "ok" }
                ?: Mono.just(false)
    }

    protected fun mixDoughWithButter(heatButter: Boolean?, dough: Boolean) = heatButter == true && dough

    protected fun prepareCakeTray(): Mono<Boolean> {
        return abstractReactiveClient.webClientCall(MethodName.tray)
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
        return abstractReactiveClient.webClientCall(MethodName.icing)
                ?.map { it == "ok" }
                ?: Mono.just(false)
    }
}