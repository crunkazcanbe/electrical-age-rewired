package mods.eln.integration.jei;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import mods.eln.init.Descriptors;
import mods.eln.misc.Recipe;
import mods.eln.misc.RecipesList;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * JEI / HadEnoughItems integration for Electrical Age: Re-Wired.
 *
 * Exposes the processing recipes of eln's machines (Macerator, Compressor, Plate
 * Machine, Magnetizer) in the item viewer, and registers each machine block as the
 * catalyst so right-clicking it shows what it can make. Soft dependency: this class
 * is only loaded when JEI/HEI is present.
 */
@JEIPlugin
public class ElnJeiPlugin implements IModPlugin {

    public static final String MACERATOR = "eln.macerator";
    public static final String COMPRESSOR = "eln.compressor";
    public static final String PLATE_MACHINE = "eln.plate_machine";
    public static final String MAGNETIZER = "eln.magnetizer";

    @Override
    public void registerCategories(IRecipeCategoryRegistration registry) {
        IGuiHelper guiHelper = registry.getJeiHelpers().getGuiHelper();
        registry.addRecipeCategories(
            new ElnMachineRecipeCategory(guiHelper, MACERATOR, "Macerator"),
            new ElnMachineRecipeCategory(guiHelper, COMPRESSOR, "Compressor"),
            new ElnMachineRecipeCategory(guiHelper, PLATE_MACHINE, "Plate Machine"),
            new ElnMachineRecipeCategory(guiHelper, MAGNETIZER, "Magnetizer")
        );
    }

    @Override
    public void register(IModRegistry registry) {
        registerMachine(registry, Descriptors.maceratorRecipes, MACERATOR);
        registerMachine(registry, Descriptors.compressorRecipes, COMPRESSOR);
        registerMachine(registry, Descriptors.plateMachineRecipes, PLATE_MACHINE);
        registerMachine(registry, Descriptors.magnetizerRecipes, MAGNETIZER);
    }

    private static void registerMachine(IModRegistry registry, RecipesList list, String uid) {
        if (list == null) return;

        List<ElnMachineRecipeWrapper> wrappers = new ArrayList<ElnMachineRecipeWrapper>();
        for (Recipe recipe : list.getRecipes()) {
            if (recipe != null && recipe.input != null && !recipe.input.isEmpty()) {
                wrappers.add(new ElnMachineRecipeWrapper(recipe));
            }
        }
        registry.addRecipes(wrappers, uid);

        // eln already tracks which machine items use each recipe list -> use them as catalysts.
        for (ItemStack machine : list.getMachines()) {
            if (machine != null && !machine.isEmpty()) {
                registry.addRecipeCatalyst(machine, uid);
            }
        }
    }
}
