<center>
<div align="center">

<img height="200" width="200" src="./icon/400x400-icon.png" alt="RecipeInProgramming Icon"/>

# RecipeInProgramming

A Lib mod, Take the add Recipe(Data) function to fabric event, so you can use the event to register recipes, and make some JSON can't do contents(Infinity Recipes).

Un-support Quilt, Because Quilt was added this feature, I port it to Fabric.

</div>
</center>

## Use in your project
We use jitpack maven (Temporary)

```groovy

repositories {
    maven { url "https://jitpack.io" }
}

dependencies {
    modImplementation "com.github.HO-Artisan:RecipeInProgramming:Tag"
}

```

## Features

Now Support:
- Shaped Crafting；
- Shapeless Crafting；
- Smelting Recipe；
- Cutting Recipe；
- Smithing Recipe；
- Any of the recipes that use json!

How to use it: [Quilt Recipe API Wiki](https://modder.wiki.quiltmc.org/versions/1.19/data/recipes/recipe_api/)