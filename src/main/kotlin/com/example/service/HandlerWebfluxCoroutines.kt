package com.example.service

import com.example.Gingerbread
import com.example.Ingredient
import com.example.requiredIngredients
import com.example.service.internal.ExternalServicesCaller
import kotlinx.coroutines.reactive.awaitFirstOrDefault
import kotlinx.coroutines.reactor.mono
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class HandlerWebfluxCoroutines(externalServicesCaller: ExternalServicesCaller) : AbstractHandlerWebflux(externalServicesCaller) {

    fun prepareGingerbread(existingIngredients: Set<Ingredient>): Mono<Gingerbread> {
        return mono {
            buyMissingIngredients(requiredIngredients, existingIngredients)?.map { if (!it)
                throw RuntimeException("cannot get missing ingredients")
            }

            val oven = heatOven()
            val heat = heatButterWithHoney()
            val dough = prepareDough()
            val mixedDoughWithButter = mixDoughWithButter(heat?.awaitFirstOrDefault(false), dough.awaitFirstOrDefault(false))

            val tray = prepareCakeTray()
            val baked = bake(oven.awaitFirstOrDefault(false), mixedDoughWithButter, tray.awaitFirstOrDefault(false))
            val icing = prepareIcing().awaitFirstOrDefault(false)

            Gingerbread(baked, icing == true)
        }
    }
}