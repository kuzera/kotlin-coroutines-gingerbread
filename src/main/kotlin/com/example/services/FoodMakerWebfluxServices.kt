package com.example.services

import com.example.Gingerbread
import com.example.Ingredient
import com.example.clients.ReactiveWebClient
import com.example.requiredIngredients
import kotlinx.coroutines.reactive.awaitFirstOrDefault
import kotlinx.coroutines.reactor.mono
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class WebfluxHandler(reactiveWebClient: ReactiveWebClient) : AbstractWebfluxHandler(reactiveWebClient) {
    fun prepareGingerbread(existingIngredients: Set<Ingredient>): Mono<Gingerbread>? {
        return buyMissingIngredients(requiredIngredients, existingIngredients)
                ?.filter { succeeded -> succeeded }
                //.map(succeeded -> heatOvenToDegrees(180L)) //oven should be heated at this point
                ?.flatMap { _ -> heatButterWithHoney() }
                ?.zipWith(prepareDough())
                ?.map { heatedAndDoughTuple -> mixDoughWithButter(heatedAndDoughTuple.t1, heatedAndDoughTuple.t2) }
                ?.zipWith(prepareCakeTray())
                ?.zipWith(heatOven()) { contentAndVesselTuple,ovenHeated ->
                    bake(ovenHeated, contentAndVesselTuple.t1, contentAndVesselTuple.t2)}
                ?.zipWith(prepareIcing()) { baked, icing -> Gingerbread(baked, icing) }
    }
}

@Service
class WebfluxHandlerCoroutines(reactiveWebClient: ReactiveWebClient) : AbstractWebfluxHandler(reactiveWebClient) {
    fun prepareGingerbread(existingIngredients: Set<Ingredient>): Mono<Gingerbread> {
        return mono {
            buyMissingIngredients(requiredIngredients, existingIngredients)?.awaitFirstOrDefault(false)

            val oven = heatOven()
            val heat = heatButterWithHoney()
            val dough = prepareDough()
            val mixedDoughWithButter = mixDoughWithButter(heat?.awaitFirstOrDefault(false), dough.awaitFirstOrDefault(false))

            val tray = prepareCakeTray()
            val baked = bake(oven.awaitFirstOrDefault(false), mixedDoughWithButter, tray.awaitFirstOrDefault(false))
            val icing = prepareIcing().awaitFirstOrDefault(false)

            Gingerbread(baked, icing)
        }
    }
}