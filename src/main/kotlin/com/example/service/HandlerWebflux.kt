package com.example.service

import com.example.Gingerbread
import com.example.Ingredient
import com.example.requiredIngredients
import com.example.service.internal.ExternalServicesCaller
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class HandlerWebflux(externalServicesCaller: ExternalServicesCaller) : AbstractHandlerWebflux(externalServicesCaller) {

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