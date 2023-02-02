package de.jeff_media.drop2inventory.utils;

import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.*;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class AutoSmelter {

    private final Map<Material, SmeltRecipeData> smeltRecipeDataMap = new HashMap<>();

    private static class SmeltRecipeData {

        public final ItemStack input;
        public final ItemStack output;
        public float experience;

        public SmeltRecipeData(ItemStack input, ItemStack output, float xp) {
            this.input = input;
            this.output = output;
            this.experience = xp;
        }
    }

    {
        Iterator<Recipe> iterator = Bukkit.recipeIterator();
        while(iterator.hasNext()) {
            Recipe recipe = iterator.next();
            if(!(recipe instanceof FurnaceRecipe)) continue;
            CookingRecipe cookingRecipe = (FurnaceRecipe) recipe;
            ItemStack output = cookingRecipe.getResult().clone();
            ItemStack input = cookingRecipe.getInput();
            float xp = cookingRecipe.getExperience();
            smeltRecipeDataMap.put(input.getType(), new SmeltRecipeData(input, output, xp));
        }
    }

    public ItemStack transform(ItemStack itemStack) {
        int amount = itemStack.getAmount();
        SmeltRecipeData smeltRecipeData = smeltRecipeDataMap.get(itemStack.getType());
        if(smeltRecipeData == null) return itemStack;
        ItemStack clonedResult = smeltRecipeData.output.clone();
        clonedResult.setAmount(amount);
        return clonedResult;
    }

}
