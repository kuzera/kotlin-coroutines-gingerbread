package com.example.service

import com.example.Gingerbread
import com.example.Ingredient
import com.example.requiredIngredients
import com.example.service.internal.ExternalServicesCaller
import kotlinx.coroutines.async
import kotlinx.coroutines.supervisorScope
import org.springframework.stereotype.Component
import org.springframework.web.client.ResourceAccessException

@Component
class HandlerSuspending(externalServicesCaller: ExternalServicesCaller) : AbstractHandlerRestTemplate(externalServicesCaller) {

    suspend fun prepareGingerbread(existingIngredients: Set<Ingredient>): Gingerbread? {
        return supervisorScope { //prevents from exception propagation
            val missingIngredientsBought = try { async { buyMissingIngredients(requiredIngredients, existingIngredients) }.await() } catch (ex: ResourceAccessException) { false }
            if (!missingIngredientsBought) {
                throw RuntimeException("cannot get missing ingredients")
            }

            val oven = async { heatOven() }
            val heat = async { heatButterWithHoney() }
            val dough = async { prepareDough() }
            val mixedDoughWithButter = try { mixDoughWithButter(heat.await(), dough.await()) } catch (ex: ResourceAccessException) { false }

            val tray = async { prepareCakeTray() }
            val baked = try { async { bake(oven.await(), mixedDoughWithButter, tray.await()) }.await() } catch (ex: ResourceAccessException) { false }
            val icing = try { async { prepareIcing() }.await() } catch (ex: ResourceAccessException) { false }
            Gingerbread(baked, icing)
        }
    }
}