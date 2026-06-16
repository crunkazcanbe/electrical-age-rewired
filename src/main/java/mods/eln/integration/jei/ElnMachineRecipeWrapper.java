package mods.eln.integration.jei;

import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import mods.eln.misc.Recipe;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Wraps a single eln machine {@link Recipe} (input -> output[] + energy) for display in JEI/HEI.
 */
public class ElnMachineRecipeWrapper implements IRecipeWrapper {

    private final ItemStack input;
    private final List<ItemStack> outputs;
    private final double energy;

    public ElnMachineRecipeWrapper(Recipe recipe) {
        this.input = recipe.input;
        this.outputs = new ArrayList<ItemStack>(Arrays.asList(recipe.getOutputCopy()));
        this.energy = recipe.energy;
    }

    public ItemStack getInput() {
        return input;
    }

    public List<ItemStack> getOutputs() {
        return outputs;
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients.setInput(VanillaTypes.ITEM, input);
        ingredients.setOutputs(VanillaTypes.ITEM, outputs);
    }

    @Override
    public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        if (energy > 0) {
            String text = String.format("%,.0f J", energy);
            minecraft.fontRenderer.drawString(text, 2, recipeHeight - 9, 0x707070);
        }
    }
}
