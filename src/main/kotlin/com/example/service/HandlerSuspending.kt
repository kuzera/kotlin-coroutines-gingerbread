package com.example.service

import com.example.Gingerbread
import com.example.Ingredient
import com.example.requiredIngredients
import com.example.service.internal.ExternalServicesCaller
import kotlinx.coroutines.async
import kotlinx.coroutines.withTimeoutOrNull
import org.springframework.stereotype.Component

@Component
class HandlerSuspending(externalServicesCaller: ExternalServicesCaller) : AbstractHandlerRestTemplate(externalServicesCaller) {

    suspend fun prepareGingerbread(existingIngredients: Set<Ingredient>): Gingerbread? {
        return withTimeoutOrNull(5000) {
            if (!buyMissingIngredients(requiredIngredients, existingIngredients))
                throw RuntimeException("cannot get missing ingredients")

            val oven = async { heatOven() }
            val heat = async { heatButterWithHoney() }
            val dough = async { prepareDough() }
            val mixedDoughWithButter = mixDoughWithButter(heat.await(), dough.await())

            val tray = async { prepareCakeTray() }
            val baked = async { bake(oven.await(), mixedDoughWithButter, tray.await()) }
            val icing = async { prepareIcing() }

            Gingerbread(baked.await(), icing.await())
        }
    }
}