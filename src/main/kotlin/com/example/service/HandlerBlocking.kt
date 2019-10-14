package com.example.service

import com.example.Gingerbread
import com.example.Ingredient
import com.example.requiredIngredients
import com.example.service.internal.ExternalServicesCaller
import org.springframework.stereotype.Component

@Component
class HandlerBlocking(externalServicesCaller: ExternalServicesCaller) : AbstractHandlerRestTemplate(externalServicesCaller) {

    fun prepareGingerbread(existingIngredients: Set<Ingredient>): Gingerbread {
        if (!buyMissingIngredients(requiredIngredients, existingIngredients))
            throw RuntimeException("cannot get missing ingredients")

        val oven = heatOven()
        val heat = heatButterWithHoney()
        val dough = prepareDough()
        val mixedDoughWithButter = mixDoughWithButter(heat, dough)

        val tray = prepareCakeTray()
        val baked = bake(oven, mixedDoughWithButter, tray)
        val icing = prepareIcing()

        return Gingerbread(baked, icing)
    }
}