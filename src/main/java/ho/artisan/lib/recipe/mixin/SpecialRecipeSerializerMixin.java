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

import java.util.Objects;

import com.google.gson.JsonObject;
import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.SpecialRecipeSerializer;
import net.minecraft.util.registry.Registry;

import ho.artisan.lib.recipe.api.serializer.FabricRecipeSerializer;

@Mixin(SpecialRecipeSerializer.class)
public abstract class SpecialRecipeSerializerMixin<T extends Recipe<?>> implements FabricRecipeSerializer<T> {
	@Override
	public JsonObject toJson(T recipe) {
		var json = new JsonObject();
		json.addProperty("type", Objects.requireNonNull(Registry.RECIPE_SERIALIZER.getId(this)).toString());
		return json;
	}
}
