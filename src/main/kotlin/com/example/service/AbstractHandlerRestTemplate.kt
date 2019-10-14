package com.example.service

import com.example.Ingredient
import com.example.service.internal.ExternalServicesCaller
import com.example.service.internal.MethodName

abstract class AbstractHandlerRestTemplate(private val externalServicesCaller: ExternalServicesCaller) {
    protected fun heatOven(): Boolean {
        return true
    }

    protected fun buyMissingIngredients(requiredIngredients: Set<Ingredient>, existingIngredients: Set<Ingredient>): Boolean {
        val ingredientsToBuy = requiredIngredients.minus(existingIngredients)
        return externalServicesCaller.restTemplateCall(MethodName.ingredients, ingredientsToBuy.joinToString(",")) == "ok"
    }

    protected fun heatButterWithHoney(): Boolean {
        return externalServicesCaller.restTemplateCall(MethodName.heat, "butter,honey") == "ok"
    }

    fun prepareDough(): Boolean {
        return externalServicesCaller.restTemplateCall(MethodName.dough, "flour,cinammon,soda,sugar") == "ok"
    }

    protected fun mixDoughWithButter(heatButter: Boolean, dough: Boolean) = heatButter && dough

    protected fun prepareCakeTray(): Boolean {
        return externalServicesCaller.restTemplateCall(MethodName.tray) == "ok"
    }

    protected fun bake(ovenHeated: Boolean, content: Boolean, vessel: Boolean): Boolean {
        if (ovenHeated && content && vessel) {
            return true
        }
        return false
    }

    protected fun prepareIcing() : Boolean {
        return externalServicesCaller.restTemplateCall(MethodName.icing) == "ok"
    }
}