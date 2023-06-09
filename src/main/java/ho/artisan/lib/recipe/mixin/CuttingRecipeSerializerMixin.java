/*
 * Copyright 2022 QuiltMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ho.artisan.lib.recipe.mixin;

import com.google.gson.JsonObject;
import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.data.server.recipe.SingleItemRecipeJsonBuilder;
import net.minecraft.recipe.CuttingRecipe;

import ho.artisan.lib.recipe.api.serializer.FabricRecipeSerializer;

@Mixin(CuttingRecipe.Serializer.class)
public abstract class CuttingRecipeSerializerMixin<T extends CuttingRecipe> implements FabricRecipeSerializer<T> {
	@Override
	public JsonObject toJson(T recipe) {
		return new SingleItemRecipeJsonBuilder.SingleItemRecipeJsonProvider(recipe.getId(), this, recipe.getGroup(),
				recipe.getIngredients().get(0), recipe.getOutput().getItem(), recipe.getOutput().getCount(),
				null, null)
				.toJson();
	}
}
