package de.jeff_media.drop2inventory.utils;

import com.jeff_media.jefflib.NumberUtils;
import de.jeff_media.drop2inventory.Main;
import de.jeff_media.drop2inventory.config.Config;
import de.jeff_media.drop2inventory.config.Permissions;
import de.jeff_media.morepersistentdatatypes.DataType;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class AutoSmelter {

    private final Main main;

    private final Map<Material, SmeltRecipeData> smeltRecipeDataMap = new EnumMap<>(Material.class);
    @Getter private final NamespacedKey autoSmeltKey;
    private final Map<UUID, Float> experienceToGive = new HashMap<>();

    public AutoSmelter(Main main) {
        this.main = main;
        this.autoSmeltKey = new NamespacedKey(main, "has-autosmelt-enabled");
        loadRecipes();
    }

    private static class SmeltRecipeData {

        //public final ItemStack input;
        //public final Set<Material> inputMaterials;
        public final ItemStack output;
        public float experience;

        public SmeltRecipeData(/*ItemStack input, *//*Set<Material> inputMaterials, */ItemStack output, float xp) {
            //this.input = input;
            this.output = output;
            this.experience = xp;
            //this.inputMaterials = inputMaterials;
        }
    }

    private String recipe2String(CookingRecipe recipe) {
        String input = recipe.getInput().toString();
        String inputChoice = recipe.getInputChoice().toString();
        String output = recipe.getResult().toString();
        float exp = recipe.getExperience();
        return "input=" + input + ", inputChoice=" + inputChoice + ", output=" + output + " exp=" + exp;
    }

    private void loadRecipes() {
        Iterator<Recipe> iterator = Bukkit.recipeIterator();
        while(iterator.hasNext()) {
            Recipe recipe = iterator.next();
            //if(!(recipe instanceof FurnaceRecipe)) continue;
            //FurnaceRecipe cookingRecipe = (FurnaceRecipe) recipe;
            if(!(recipe instanceof CookingRecipe)) continue;
            CookingRecipe cookingRecipe = (CookingRecipe) recipe;
            if(main.isDebug()) {
                main.debug("Found cooking recipe: " + recipe2String(cookingRecipe)/*cookingRecipe.getInput().getType() + " -> " + cookingRecipe.getResult().getType()*/);
            }
            ItemStack output = cookingRecipe.getResult().clone();
            //ItemStack input = cookingRecipe.getInput();
            RecipeChoice choice = cookingRecipe.getInputChoice();
            Set<Material> materialChoices = null;
            if(choice != null) {
                if(choice instanceof RecipeChoice.MaterialChoice) {
                    RecipeChoice.MaterialChoice materialChoice = (RecipeChoice.MaterialChoice) choice;
                    materialChoices = new HashSet<>(materialChoice.getChoices());
                } else {
                    main.debug("Unknown RecipeChoice type: " + choice.getClass().getName());
                }
            } else {
                materialChoices = Collections.emptySet();
            }
            float xp = cookingRecipe.getExperience();
            for(Material material : materialChoices) {
                smeltRecipeDataMap.put(material, new SmeltRecipeData(/*input, *//*materialChoices, */output, xp));
            }
            // smeltRecipeDataMap.put(input.getType(), new SmeltRecipeData(/*input, *//*materialChoices, */output, xp));
        }
    }

    public boolean hasEnabled(Player player) {
        if(main.isDebug()) {
            System.out.println("AutoSmelter.hasEnabled: " + player.getName());
            System.out.println("Config: " + main.getConfig().getBoolean(Config.FORCE_AUTO_SMELT));
            System.out.println("Permission: " + player.hasPermission(Permissions.ALLOW_AUTO_SMELT));
            System.out.println("PDC: " + player.getPersistentDataContainer().has(autoSmeltKey, DataType.BOOLEAN));
        }
        return (main.getConfig().getBoolean(Config.FORCE_AUTO_SMELT) || player.getPersistentDataContainer().has(autoSmeltKey, DataType.BOOLEAN)) && player.hasPermission(Permissions.ALLOW_AUTO_SMELT);
    }

    @Nullable
    public ItemStack transform(Player player, ItemStack itemStack) {
        int amount = itemStack.getAmount();
        SmeltRecipeData smeltRecipeData = smeltRecipeDataMap.get(itemStack.getType());
        if(smeltRecipeData == null) return null;
        ItemStack clonedResult = smeltRecipeData.output.clone();
        float exp = smeltRecipeData.experience * amount;
        if(main.isDebug()) {
            main.debug("AutoSmelter.transform");
            main.debug("  Amount : " + amount);
            main.debug("  Input  : " + itemStack.getType());
            main.debug("  Output : " + clonedResult.getType());
            main.debug("  Base XP: " + smeltRecipeData.experience);
            main.debug("  Mult.XP: " + exp);
        }
        if(main.getConfig().getBoolean(Config.AUTOSMELT_GIVES_XP)) {
            applyExp(player, exp);
        }
        clonedResult.setAmount(amount);
        return clonedResult;
    }

    private void applyExp(Player player, float exp) {
        if(NumberUtils.isZeroOrNegative(exp)) return;
        float currentToGive = experienceToGive.getOrDefault(player.getUniqueId(), 0f);
        if(main.isDebug()) {
            main.debug("  XP left to give: " + currentToGive);
        }
        currentToGive += exp;
        if(main.isDebug()) {
            main.debug("  XP to give after adding: " + currentToGive);
        }
        int floorToGive = (int) Math.floor(currentToGive);
        if(main.isDebug()) {
            main.debug("  XP to give after floor: " + floorToGive);
        }
        if(floorToGive > 0) {
            main.debug("  Giving XP: " + floorToGive);
            player.giveExp(floorToGive);
        }
        currentToGive -= floorToGive;
        if(main.isDebug()) {
            main.debug("  XP left to give after subtracting: " + currentToGive);
        }
        if(NumberUtils.isZeroOrNegative(currentToGive)) {
            if(main.isDebug()) {
                main.debug("  XP left to give is zero or negative, removing from map");
            }
            experienceToGive.remove(player.getUniqueId());
        } else {
            if(main.isDebug()) {
                main.debug("  XP left to give is positive, putting back into map: " + currentToGive);
            }
            experienceToGive.put(player.getUniqueId(), currentToGive);
        }
    }


}
