package com.example.services

import com.example.Gingerbread
import com.example.Ingredient
import com.example.clients.FuelClient
import com.example.clients.RestTemplateClient
import com.example.requiredIngredients
import kotlinx.coroutines.async
import kotlinx.coroutines.supervisorScope
import org.springframework.stereotype.Service
import org.springframework.web.client.ResourceAccessException

@Service
class FoodMakerFuelService(fuelClient: FuelClient) : AbstractSuspendHandler(fuelClient)

@Service
class FoodMakerRestTemplateService(restTemplateClient: RestTemplateClient) : AbstractSuspendHandler(restTemplateClient)

@Service
class FoodMakerSuspendingService(restTemplateClient: RestTemplateClient) : AbstractSuspendHandler(restTemplateClient) {
    override suspend fun prepareGingerbread(existingIngredients: Set<Ingredient>): Gingerbread? {
        return supervisorScope { //prevents from exception propagation
            val missingIngredientsBought = try { async { buyMissingIngredients(requiredIngredients, existingIngredients) }.await() } catch (ex: ResourceAccessException) { false }
            if (!missingIngredientsBought) {
                throw RuntimeException("cannot get missing ingredients")
            }

            val oven = heatOven()
            val heat = async { heatButterWithHoney() }
            val dough = async { prepareDough() }
            val mixedDoughWithButter = try { mixDoughWithButter(heat.await(), dough.await()) } catch (ex: ResourceAccessException) { false }

            val tray = async { prepareCakeTray() }
            val baked = try { async { bake(oven, mixedDoughWithButter, tray.await()) }.await() } catch (ex: ResourceAccessException) { false }
            val icing = try { async { prepareIcing() }.await() } catch (ex: ResourceAccessException) { false }

            Gingerbread(baked, icing)
        }
    }
}