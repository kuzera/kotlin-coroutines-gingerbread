package com.example.service

import com.example.Gingerbread
import com.example.Ingredient
import com.example.service.internal.ExternalServicesCaller
import com.example.service.internal.MethodName
import kotlinx.coroutines.async
import kotlinx.coroutines.withTimeoutOrNull
import org.springframework.stereotype.Component

@Component
class FoodMakerFuel(val externalServicesCaller: ExternalServicesCaller) {

    suspend fun prepareGingerbread(existingIngredients: Set<Ingredient>): Gingerbread? {
        return withTimeoutOrNull(5000) {
            val requiredIngredients = setOf(Ingredient.butter, Ingredient.honey, Ingredient.flour, Ingredient.soda, Ingredient.cinammon, Ingredient.sugar, Ingredient.egg)
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

    private fun heatOven(): Boolean {
        return true
    }

    private suspend fun buyMissingIngredients(requiredIngredients: Set<Ingredient>, existingIngredients: Set<Ingredient>): Boolean {
        val ingredientsToBuy = requiredIngredients.minus(existingIngredients)
        return externalServicesCaller.fuelClientCall(MethodName.ingredients, ingredientsToBuy.joinToString(",")) == "ok"
    }

    private suspend fun heatButterWithHoney(): Boolean {
        return externalServicesCaller.fuelClientCall(MethodName.heat, "butter,honey") == "ok"
    }

    private suspend fun prepareDough(): Boolean {
        return externalServicesCaller.fuelClientCall(MethodName.dough, "flour,cinammon,soda,sugar") == "ok"
    }

    private fun mixDoughWithButter(heatButter: Boolean, dough: Boolean) = heatButter && dough

    private suspend fun prepareCakeTray(): Boolean {
        return externalServicesCaller.fuelClientCall(MethodName.tray) == "ok"
    }

    private fun bake(ovenHeated: Boolean, content: Boolean, vessel: Boolean): Boolean {
        if (ovenHeated && content && vessel) {
            return true
        }
        return false
    }

    private suspend fun prepareIcing() : Boolean {
        return externalServicesCaller.fuelClientCall(MethodName.icing) == "ok"
    }
}