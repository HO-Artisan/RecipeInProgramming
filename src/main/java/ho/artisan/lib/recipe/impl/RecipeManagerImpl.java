/*
 * Copyright 2022 The Quilt Project
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

package ho.artisan.lib.recipe.impl;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonWriter;
import com.mojang.logging.LogUtils;
import ho.artisan.lib.recipe.api.serializer.FabricRecipeSerializer;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;

import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import ho.artisan.lib.recipe.api.RecipeLoadingEvents;

import net.fabricmc.fabric.api.event.registry.DynamicRegistrySetupCallback;
import net.fabricmc.fabric.api.event.registry.DynamicRegistryView;
import net.fabricmc.loader.api.FabricLoader;

@ApiStatus.Internal
public final class RecipeManagerImpl implements DynamicRegistrySetupCallback {
	/**
	 * Stores the static recipes which are added to the {@link net.minecraft.recipe.RecipeManager} when recipes are
	 * loaded.
	 */
	private static final Map<Identifier, Recipe<?>> STATIC_RECIPES = new Object2ObjectOpenHashMap<>();
	static final boolean DEBUG_MODE = Boolean.getBoolean("quilt.recipe.debug");
	private static final boolean DUMP_MODE = Boolean.getBoolean("quilt.recipe.dump");
	static final Logger LOGGER = LogUtils.getLogger();
	private static DynamicRegistryManager currentRegistryManager;

	public static void registerStaticRecipe(Recipe<?> recipe) {
		if (STATIC_RECIPES.putIfAbsent(recipe.getId(), recipe) != null) {
			throw new IllegalArgumentException("Cannot register " + recipe.getId()
					+ " as another recipe with the same identifier already exists.");
		}
	}

	public static void apply(Map<Identifier, JsonElement> map,
			Map<RecipeType<?>, ImmutableMap.Builder<Identifier, Recipe<?>>> builderMap,
			ImmutableMap.Builder<Identifier, Recipe<?>> globalRecipeMapBuilder) {
		var handler = new RegisterRecipeHandlerImpl(map, builderMap, globalRecipeMapBuilder, currentRegistryManager);
		RecipeLoadingEvents.ADD.invoker().addRecipes(handler);
		STATIC_RECIPES.values().forEach(handler::tryRegister);
		LOGGER.info("Registered {} custom recipes.", handler.registered);
	}

	public static void applyModifications(RecipeManager recipeManager,
			Map<RecipeType<?>, Map<Identifier, Recipe<?>>> recipes,
			Map<Identifier, Recipe<?>> globalRecipes) {
		var handler = new ModifyRecipeHandlerImpl(recipeManager, recipes, globalRecipes, currentRegistryManager);
		RecipeLoadingEvents.MODIFY.invoker().modifyRecipes(handler);
		LOGGER.info("Modified {} recipes.", handler.counter);

		var removeHandler = new RemoveRecipeHandlerImpl(recipeManager, recipes, globalRecipes, currentRegistryManager);
		RecipeLoadingEvents.REMOVE.invoker().removeRecipes(removeHandler);
		LOGGER.info("Removed {} recipes.", removeHandler.counter);

		if (DUMP_MODE) {
			dump(globalRecipes);
		}

		if (DEBUG_MODE || FabricLoader.getInstance().isDevelopmentEnvironment()) {
			for (var serializerEntry : Registries.RECIPE_SERIALIZER.getEntrySet()) {
				if (!(serializerEntry.getValue() instanceof FabricRecipeSerializer)) {
					LOGGER.warn(
							"Recipe serializer {} doesn't implement FabricRecipeSerializer. For full compatibility, the interface should be implemented.",
							serializerEntry.getKey().getValue()
					);
				}
			}
		}

		currentRegistryManager = null;
	}

	@SuppressWarnings("unchecked")
	private static void dump(Map<Identifier, Recipe<?>> recipes) {
		Path debugPath = Paths.get("debug", "quilt", "recipe").normalize();

		if (!Files.exists(debugPath)) {
			try {
				Files.createDirectories(debugPath);
			} catch (IOException e) {
				LOGGER.error("Failed to create debug directory for recipe dumping.", e);
				return;
			}
		}

		for (Recipe<?> recipe : recipes.values()) {
			if (!(recipe.getSerializer() instanceof FabricRecipeSerializer)) continue;

			var serializer = (FabricRecipeSerializer<Recipe<?>>) recipe.getSerializer();
			JsonObject serialized = serializer.toJson(recipe);

			Path path = debugPath.resolve(recipe.getId().getNamespace() + "/recipes/" + recipe.getId().getPath() + ".json");
			Path parent = path.getParent();

			if (!Files.exists(parent)) {
				try {
					Files.createDirectories(parent);
				} catch (IOException e) {
					LOGGER.error("Failed to create parent recipe directory {}. Cannot dump recipe {}.",
							parent, recipe.getId(), e);
					continue;
				}
			}

			var stringWriter = new StringWriter();
			var jsonWriter = new JsonWriter(stringWriter);
			jsonWriter.setLenient(true);
			jsonWriter.setIndent("  ");

			try {
				Streams.write(serialized, jsonWriter);
				Files.writeString(path, stringWriter.toString(),
						StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
			} catch (IOException e) {
				LOGGER.error("Failed to write JSON for recipe {}.", recipe.getId(), e);
			} finally {
				try {
					jsonWriter.close();
				} catch (IOException e) {
					LOGGER.error("Failed to close JSON writer for recipe {}.", recipe.getId(), e);
				}
			}
		}
	}

	@Override
	public void onRegistrySetup(DynamicRegistryView registryView) {
		currentRegistryManager = (DynamicRegistryManager) registryView;
	}
}
