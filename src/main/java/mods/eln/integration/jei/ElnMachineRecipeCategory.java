package mods.eln.integration.jei;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.item.ItemStack;

import java.util.List;

/**
 * A generic JEI/HEI recipe category for an eln machine: one input slot on the left,
 * a row of output slots on the right. Used for the Macerator, Compressor, Plate
 * Machine and Magnetizer (all share the simple input -> output[] recipe shape).
 */
public class ElnMachineRecipeCategory implements IRecipeCategory<ElnMachineRecipeWrapper> {

    private static final int INPUT_X = 4;
    private static final int SLOT_Y = 8;
    private static final int OUTPUT_X = 58;

    private final String uid;
    private final String title;
    private final IDrawable background;

    public ElnMachineRecipeCategory(IGuiHelper guiHelper, String uid, String title) {
        this.uid = uid;
        this.title = title;
        this.background = guiHelper.createBlankDrawable(120, 36);
    }

    @Override
    public String getUid() {
        return uid;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getModName() {
        return "Electrical Age: Re-Wired";
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, ElnMachineRecipeWrapper wrapper, IIngredients ingredients) {
        IGuiItemStackGroup stacks = recipeLayout.getItemStacks();

        stacks.init(0, true, INPUT_X, SLOT_Y);
        if (wrapper.getInput() != null) {
            stacks.set(0, wrapper.getInput());
        }

        List<ItemStack> outputs = wrapper.getOutputs();
        for (int i = 0; i < outputs.size(); i++) {
            int slot = 1 + i;
            stacks.init(slot, false, OUTPUT_X + i * 18, SLOT_Y);
            stacks.set(slot, outputs.get(i));
        }
    }
}
