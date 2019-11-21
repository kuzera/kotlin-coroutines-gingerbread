package com.example.services

import com.example.Gingerbread
import com.example.Ingredient
import com.example.requiredIngredients
import com.example.clients.AbstractSuspendClient
import com.example.clients.MethodName

abstract class AbstractSuspendHandler(val abstractSuspendClient: AbstractSuspendClient) {
    open suspend fun prepareGingerbread(existingIngredients: Set<Ingredient>): Gingerbread? {
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

    protected fun heatOven(): Boolean {
        return true
    }

    protected fun mixDoughWithButter(heatButter: Boolean, dough: Boolean) = heatButter && dough

    protected fun bake(ovenHeated: Boolean, content: Boolean, vessel: Boolean): Boolean {
        if (ovenHeated && content && vessel) {
            return true
        }
        return false
    }
    protected suspend fun buyMissingIngredients(requiredIngredients: Set<Ingredient>, existingIngredients: Set<Ingredient>): Boolean {
        val ingredientsToBuy = requiredIngredients.minus(existingIngredients)
        return abstractSuspendClient.suspendClientCall(MethodName.ingredients, ingredientsToBuy.joinToString(",")) == "ok"
    }

    protected suspend fun heatButterWithHoney(): Boolean {
        return abstractSuspendClient.suspendClientCall(MethodName.heat, "butter,honey") == "ok"
    }

    suspend fun prepareDough(): Boolean {
        return abstractSuspendClient.suspendClientCall(MethodName.dough, "flour,cinammon,soda,sugar") == "ok"
    }

    protected suspend fun prepareCakeTray(): Boolean {
        return abstractSuspendClient.suspendClientCall(MethodName.tray) == "ok"
    }

    protected suspend fun prepareIcing() : Boolean {
        return abstractSuspendClient.suspendClientCall(MethodName.icing) == "ok"
    }
}