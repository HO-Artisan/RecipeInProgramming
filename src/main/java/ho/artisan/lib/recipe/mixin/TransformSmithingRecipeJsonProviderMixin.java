/*
 * Copyright 2023 The Quilt Project
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

import ho.artisan.lib.recipe.api.serializer.FabricRecipeSerializer;
import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.recipe.SmithingTransformRecipe;
import net.minecraft.data.server.recipe.SmithingTransformRecipeJsonBuilder;

@Mixin(SmithingTransformRecipe.Serializer.class)
public abstract class TransformSmithingRecipeJsonProviderMixin implements FabricRecipeSerializer<SmithingTransformRecipe> {
	@Override
	public JsonObject toJson(SmithingTransformRecipe recipe) {
		var accessor = (TransformSmithingRecipeAccessor) recipe;
		return new SmithingTransformRecipeJsonBuilder.SmithingTransformRecipeJsonProvider(
				recipe.getId(),
				this,
				accessor.getTemplate(), accessor.getBase(), accessor.getAddition(),
				recipe.getOutput(null).getItem(),
				null, null
		).toJson();
	}
}
