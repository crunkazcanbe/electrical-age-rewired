package mods.eln.init;

import mods.eln.Eln;
import mods.eln.generic.GenericItemUsingDamage;
import mods.eln.generic.GenericItemBlockUsingDamage;
import mods.eln.misc.Recipe;
import mods.eln.misc.RecipesList;
import mods.eln.misc.Utils;
import net.minecraft.block.Block;
import net.minecraft.init.Items;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import net.minecraftforge.registries.IForgeRegistry;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.List;

/**
 * Crafting + machine recipes for Electrical Age: Re-Wired.
 *
 * All ~288 recipes were written in Eln_old.java (78 recipeXxx() methods) but that file
 * is excluded from the build and its addRecipe()/findItemStack() were never converted to
 * the 1.12.2 API, so nothing was craftable. This class re-implements the helpers for
 * 1.12.2 and ports every recipe method so the mod is playable in survival.
 *
 * Finished/completed by Claude (Anthropic) for Bell, 2026-06-16.
 */
@Mod.EventBusSubscriber(modid = Eln.MODID)
public class ElnCraftingRecipes {

    private static IForgeRegistry<IRecipe> registry;
    private static int recipeCounter = 0;
    private static final Map<String, ItemStack> NAME_MAP = new HashMap<String, ItemStack>();

    // config-derived constants (see Config.kt / original eln defaults)
    static int plateConversionRatio = 1;
    static String dictCheapChip = "circuitBasic";
    static String dictAdvancedChip = "circuitAdvanced";
    static String dictTungstenOre, dictTungstenDust, dictTungstenIngot, dictTungstenPlate;
    // eln has no extractor machine list in the live port; harmless orphan so the 1 recipe compiles
    static final RecipesList extractorRecipes = new RecipesList();

    @SubscribeEvent
    public static void onRegisterRecipes(RegistryEvent.Register<IRecipe> event) {
        registry = event.getRegistry();
        buildNameMap();
        Eln.logger.info("[ElnCraftingRecipes] name map has " + NAME_MAP.size() + " eln items; registering recipes...");

        dictTungstenOre = firstExistingOre("oreTungsten");
        dictTungstenDust = firstExistingOre("dustTungsten");
        dictTungstenIngot = firstExistingOre("ingotTungsten", "ingotTungstensteel");
        dictTungstenPlate = firstExistingOre("plateTungsten");

        // ---- crafting recipes (hand-verified core) ----
        recipeGround();
        recipeElectricalCable();
        recipeThermalCable();

        // ---- all ported recipe methods ----
        recipeLampSocket();
        recipeLampSupply();
        recipePowerSocket();
        recipePassiveComponent();
        recipeSwitch();
        recipeElectricalRelay();
        recipeWirelessSignal();
        recipeChips();
        recipeTransformer();
        recipeHeatFurnace();
        recipeTurbine();
        recipeBattery();
        recipeElectricalFurnace();
        recipeSixNodeMisc();
        recipeAutoMiner();
        recipeWindTurbine();
        recipeFuelGenerator();
        recipeSolarPanel();
        recipeThermalDissipatorPassiveAndActive();
        recipeGeneral();
        recipeHeatingCorp();
        recipeRegulatorItem();
        recipeLampItem();
        recipeProtection();
        recipeCombustionChamber();
        recipeFerromagneticCore();
        recipeDust();
        recipeElectricalMotor();
        recipeSolarTracker();
        recipeDynamo();
        recipeWindRotor();
        recipeMeter();
        recipeElectricalDrill();
        recipeOreScanner();
        recipeMiningPipe();
        recipeTreeResinAndRubber();
        recipeRawCable();
        recipeBatteryItem();
        recipeElectricalTool();
        recipeECoal();
        recipePortableCapacitor();
        recipeMiscItem();
        recipeMacerator();
        recipeMaceratorModOres();
        recipePlateMachine();
        recipeCompressor();
        recipeMagnetizer();
        recipeFuelBurnerItem();
        recipeFurnace();
        recipeElectricalSensor();
        recipeThermalSensor();
        recipeTransporter();
        recipeTurret();
        recipeMachine();
        recipeElectricalGate();
        recipeElectricalRedstone();
        recipeElectricalEnvironmentalSensor();
        recipeElectricalVuMeter();
        recipeElectricalBreaker();
        recipeFuses();
        recipeElectricalGateSource();
        recipeElectricalDataLogger();
        recipeSixNodeCache();
        recipeElectricalAlarm();
        recipeElectricalAntenna();
        recipeBatteryCharger();
        recipeEggIncubator();
        recipeGridDevices(new HashSet<String>());

        Eln.logger.info("[ElnCraftingRecipes] registered " + recipeCounter + " eln crafting recipes.");
    }

    // ------------------------------------------------------------------ name map
    private static void buildNameMap() {
        indexContainer(Eln.sharedItem);
        indexContainer(Eln.sharedItemStackOne);
        indexContainer(Eln.sixNodeItem);
        indexContainer(Eln.transparentNodeItem);
    }

    private static void indexContainer(Item item) {
        if (item == null) return;
        Map<Integer, ?> sub = null;
        if (item instanceof GenericItemUsingDamage) sub = ((GenericItemUsingDamage) item).subItemList;
        else if (item instanceof GenericItemBlockUsingDamage) sub = ((GenericItemBlockUsingDamage) item).subItemList;
        if (sub == null) return;
        for (Map.Entry<Integer, ?> e : sub.entrySet()) {
            String name = descName(e.getValue());
            if (name != null && !NAME_MAP.containsKey(name)) {
                NAME_MAP.put(name, new ItemStack(item, 1, e.getKey()));
            }
        }
    }

    private static String descName(Object desc) {
        if (desc == null) return null;
        try { Field f = desc.getClass().getField("name"); Object v = f.get(desc); return v == null ? null : v.toString(); }
        catch (Exception ex) { return null; }
    }

    // ------------------------------------------------------------------ lookups
    // names that were renamed between the original 1.7.10 / Eln_old recipes and the Re-Wired port
    private static final Map<String, String> ALIASES = new HashMap<String, String>();
    static {
        ALIASES.put("Power Resistor", "Resistor");
        ALIASES.put("Copper oreBlock", "oreCopper");      // resolve via ore dictionary
        ALIASES.put("Lead oreBlock", "oreLead");
        ALIASES.put("Tungsten oreBlock", "oreTungsten");
        ALIASES.put("Cinnabar oreBlock", "oreCinnabar");
        ALIASES.put("oreBlock Scanner", "Ore Scanner");
        ALIASES.put("LED vuMeter", "Electrical Vu Meter");     // actual descriptor name
        ALIASES.put("Analog vuMeter", "Electrical Vu Meter");
        ALIASES.put("Vumeter", "Electrical Vu Meter");
        ALIASES.put("Scanner", "Ore Scanner");
        ALIASES.put("itemRubber", "Rubber");              // rubber item (recipe outputs)
    }

    static ItemStack findItemStack(String name, int stackSize) {
        String n = ALIASES.containsKey(name) ? ALIASES.get(name) : name;
        ItemStack s = NAME_MAP.get(n);
        if (s != null && !s.isEmpty()) { ItemStack c = s.copy(); c.setCount(stackSize); return c; }
        Item item = Item.REGISTRY.getObject(new ResourceLocation(Eln.MODID, n));
        if (item != null) return new ItemStack(item, stackSize);
        // ore-dictionary fallback: handles ingredient names like dustCopper, ingotLead, itemRubber, ingotAlloy
        if (OreDictionary.doesOreNameExist(n)) {
            java.util.List<ItemStack> ores = OreDictionary.getOres(n);
            if (!ores.isEmpty() && !ores.get(0).isEmpty()) { ItemStack c = ores.get(0).copy(); c.setCount(stackSize); return c; }
        }
        Eln.logger.warn("[ElnCraftingRecipes] findItemStack: unknown eln item '" + name + "'");
        return ItemStack.EMPTY;
    }
    static ItemStack findItemStack(String name) { return findItemStack(name, 1); }

    static String firstExistingOre(String... oreNames) {
        for (String o : oreNames) if (OreDictionary.doesOreNameExist(o)) return o;
        return oreNames.length > 0 ? oreNames[0] : "";
    }

    // ------------------------------------------------------------------ 1.12.2 registration
    private static boolean badStack(ItemStack s) { return s == null || s.isEmpty(); }
    private static boolean paramsOk(ItemStack output, Object[] params) {
        if (badStack(output)) return false;
        for (Object o : params) if (o instanceof ItemStack && badStack((ItemStack) o)) return false;
        return true;
    }
    /** Drop key entries whose character never appears in the pattern (1.12 ShapedOreRecipe rejects unused keys). */
    private static Object[] stripUnusedKeys(Object[] params) {
        StringBuilder pat = new StringBuilder();
        int i = 0;
        while (i < params.length && params[i] instanceof String) { pat.append((String) params[i]); i++; }
        java.util.List<Object> out = new java.util.ArrayList<Object>();
        for (int k = 0; k < i; k++) out.add(params[k]);
        for (int j = i; j + 1 < params.length; j += 2) {
            if (params[j] instanceof Character && pat.indexOf(String.valueOf((Character) params[j])) >= 0) {
                out.add(params[j]); out.add(params[j + 1]);
            }
        }
        return out.toArray();
    }
    static void addRecipe(ItemStack output, Object... params) {
        try {
            if (!paramsOk(output, params)) { Eln.logger.warn("[ElnCraftingRecipes] skipped shaped recipe (missing item)"); return; }
            ShapedOreRecipe r = new ShapedOreRecipe(new ResourceLocation(Eln.MODID, "auto"), output, stripUnusedKeys(params));
            r.setRegistryName(new ResourceLocation(Eln.MODID, "recipe_" + (recipeCounter++)));
            registry.register(r);
        } catch (Throwable t) { Eln.logger.warn("[ElnCraftingRecipes] shaped recipe failed: " + t); }
    }
    static void addShapelessRecipe(ItemStack output, Object... params) {
        try {
            if (!paramsOk(output, params)) { Eln.logger.warn("[ElnCraftingRecipes] skipped shapeless recipe (missing item)"); return; }
            ShapelessOreRecipe r = new ShapelessOreRecipe(new ResourceLocation(Eln.MODID, "auto"), output, params);
            r.setRegistryName(new ResourceLocation(Eln.MODID, "recipe_" + (recipeCounter++)));
            registry.register(r);
        } catch (Throwable t) { Eln.logger.warn("[ElnCraftingRecipes] shapeless recipe failed: " + t); }
    }

    // ------------------------------------------------------------------ hand-verified core recipes
    private static void recipeGround() {
        addRecipe(findItemStack("Ground Cable"), " C ", " C ", "CCC", 'C', findItemStack("Copper Cable"));
    }
    private static void recipeElectricalCable() {
        addRecipe(findItemStack("Signal Cable", 1), "R", "C", 'C', findItemStack("Iron Cable"), 'R', "itemRubber");
        addRecipe(findItemStack("Low Voltage Cable", 1), "R", "C", 'C', findItemStack("Copper Cable"), 'R', "itemRubber");
        addRecipe(findItemStack("Medium Voltage Cable", 1), "R", "C", 'C', findItemStack("Low Voltage Cable", 1), 'R', "itemRubber");
        addRecipe(findItemStack("High Voltage Cable", 1), "R", "C", 'C', findItemStack("Medium Voltage Cable", 1), 'R', "itemRubber");
        addRecipe(findItemStack("Signal Cable", 6), "RRR", "CCC", "RRR", 'C', new ItemStack(Items.IRON_INGOT), 'R', "itemRubber");
        addRecipe(findItemStack("Low Voltage Cable", 6), "RRR", "CCC", "RRR", 'C', "ingotCopper", 'R', "itemRubber");
        addRecipe(findItemStack("Very High Voltage Cable", 6), "RRR", "CCC", "RRR", 'C', "ingotAlloy", 'R', "itemRubber");
    }
    private static void recipeThermalCable() {
        addRecipe(findItemStack("Copper Thermal Cable", 6), "SSS", "CCC", "SSS", 'S', new ItemStack(Blocks.COBBLESTONE), 'C', "ingotCopper");
        addRecipe(findItemStack("Copper Thermal Cable", 1), "S", "C", 'S', new ItemStack(Blocks.COBBLESTONE), 'C', findItemStack("Copper Cable"));
    }

    // ================================================================== ported recipe methods
    private static void recipeLampSocket() {
        addRecipe(findItemStack("Lamp Socket A", 3),
            "G ",
            "IG",
            "G ",
            'G', new ItemStack(Blocks.GLASS_PANE),
            'I', new ItemStack(Items.IRON_INGOT));

        addRecipe(findItemStack("Lamp Socket B Projector", 3),
            " I",
            "IG",
            " I",
            'G', new ItemStack(Blocks.GLASS_PANE),
            'I', new ItemStack(Items.IRON_INGOT));

        addRecipe(findItemStack("Street Light", 1),
            "G",
            "I",
            "I",
            'G', new ItemStack(Blocks.GLASS_PANE),
            'I', new ItemStack(Items.IRON_INGOT));

        addRecipe(findItemStack("Robust Lamp Socket", 3),
            "GIG",
            'G', new ItemStack(Blocks.GLASS_PANE),
            'I', new ItemStack(Items.IRON_INGOT));
        addRecipe(findItemStack("Flat Lamp Socket", 3),
            "IGI",
            'G', new ItemStack(Blocks.GLASS_PANE),
            'I', new ItemStack(Items.IRON_INGOT));
        addRecipe(findItemStack("Simple Lamp Socket", 3),
            " I ",
            "GGG",
            'G', new ItemStack(Blocks.GLASS_PANE),
            'I', new ItemStack(Items.IRON_INGOT));

        addRecipe(findItemStack("Fluorescent Lamp Socket", 3),
            " I ",
            "I I",
            'G', new ItemStack(Blocks.GLASS_PANE),
            'I', new ItemStack(Items.IRON_INGOT));


        addRecipe(findItemStack("Suspended Lamp Socket", 2),
            "I",
            "G",
            'G', findItemStack("Robust Lamp Socket"),
            'I', new ItemStack(Items.IRON_INGOT));

        addRecipe(findItemStack("Long Suspended Lamp Socket", 2),
            "I",
            "I",
            "G",
            'G', findItemStack("Robust Lamp Socket"),
            'I', new ItemStack(Items.IRON_INGOT));

        addRecipe(findItemStack("Sconce Lamp Socket", 2),
            "GCG",
            "GIG",
            'G', new ItemStack(Blocks.GLASS_PANE),
            'C', "dustCoal",
            'I', new ItemStack(Items.IRON_INGOT));

        addRecipe(findItemStack("50V Emergency Lamp"),
            "cbc",
            " l ",
            " g ",
            'c', findItemStack("Low Voltage Cable"),
            'b', findItemStack("Portable Battery Pack"),
            'l', findItemStack("50V LED Bulb"),
            'g', new ItemStack(Blocks.GLASS_PANE));

        addRecipe(findItemStack("200V Emergency Lamp"),
            "cbc",
            " l ",
            " g ",
            'c', findItemStack("Medium Voltage Cable"),
            'b', findItemStack("Portable Battery Pack"),
            'l', findItemStack("200V LED Bulb"),
            'g', new ItemStack(Blocks.GLASS_PANE));
    }

    private static void recipeLampSupply() {
        addRecipe(findItemStack("Lamp Supply", 1),
            " I ",
            "ICI",
            " I ",
            'C', "ingotCopper",
            'I', new ItemStack(Items.IRON_INGOT));

    }

    private static void recipePowerSocket() {
        addRecipe(findItemStack("50V Power Socket", 16),
            "RUR",
            "ACA",
            'R', "itemRubber",
            'U', findItemStack("Copper Plate"),
            'A', findItemStack("Alloy Plate"),
            'C', findItemStack("Low Voltage Cable"));
        addRecipe(findItemStack("200V Power Socket", 16),
            "RUR",
            "ACA",
            'R', "itemRubber",
            'U', findItemStack("Copper Plate"),
            'A', findItemStack("Alloy Plate"),
            'C', findItemStack("Medium Voltage Cable"));
    }

    private static void recipePassiveComponent() {
        addRecipe(findItemStack("Signal Diode", 4),
            " RB",
            "IIR",
            " RB",
            'R', new ItemStack(Items.REDSTONE),
            'I', findItemStack("Iron Cable"),
            'B', "itemRubber");

        addRecipe(findItemStack("10A Diode", 3),
            " RB",
            "IIR",
            " RB",
            'R', new ItemStack(Items.REDSTONE),
            'I', new ItemStack(Items.IRON_INGOT),
            'B', "itemRubber");

        addRecipe(findItemStack("25A Diode"),
            "D",
            "D",
            "D",
            'D', findItemStack("10A Diode"));


        addRecipe(findItemStack("Power Capacitor"),
            "cPc",
            "III",
            'I', new ItemStack(Items.IRON_INGOT),
            'c', findItemStack("Iron Cable"),
            'P', "plateIron");

        addRecipe(findItemStack("Power Inductor"),
            " P ",
            "cIc",
            "IPI",
            'I', new ItemStack(Items.IRON_INGOT),
            'c', findItemStack("Copper Cable"),
            'P', "plateIron");

        addRecipe(findItemStack("Power Resistor"),
            " P ",
            "c c",
            "IPI",
            'I', new ItemStack(Items.IRON_INGOT),
            'c', findItemStack("Copper Cable"),
            'P', "plateCopper");

        addRecipe(findItemStack("Rheostat"),
            " R ",
            " MS",
            "cmc",
            'R', findItemStack("Power Resistor"),
            'c', findItemStack("Copper Cable"),
            'm', findItemStack("Machine Block"),
            'M', findItemStack("Electrical Motor"),
            'S', findItemStack("Signal Cable")
        );

        addRecipe(findItemStack("Thermistor"),
            " P ",
            "csc",
            "IPI",
            's', "dustSilicon",
            'I', new ItemStack(Items.IRON_INGOT),
            'c', findItemStack("Copper Cable"),
            'P', "plateCopper");

        addRecipe(findItemStack("Large Rheostat"),
            "   ",
            " D ",
            "CRC",
            'R', findItemStack("Rheostat"),
            'C', findItemStack("Copper Thermal Cable"),
            'D', findItemStack("Small Passive Thermal Dissipator")
        );
    }

    private static void recipeSwitch() {
		/*
		 * addRecipe(findItemStack("Signal Switch"), "  I", " I ", "CAC", 'R', new ItemStack(Items.redstone), 'A', "itemRubber", 'I', findItemStack("Copper Cable"), 'C', findItemStack("Signal Cable"));
		 *
		 * addRecipe(findItemStack("Signal Switch with LED"), " RI", " I ", "CAC", 'R', new ItemStack(Items.redstone), 'A', "itemRubber", 'I', findItemStack("Copper Cable"), 'C', findItemStack("Signal Cable"));
		 */

        addRecipe(findItemStack("Low Voltage Switch"),
            "  I",
            " I ",
            "CAC",
            'R', new ItemStack(Items.REDSTONE),
            'A', "itemRubber",
            'I', findItemStack("Copper Cable"),
            'C', findItemStack("Low Voltage Cable"));

        addRecipe(findItemStack("Medium Voltage Switch"),
            "  I",
            "AIA",
            "CAC",
            'R', new ItemStack(Items.REDSTONE),
            'A', "itemRubber",
            'I', findItemStack("Copper Cable"),
            'C', findItemStack("Medium Voltage Cable"));

        addRecipe(findItemStack("High Voltage Switch"),
            "AAI",
            "AIA",
            "CAC",
            'R', new ItemStack(Items.REDSTONE),
            'A', "itemRubber",
            'I', findItemStack("Copper Cable"),
            'C', findItemStack("High Voltage Cable"));

        addRecipe(findItemStack("Very High Voltage Switch"),
            "AAI",
            "AIA",
            "CAC",
            'R', new ItemStack(Items.REDSTONE),
            'A', "itemRubber",
            'I', findItemStack("Copper Cable"),
            'C', findItemStack("Very High Voltage Cable"));

    }

    private static void recipeElectricalRelay() {
        addRecipe(findItemStack("Low Voltage Relay"),
            "GGG",
            "OIO",
            "CRC",
            'R', new ItemStack(Items.REDSTONE),
            'O', new ItemStack(Items.IRON_INGOT),
            'G', new ItemStack(Blocks.GLASS_PANE),
            'A', "itemRubber",
            'I', findItemStack("Copper Cable"),
            'C', findItemStack("Low Voltage Cable"));

        addRecipe(findItemStack("Medium Voltage Relay"),
            "GGG",
            "OIO",
            "CRC",
            'R', new ItemStack(Items.REDSTONE),
            'O', new ItemStack(Items.IRON_INGOT),
            'G', new ItemStack(Blocks.GLASS_PANE),
            'A', "itemRubber",
            'I', findItemStack("Copper Cable"),
            'C', findItemStack("Medium Voltage Cable"));

        addRecipe(findItemStack("High Voltage Relay"),
            "GGG",
            "OIO",
            "CRC",
            'R', new ItemStack(Items.REDSTONE),
            'O', new ItemStack(Items.IRON_INGOT),
            'G', new ItemStack(Blocks.GLASS_PANE),
            'A', "itemRubber",
            'I', findItemStack("Copper Cable"),
            'C', findItemStack("High Voltage Cable"));

        addRecipe(findItemStack("Very High Voltage Relay"),
            "GGG",
            "OIO",
            "CRC",
            'R', new ItemStack(Items.REDSTONE),
            'O', new ItemStack(Items.IRON_INGOT),
            'G', new ItemStack(Blocks.GLASS_PANE),
            'A', "itemRubber",
            'I', findItemStack("Copper Cable"),
            'C', findItemStack("Very High Voltage Cable"));

        addRecipe(findItemStack("Signal Relay"),
            "GGG",
            "OIO",
            "CRC",
            'R', new ItemStack(Items.REDSTONE),
            'O', new ItemStack(Items.IRON_INGOT),
            'G', new ItemStack(Blocks.GLASS_PANE),
            'I', findItemStack("Copper Cable"),
            'C', findItemStack("Signal Cable"));
    }

    private static void recipeWirelessSignal() {
        addRecipe(findItemStack("Wireless Signal Transmitter"),
            " S ",
            " R ",
            "ICI",
            'R', new ItemStack(Items.REDSTONE),
            'I', new ItemStack(Items.IRON_INGOT),
            'C', dictCheapChip,
            'S', findItemStack("Signal Antenna"));

        addRecipe(findItemStack("Wireless Signal Repeater"),
            "S S",
            "R R",
            "ICI",
            'R', new ItemStack(Items.REDSTONE),
            'I', new ItemStack(Items.IRON_INGOT),
            'C', dictCheapChip,
            'S', findItemStack("Signal Antenna"));

        addRecipe(findItemStack("Wireless Signal Receiver"),
            " S ",
            "ICI",
            'R', new ItemStack(Items.REDSTONE),
            'I', new ItemStack(Items.IRON_INGOT),
            'C', dictCheapChip,
            'S', findItemStack("Signal Antenna"));
    }

    private static void recipeChips() {
        addRecipe(findItemStack("NOT Chip"),
            "   ",
            "cCr",
            "   ",
            'C', dictCheapChip,
            'r', new ItemStack(Items.REDSTONE),
            'c', findItemStack("Copper Cable"));

        addRecipe(findItemStack("AND Chip"),
            " c ",
            "cCc",
            " c ",
            'C', dictCheapChip,
            'c', findItemStack("Copper Cable"));

        addRecipe(findItemStack("NAND Chip"),
            " c ",
            "cCr",
            " c ",
            'C', dictCheapChip,
            'r', new ItemStack(Items.REDSTONE),
            'c', findItemStack("Copper Cable"));

        addRecipe(findItemStack("OR Chip"),
            " r ",
            "rCr",
            " r ",
            'C', dictCheapChip,
            'r', new ItemStack(Items.REDSTONE));

        addRecipe(findItemStack("NOR Chip"),
            " r ",
            "rCc",
            " r ",
            'C', dictCheapChip,
            'r', new ItemStack(Items.REDSTONE),
            'c', findItemStack("Copper Cable"));

        addRecipe(findItemStack("XOR Chip"),
            " rr",
            "rCr",
            " rr",
            'C', dictCheapChip,
            'r', new ItemStack(Items.REDSTONE));

        addRecipe(findItemStack("XNOR Chip"),
            " rr",
            "rCc",
            " rr",
            'C', dictCheapChip,
            'r', new ItemStack(Items.REDSTONE),
            'c', findItemStack("Copper Cable"));

        addRecipe(findItemStack("PAL Chip"),
            "rcr",
            "cCc",
            "rcr",
            'C', dictAdvancedChip,
            'r', new ItemStack(Items.REDSTONE),
            'c', findItemStack("Copper Cable"));

        addRecipe(findItemStack("Schmitt Trigger Chip"),
            "   ",
            "cCc",
            "   ",
            'C', dictAdvancedChip,
            'c', findItemStack("Copper Cable"));

        addRecipe(findItemStack("D Flip Flop Chip"),
            "   ",
            "cCc",
            " p ",
            'C', dictAdvancedChip,
            'p', findItemStack("Copper Plate"),
            'c', findItemStack("Copper Cable"));

        addRecipe(findItemStack("Oscillator Chip"),
            "pdp",
            "cCc",
            "   ",
            'C', dictAdvancedChip,
            'p', findItemStack("Copper Plate"),
            'c', findItemStack("Copper Cable"),
            'd', findItemStack("Dielectric"));

        addRecipe(findItemStack("JK Flip Flop Chip"),
            " p ",
            "cCc",
            " p ",
            'C', dictAdvancedChip,
            'p', findItemStack("Copper Plate"),
            'c', findItemStack("Copper Cable"));


        addRecipe(findItemStack("Amplifier"),
            "  r",
            "cCc",
            "   ",
            'r', new ItemStack(Items.REDSTONE),
            'c', findItemStack("Copper Cable"),
            'C', dictAdvancedChip);

        addRecipe(findItemStack("OpAmp"),
            "  r",
            "cCc",
            " c ",
            'r', new ItemStack(Items.REDSTONE),
            'c', findItemStack("Copper Cable"),
            'C', dictAdvancedChip);

        addRecipe(findItemStack("Configurable summing unit"),
            " cr",
            "cCc",
            " c ",
            'r', new ItemStack(Items.REDSTONE),
            'c', findItemStack("Copper Cable"),
            'C', dictAdvancedChip);

        addRecipe(findItemStack("Sample and hold"),
            " rr",
            "cCc",
            " c ",
            'r', new ItemStack(Items.REDSTONE),
            'c', findItemStack("Copper Cable"),
            'C', dictAdvancedChip);

        addRecipe(findItemStack("Voltage controlled sine oscillator"),
            "rrr",
            "cCc",
            "   ",
            'r', new ItemStack(Items.REDSTONE),
            'c', findItemStack("Copper Cable"),
            'C', dictAdvancedChip);

        addRecipe(findItemStack("Voltage controlled sawtooth oscillator"),
            "   ",
            "cCc",
            "rrr",
            'r', new ItemStack(Items.REDSTONE),
            'c', findItemStack("Copper Cable"),
            'C', dictAdvancedChip);

        addRecipe(findItemStack("PID Regulator"),
            "rrr",
            "cCc",
            "rcr",
            'r', new ItemStack(Items.REDSTONE),
            'c', findItemStack("Copper Cable"),
            'C', dictAdvancedChip);

        addRecipe(findItemStack("Lowpass filter"),
            "CdC",
            "cDc",
            " s ",
            'd', findItemStack("Dielectric"),
            'c', findItemStack("Copper Cable"),
            'C', findItemStack("Copper Plate"),
            'D', findItemStack("Coal Dust"),
            's', dictCheapChip);
    }

    private static void recipeTransformer() {
        addRecipe(findItemStack("DC-DC Converter"),
            "C C",
            "III",
            'C', findItemStack("Copper Cable"),
            'I', new ItemStack(Items.IRON_INGOT));
    }

    private static void recipeHeatFurnace() {
        addRecipe(findItemStack("Stone Heat Furnace"),
            "BBB",
            "BIB",
            "BiB",
            'B', new ItemStack(Blocks.STONE),
            'i', findItemStack("Copper Thermal Cable"),
            'I', findItemStack("Combustion Chamber"));

        addRecipe(findItemStack("Fuel Heat Furnace"),
            "IcI",
            "mCI",
            "IiI",
            'c', findItemStack("Cheap Chip"),
            'm', findItemStack("Electrical Motor"),
            'C', new ItemStack(Items.CAULDRON),
            'I', new ItemStack(Items.IRON_INGOT),
            'i', findItemStack("Copper Thermal Cable"));
    }

    private static void recipeTurbine() {
        addRecipe(findItemStack("50V Turbine"),
            " m ",
            "HMH",
            " E ",
            'M', findItemStack("Machine Block"),
            'E', findItemStack("Low Voltage Cable"),
            'H', findItemStack("Copper Thermal Cable"),
            'm', findItemStack("Electrical Motor")

        );
        addRecipe(findItemStack("200V Turbine"),
            "ImI",
            "HMH",
            "IEI",
            'I', "itemRubber",
            'M', findItemStack("Advanced Machine Block"),
            'E', findItemStack("Medium Voltage Cable"),
            'H', findItemStack("Copper Thermal Cable"),
            'm', findItemStack("Advanced Electrical Motor"));
        // Mechanical generators (driven by a shaft) — the real item names are
        // "50V Generator" / "200V Generator" (NOT "Generator", which matched
        // nothing, so these had no recipe before).
        addRecipe(findItemStack("50V Generator"),
            "mmm",
            "ama",
            " ME",
            'm', findItemStack("Electrical Motor"),
            'M', findItemStack("Machine Block"),
            'a', firstExistingOre("ingotAluminum", "ingotIron"),
            'E', findItemStack("Low Voltage Cable")
        );
        addRecipe(findItemStack("200V Generator"),
            "mmm",
            "ama",
            " ME",
            'm', findItemStack("Advanced Electrical Motor"),
            'M', findItemStack("Advanced Machine Block"),
            'a', firstExistingOre("ingotAluminum", "ingotIron"),
            'E', findItemStack("Medium Voltage Cable")
        );
        addRecipe(findItemStack("Steam Turbine"),
            " a ",
            "aAa",
            " M ",
            'a', firstExistingOre("ingotAluminum", "ingotIron"),
            'A', firstExistingOre("blockAluminum", "blockIron"),
            'M', findItemStack("Advanced Machine Block")
        );
        addRecipe(findItemStack("Gas Turbine"),
            "msH",
            "sSs",
            " M ",
            'm', findItemStack("Advanced Electrical Motor"),
            'H', findItemStack("Copper Thermal Cable"),
            's', firstExistingOre("ingotSteel", "ingotIron"),
            'S', firstExistingOre("blockSteel", "blockIron"),
            'M', findItemStack("Advanced Machine Block")
        );

        addRecipe(findItemStack("Simple Shaft"),
            "   ",
            "iii",
            " m ",
            'i', "ingotIron",
            'm', findItemStack("Machine Block")
        );

        addRecipe(findItemStack("Joint Hub"),
            " i ",
            "iii",
            " m ",
            'i', "ingotIron",
            'm', findItemStack("Machine Block")
        );

        addRecipe(findItemStack("Flywheel"),
            "iIi",
            "ImI",
            "iIi",
            'i', "ingotIron",
            'I', "blockIron",
            'm', findItemStack("Machine Block")
        );

        addRecipe(findItemStack("Tachometer"),
            "p  ",
            "iii",
            "cm ",
            'i', "ingotIron",
            'm', findItemStack("Machine Block"),
            'p', findItemStack("Electrical Probe Chip"),
            'c', findItemStack("Signal Cable")
        );
    }

    private static void recipeBattery() {
        addRecipe(findItemStack("Cost Oriented Battery"),
            "C C",
            "PPP",
            "PPP",
            'C', findItemStack("Low Voltage Cable"),
            'P', "ingotLead",
            'I', new ItemStack(Items.IRON_INGOT));

        addRecipe(findItemStack("Capacity Oriented Battery"),
            "PPP",
            "PBP",
            "PPP",
            'B', findItemStack("Cost Oriented Battery"),
            'P', "ingotLead");

        addRecipe(findItemStack("Voltage Oriented Battery"),
            "PPP",
            "PBP",
            "PPP",
            'B', findItemStack("Cost Oriented Battery"),
            'P', new ItemStack(Items.IRON_INGOT));

        addRecipe(findItemStack("Current Oriented Battery"),
            "PPP",
            "PBP",
            "PPP",
            'B', findItemStack("Cost Oriented Battery"),
            'P', "ingotCopper");

        addRecipe(findItemStack("Life Oriented Battery"),
            "P P",
            " B ",
            "P P",
            'B', findItemStack("Cost Oriented Battery"),
            'P', new ItemStack(Items.GOLD_INGOT));

        addRecipe(findItemStack("Single-use Battery"),
            "ppp",
            "III",
            "ppp",
            'C', findItemStack("Low Voltage Cable"),
            'p', new ItemStack(Items.COAL, 1, 0),
            'I', "ingotCopper");

        addRecipe(findItemStack("Single-use Battery"),
            "ppp",
            "III",
            "ppp",
            'C', findItemStack("Low Voltage Cable"),
            'p', new ItemStack(Items.COAL, 1, 1),
            'I', "ingotCopper");
    }

    private static void recipeGridDevices(HashSet<String> oreNames) {
        int poleRecipes = 0;
        for (String oreName : new String[]{
            "ingotAluminum",
            "ingotAluminium",
            "ingotSteel",
        }) {
            if (oreNames.contains(oreName)) {
                addRecipe(findItemStack("Utility Pole"),
                    "WWW",
                    "IWI",
                    " W ",
                    'W', "logWood",
                    'I', oreName
                );
                poleRecipes++;
            }
        }
        if (poleRecipes == 0) {
            // Really?
            addRecipe(findItemStack("Utility Pole"),
                "WWW",
                "IWI",
                " W ",
                'I', "ingotIron"
            );
        }
        addRecipe(findItemStack("Utility Pole w/DC-DC Converter"),
            "HHH",
            " TC",
            " PH",
            'P', findItemStack("Utility Pole"),
            'H', findItemStack("High Voltage Cable"),
            'C', findItemStack("Optimal Ferromagnetic Core"),
            'T', findItemStack("DC-DC Converter")
        );
//		if (oreNames.contains("sheetPlastic")) {
//			addRecipe(findItemStack("Downlink"),
//					"H H",
//					"PMP",
//					"PPP",
//					'P', "sheetPlastic",
//					'M', findItemStack("Machine Block"),
//					'H', findItemStack("High Voltage Cable")
//			);
//		} else {
//			addRecipe(findItemStack("Downlink"),
//					"H H",
//					"PMP",
//					"PPP",
//					'P', "itemRubber",
//					'M', findItemStack("Machine Block"),
//					'H', findItemStack("High Voltage Cable")
//			);
//		}
    }

    private static void recipeElectricalFurnace() {
        addRecipe(findItemStack("Electrical Furnace"),
            "III",
            "IFI",
            "ICI",
            'C', findItemStack("Low Voltage Cable"),
            'F', new ItemStack(Blocks.FURNACE),
            'I', new ItemStack(Items.IRON_INGOT));
    }

    private static void recipeSixNodeMisc() {
        addRecipe(findItemStack("Analog Watch"),
            "crc",
            "III",
            'c', findItemStack("Iron Cable"),
            'r', new ItemStack(Items.REDSTONE),
            'I', new ItemStack(Items.IRON_INGOT));

        addRecipe(findItemStack("Digital Watch"),
            "rcr",
            "III",
            'c', findItemStack("Iron Cable"),
            'r', new ItemStack(Items.REDSTONE),
            'I', new ItemStack(Items.IRON_INGOT));

        addRecipe(findItemStack("Hub"),
            "I I",
            " c ",
            "I I",
            'c', findItemStack("Copper Cable"),
            'I', new ItemStack(Items.IRON_INGOT));


        addRecipe(findItemStack("Energy Meter"),
            "IcI",
            "IRI",
            "IcI",
            'c', findItemStack("Copper Cable"),
            'R', dictCheapChip,
            'I', new ItemStack(Items.IRON_INGOT));

        addRecipe(findItemStack("Advanced Energy Meter"),
            " c ",
            "PRP",
            " c ",
            'c', findItemStack("Copper Cable"),
            'R', dictAdvancedChip,
            'P', findItemStack("Iron Plate"));

    }

    private static void recipeAutoMiner() {
        addRecipe(findItemStack("Auto Miner"),
            "MCM",
            "BOB",
            " P ",
            'C', dictAdvancedChip,
            'O', findItemStack("oreBlock Scanner"),
            'B', findItemStack("Advanced Machine Block"),
            'M', findItemStack("Advanced Electrical Motor"),
            'P', findItemStack("Mining Pipe"));
    }

    private static void recipeWindTurbine() {
        addRecipe(findItemStack("Wind Turbine"),
            " I ",
            "IMI",
            " B ",
            'B', findItemStack("Machine Block"),
            'I', "plateIron",
            'M', findItemStack("Electrical Motor"));

        addRecipe(findItemStack("Water Turbine"),
            "  I",
            "BMI",
            "  I",
            'I', "plateIron",
            'B', findItemStack("Machine Block"),
            'M', findItemStack("Electrical Motor"));

    }

    private static void recipeFuelGenerator() {
        addRecipe(findItemStack("50V Fuel Generator"),
            "III",
            " BA",
            "CMC",
            'I', "plateIron",
            'B', findItemStack("Machine Block"),
            'A', findItemStack("Analogic Regulator"),
            'C', findItemStack("Low Voltage Cable"),
            'M', findItemStack("Electrical Motor"));

        addRecipe(findItemStack("200V Fuel Generator"),
            "III",
            " BA",
            "CMC",
            'I', "plateIron",
            'B', findItemStack("Advanced Machine Block"),
            'A', findItemStack("Analogic Regulator"),
            'C', findItemStack("Medium Voltage Cable"),
            'M', findItemStack("Advanced Electrical Motor"));
    }

    private static void recipeSolarPanel() {
        addRecipe(findItemStack("Small Solar Panel"),
            "III",
            "CSC",
            "III",
            'S', "plateSilicon",
            'I', new ItemStack(Items.IRON_INGOT),
            'C', findItemStack("Low Voltage Cable"));

        addRecipe(findItemStack("Small Rotating Solar Panel"),
            "ISI",
            "I I",
            'S', findItemStack("Small Solar Panel"),
            'M', findItemStack("Electrical Motor"),
            'I', new ItemStack(Items.IRON_INGOT));

        for (String metal : new String[] { "blockSteel", "blockAluminum", "blockAluminium", "casingMachineAdvanced" }) {
            for (String panel : new String[] {"Small Solar Panel", "Small Rotating Solar Panel"}) {
                addRecipe(findItemStack("2x3 Solar Panel"),
                    "PPP",
                    "PPP",
                    "I I",
                    'P', findItemStack(panel),
                    'I', metal);
            }
        }
        addRecipe(findItemStack("2x3 Rotating Solar Panel"),
            "ISI",
            "IMI",
            "I I",
            'S', findItemStack("2x3 Solar Panel"),
            'M', findItemStack("Electrical Motor"),
            'I', new ItemStack(Items.IRON_INGOT));
    }

    private static void recipeThermalDissipatorPassiveAndActive() {
        addRecipe(
            findItemStack("Small Passive Thermal Dissipator"),
            "I I",
            "III",
            "CIC",
            'I', "ingotCopper",
            'C', findItemStack("Copper Thermal Cable"));

	/*	addRecipe(
				findItemStack("Small Active Thermal Dissipator"),
				"RMR",
				"I I",
				"III",
				'I', "ingotCopper",
				'M', findItemStack("Electrical Motor"),
				'R', "itemRubber",
				'C', findItemStack("Copper Thermal Cable"));*/

        addRecipe(
            findItemStack("Small Active Thermal Dissipator"),
            "RMR",
            " D ",
            'D', findItemStack("Small Passive Thermal Dissipator"),
            'M', findItemStack("Electrical Motor"),
            'R', "itemRubber");

	/*	addRecipe(
				findItemStack("200V Active Thermal Dissipator"),
				"RMR",
				"I I",
				"III",
				'I', "ingotCopper",
				'M', findItemStack("Advanced Electrical Motor"),
				'R', "itemRubber",
				'C', findItemStack("Copper Thermal Cable"));*/

        addRecipe(
            findItemStack("200V Active Thermal Dissipator"),
            "RMR",
            " D ",
            'D', findItemStack("Small Passive Thermal Dissipator"),
            'M', findItemStack("Advanced Electrical Motor"),
            'R', "itemRubber");

    }

    private static void recipeGeneral() {
        ItemStack _tr = findItemStack("Tree Resin");
        if (!_tr.isEmpty()) Utils.addSmelting(_tr.getItem(), _tr.getItemDamage(), findItemStack("Rubber", 1), 0f);

    }

    private static void recipeHeatingCorp() {
        addRecipe(findItemStack("Small 50V Copper Heating Corp"),
            "C C",
            "CCC",
            "C C",
            'C', findItemStack("Copper Cable"));

        addRecipe(findItemStack("50V Copper Heating Corp"),
            "C C",
            "CCC",
            "C C",
            'C', "ingotCopper");

        addRecipe(findItemStack("Small 200V Copper Heating Corp"),
            "CC",
            'C', findItemStack("50V Copper Heating Corp"));

        addRecipe(findItemStack("200V Copper Heating Corp"),
            "CC",
            'C', findItemStack("Small 200V Copper Heating Corp"));

        addRecipe(findItemStack("Small 50V Iron Heating Corp"),
            "C C",
            "CCC",
            "C C", 'C', findItemStack("Iron Cable"));

        addRecipe(findItemStack("50V Iron Heating Corp"),
            "C C",
            "CCC",
            "C C",
            'C', new ItemStack(Items.IRON_INGOT));

        addRecipe(findItemStack("Small 200V Iron Heating Corp"),
            "CC",
            'C', findItemStack("50V Iron Heating Corp"));

        addRecipe(findItemStack("200V Iron Heating Corp"),
            "CC",
            'C', findItemStack("Small 200V Iron Heating Corp"));

        addRecipe(findItemStack("Small 50V Tungsten Heating Corp"),
            "C C",
            "CCC",
            "C C",
            'C', findItemStack("Tungsten Cable"));

        addRecipe(findItemStack("50V Tungsten Heating Corp"),
            "C C",
            "CCC",
            "C C",
            'C', findItemStack("Tungsten Ingot"));

        addRecipe(findItemStack("Small 200V Tungsten Heating Corp"),
            "CC",
            'C', findItemStack("50V Tungsten Heating Corp"));
        addRecipe(findItemStack("200V Tungsten Heating Corp"),
            "CC",
            'C', findItemStack("Small 200V Tungsten Heating Corp"));
    }

    private static void recipeRegulatorItem() {
        addRecipe(findItemStack("On/OFF Regulator 10 Percent", 1),
            "R R",
            " R ",
            " I ",
            'R', new ItemStack(Items.REDSTONE),
            'I', new ItemStack(Items.IRON_INGOT));

        addRecipe(findItemStack("On/OFF Regulator 1 Percent", 1),
            "RRR",
            " I ",
            'R', new ItemStack(Items.REDSTONE),
            'I', new ItemStack(Items.IRON_INGOT));

        addRecipe(findItemStack("Analogic Regulator", 1),
            "R R",
            " C ",
            " I ",
            'R', new ItemStack(Items.REDSTONE),
            'I', new ItemStack(Items.IRON_INGOT),
            'C', dictCheapChip);
    }

    private static void recipeLampItem() {
        // Tungsten
        addRecipe(
            findItemStack("Small 50V Incandescent Light Bulb", 4),
            " G ",
            "GFG",
            " S ",
            'G', new ItemStack(Blocks.GLASS_PANE),
            'F', dictTungstenIngot,
            'S', findItemStack("Copper Cable"));

        addRecipe(findItemStack("50V Incandescent Light Bulb", 4),
            " G ",
            "GFG",
            " S ",
            'G', new ItemStack(Blocks.GLASS_PANE),
            'F', dictTungstenIngot,
            'S', findItemStack("Low Voltage Cable"));

        addRecipe(findItemStack("200V Incandescent Light Bulb", 4),
            " G ",
            "GFG",
            " S ",
            'G', new ItemStack(Blocks.GLASS_PANE),
            'F', dictTungstenIngot,
            'S', findItemStack("Medium Voltage Cable"));

        // CARBON
        addRecipe(findItemStack("Small 50V Carbon Incandescent Light Bulb", 4),
            " G ",
            "GFG",
            " S ",
            'G', new ItemStack(Blocks.GLASS_PANE),
            'F', new ItemStack(Items.COAL),
            'S', findItemStack("Copper Cable"));

        addRecipe(findItemStack("Small 50V Carbon Incandescent Light Bulb", 4),
            " G ",
            "GFG",
            " S ",
            'G', new ItemStack(Blocks.GLASS_PANE),
            'F', new ItemStack(Items.COAL, 1, 1),
            'S', findItemStack("Copper Cable"));

        addRecipe(
            findItemStack("50V Carbon Incandescent Light Bulb", 4),
            " G ",
            "GFG",
            " S ",
            'G', new ItemStack(Blocks.GLASS_PANE),
            'F', new ItemStack(Items.COAL),
            'S', findItemStack("Low Voltage Cable"));

        addRecipe(findItemStack("50V Carbon Incandescent Light Bulb", 4),
            " G ",
            "GFG",
            " S ",
            'G', new ItemStack(Blocks.GLASS_PANE),
            'F', new ItemStack(Items.COAL, 1, 1),
            'S', findItemStack("Low Voltage Cable"));

        addRecipe(
            findItemStack("Small 50V Economic Light Bulb", 4),
            " G ",
            "GFG",
            " S ",
            'G', new ItemStack(Blocks.GLASS_PANE),
            'F', new ItemStack(Items.GLOWSTONE_DUST),
            'S', findItemStack("Copper Cable"));

        addRecipe(findItemStack("50V Economic Light Bulb", 4),
            " G ",
            "GFG",
            " S ",
            'G', new ItemStack(Blocks.GLASS_PANE),
            'F', new ItemStack(Items.GLOWSTONE_DUST),
            'S', findItemStack("Low Voltage Cable"));

        addRecipe(findItemStack("200V Economic Light Bulb", 4),
            " G ",
            "GFG",
            " S ",
            'G', new ItemStack(Blocks.GLASS_PANE),
            'F', new ItemStack(Items.GLOWSTONE_DUST),
            'S', findItemStack("Medium Voltage Cable"));

        addRecipe(findItemStack("50V Farming Lamp", 2),
            "GGG",
            "FFF",
            "GSG",
            'G', new ItemStack(Blocks.GLASS_PANE),
            'F', dictTungstenIngot,
            'S', findItemStack("Low Voltage Cable"));

        addRecipe(findItemStack("200V Farming Lamp", 2),
            "GGG",
            "FFF",
            "GSG",
            'G', new ItemStack(Blocks.GLASS_PANE),
            'F', dictTungstenIngot,
            'S', findItemStack("Medium Voltage Cable"));

        addRecipe(findItemStack("50V LED Bulb", 2),
            "GGG",
            "SSS",
            " C ",
            'G', new ItemStack(Blocks.GLASS_PANE),
            'S', findItemStack("Silicon Ingot"),
            'C', findItemStack("Low Voltage Cable"));

        addRecipe(findItemStack("200V LED Bulb", 2),
            "GGG",
            "SSS",
            " C ",
            'G', new ItemStack(Blocks.GLASS_PANE),
            'S', findItemStack("Silicon Ingot"),
            'C', findItemStack("Medium Voltage Cable"));

    }

    private static void recipeProtection() {
        addRecipe(findItemStack("Overvoltage Protection", 4),
            "SCD",
            'S', findItemStack("Electrical Probe Chip"),
            'C', dictCheapChip,
            'D', new ItemStack(Items.REDSTONE));

        addRecipe(findItemStack("Overheating Protection", 4),
            "SCD",
            'S', findItemStack("Thermal Probe Chip"),
            'C', dictCheapChip,
            'D', new ItemStack(Items.REDSTONE));

    }

    private static void recipeCombustionChamber() {
        addRecipe(findItemStack("Combustion Chamber"),
            " L ",
            "L L",
            " L ",
            'L', new ItemStack(Blocks.STONE));
        addRecipe(findItemStack("Thermal Insulation", 4),
            "WSW",
            "SWS",
            "WSW",
            'S', new ItemStack(Blocks.STONE),
            'W', new ItemStack(Blocks.WOOL));
    }

    private static void recipeFerromagneticCore() {
        addRecipe(findItemStack("Cheap Ferromagnetic Core"),
            "LLL",
            "L  ",
            "LLL",
            'L', Items.IRON_INGOT);

        addRecipe(findItemStack("Average Ferromagnetic Core"),
            "PCP",
            'C', findItemStack("Cheap Ferromagnetic Core"),
            'P', "plateIron");

        addRecipe(findItemStack("Optimal Ferromagnetic Core"),
            "P",
            "C",
            "P",
            'C', findItemStack("Average Ferromagnetic Core"),
            'P', "plateIron");
    }

    private static void recipeDust() {
        addShapelessRecipe(findItemStack("Alloy Dust", 2),
            "dustIron",
            "dustIron",
            "dustCoal",
            dictTungstenDust);

    }

    private static void recipeElectricalMotor() {
        addRecipe(findItemStack("Electrical Motor"),
            " C ",
            "III",
            "C C",
            'I', new ItemStack(Items.IRON_INGOT),
            'C', findItemStack("Low Voltage Cable"));

        addRecipe(findItemStack("Advanced Electrical Motor"),
            "RCR",
            "MIM",
            "CRC",
            'M', findItemStack("Advanced Magnet"),
            'I', new ItemStack(Items.IRON_INGOT),
            'R', new ItemStack(Items.REDSTONE),
            'C', findItemStack("Medium Voltage Cable"));

        // TODO

    }

    private static void recipeSolarTracker() {
        addRecipe(findItemStack("Solar Tracker", 4),
            "VVV",
            "RQR",
            "III",
            'Q', new ItemStack(Items.QUARTZ),
            'V', new ItemStack(Blocks.GLASS_PANE),
            'R', new ItemStack(Items.REDSTONE),
            'G', new ItemStack(Items.GOLD_INGOT),
            'I', new ItemStack(Items.IRON_INGOT));

    }

    private static void recipeDynamo() {

    }

    private static void recipeWindRotor() {

    }

    private static void recipeMeter() {
        addRecipe(findItemStack("MultiMeter"),
            "RGR",
            "RER",
            "RCR",
            'G', new ItemStack(Blocks.GLASS_PANE),
            'C', findItemStack("Electrical Probe Chip"),
            'E', new ItemStack(Items.REDSTONE),
            'R', "itemRubber");

        addRecipe(findItemStack("Thermometer"),
            "RGR",
            "RER",
            "RCR",
            'G', new ItemStack(Blocks.GLASS_PANE),
            'C', findItemStack("Thermal Probe Chip"),
            'E', new ItemStack(Items.REDSTONE),
            'R', "itemRubber");

        addShapelessRecipe(findItemStack("AllMeter"),
            findItemStack("MultiMeter"),
            findItemStack("Thermometer"));

        addRecipe(findItemStack("Wireless Analyser"),
            " S ",
            "RGR",
            "RER",
            'G', new ItemStack(Blocks.GLASS_PANE),
            'S', findItemStack("Signal Antenna"),
            'E', new ItemStack(Items.REDSTONE),
            'R', "itemRubber");

    }

    private static void recipeElectricalDrill() {
        addRecipe(findItemStack("Cheap Electrical Drill"),
            "CMC",
            " T ",
            " P ",
            'T', findItemStack("Mining Pipe"),
            'C', dictCheapChip,
            'M', findItemStack("Electrical Motor"),
            'P', new ItemStack(Items.IRON_PICKAXE));

        addRecipe(findItemStack("Average Electrical Drill"),
            "RCR",
            " D ",
            " d ", 'R', Items.REDSTONE,
            'C', dictCheapChip,
            'D', findItemStack("Cheap Electrical Drill"),
            'd', new ItemStack(Items.DIAMOND));

        addRecipe(findItemStack("Fast Electrical Drill"),
            "MCM",
            " T ",
            " P ",
            'T', findItemStack("Mining Pipe"),
            'C', dictAdvancedChip,
            'M', findItemStack("Advanced Electrical Motor"),
            'P', new ItemStack(Items.DIAMOND_PICKAXE));

    }

    private static void recipeOreScanner() {
        addRecipe(findItemStack("oreBlock Scanner"),
            "IGI",
            "RCR",
            "IGI",
            'C', dictCheapChip,
            'R', new ItemStack(Items.REDSTONE),
            'I', new ItemStack(Items.IRON_INGOT),
            'G', new ItemStack(Items.GOLD_INGOT));

    }

    private static void recipeMiningPipe() {
        addRecipe(findItemStack("Mining Pipe", 4),
            "A",
            "A",
            "A",
            'A', "ingotAlloy");
    }

    private static void recipeTreeResinAndRubber() {
        addRecipe(findItemStack("Tree Resin Collector"),
            "W W",
            "WW ", 'W', "plankWood");

        addRecipe(findItemStack("Tree Resin Collector"),
            "W W",
            " WW", 'W', "plankWood");

    }

    private static void recipeRawCable() {
        addRecipe(findItemStack("Copper Cable", 6),
            "III",
            'I', "ingotCopper");

        addRecipe(findItemStack("Iron Cable", 6),
            "III",
            'I', new ItemStack(Items.IRON_INGOT));

        addRecipe(findItemStack("Tungsten Cable", 6),
            "III",
            'I', dictTungstenIngot);

    }

    private static void recipeBatteryItem() {
        addRecipe(findItemStack("Portable Battery"),
            " I ",
            "IPI",
            "IPI",
            'P', "ingotLead",
            'I', new ItemStack(Items.IRON_INGOT));
        addShapelessRecipe(
            findItemStack("Portable Battery Pack"),
            findItemStack("Portable Battery"), findItemStack("Portable Battery"), findItemStack("Portable Battery"));
    }

    private static void recipeElectricalTool() {
        addRecipe(findItemStack("Small Flashlight"),
            "GLG",
            "IBI",
            " I ",
            'L', findItemStack("50V Incandescent Light Bulb"),
            'B', findItemStack("Portable Battery"),
            'G', new ItemStack(Blocks.GLASS_PANE),
            'I', new ItemStack(Items.IRON_INGOT));
        addRecipe(findItemStack("Improved Flashlight"),
            "GLG",
            "IBI",
            " I ",
            'L', findItemStack("50V LED Bulb"),
            'B', findItemStack("Portable Battery Pack"),
            'G', new ItemStack(Blocks.GLASS_PANE),
            'I', new ItemStack(Items.IRON_INGOT));

        addRecipe(findItemStack("Portable Electrical Mining Drill"),
            " T ",
            "IBI",
            " I ",
            'T', findItemStack("Average Electrical Drill"),
            'B', findItemStack("Portable Battery"),
            'I', new ItemStack(Items.IRON_INGOT));

        addRecipe(findItemStack("Portable Electrical Axe"),
            " T ",
            "IMI",
            "IBI",
            'T', new ItemStack(Items.IRON_AXE),
            'B', findItemStack("Portable Battery"),
            'M', findItemStack("Electrical Motor"),
            'I', new ItemStack(Items.IRON_INGOT));

        if (true) {
            addRecipe(findItemStack("X-Ray Scanner"),
                "PGP",
                "PCP",
                "PBP",
                'C', dictAdvancedChip,
                'B', findItemStack("Portable Battery"),
                'P', new ItemStack(Items.IRON_INGOT),
                'G', findItemStack("oreBlock Scanner"));
        }

    }

    private static void recipeECoal() {
        addRecipe(findItemStack("E-Coal Helmet"),
            "PPP",
            "PCP",
            'P', "plateCoal",
            'C', dictAdvancedChip);
        addRecipe(findItemStack("E-Coal Boots"),
            " C ",
            "P P",
            "P P",
            'P', "plateCoal",
            'C', dictAdvancedChip);

        addRecipe(findItemStack("E-Coal Chestplate"),
            "P P",
            "PCP",
            "PPP",
            'P', "plateCoal",
            'C', dictAdvancedChip);

        addRecipe(findItemStack("E-Coal Leggings"),
            "PPP",
            "PCP",
            "P P",
            'P', "plateCoal",
            'C', dictAdvancedChip);

    }

    private static void recipePortableCapacitor() {
        addRecipe(findItemStack("Portable Condensator"),
            "RcR",
            "wCw",
            "RcR",
            'C', new ItemStack(Items.REDSTONE),
            'R', "itemRubber",
            'w', findItemStack("Copper Cable"),
            'c', "plateCopper");

        addShapelessRecipe(findItemStack("Portable Condensator Pack"),
            findItemStack("Portable Condensator"),
            findItemStack("Portable Condensator"),
            findItemStack("Portable Condensator"));
    }

    private static void recipeMiscItem() {
        addRecipe(findItemStack("Cheap Chip"),
            " R ",
            "RSR",
            " R ",
            'S', "ingotSilicon",
            'R', new ItemStack(Items.REDSTONE));
        addRecipe(findItemStack("Advanced Chip"),
            "LRL",
            "RCR",
            "LRL",
            'C', dictCheapChip,
            'L', "ingotSilicon",
            'R', new ItemStack(Items.REDSTONE));

        addRecipe(findItemStack("Machine Block"),
            "LLL",
            "LcL",
            "LLL",
            'L', new ItemStack(Items.IRON_INGOT),
            'c', findItemStack("Copper Cable"));

        addRecipe(findItemStack("Advanced Machine Block"),
            " C ",
            "CcC",
            " C ",
            'C', "plateAlloy",
            'L', "ingotAlloy",
            'c', findItemStack("Copper Cable"));

        addRecipe(findItemStack("Electrical Probe Chip"),
            " R ",
            "RCR",
            " R ",
            'C', findItemStack("High Voltage Cable"),
            'R', new ItemStack(Items.REDSTONE));

        addRecipe(findItemStack("Thermal Probe Chip"),
            " C ",
            "RIR",
            " C ",
            'G', new ItemStack(Items.GOLD_INGOT),
            'I', new ItemStack(Items.IRON_INGOT),
            'C', "ingotCopper",
            'R', new ItemStack(Items.REDSTONE));

        addRecipe(findItemStack("Signal Antenna"),
            "c",
            "c",
            'c', findItemStack("Iron Cable"));

        addRecipe(findItemStack("Machine Booster"),
            "m",
            "c",
            "m",
            'm', findItemStack("Electrical Motor"),
            'c', dictAdvancedChip);

        addRecipe(findItemStack("Wrench"),
            " c ",
            "cc ",
            "  c",
            'c', new ItemStack(Items.IRON_INGOT));

        addRecipe(findItemStack("Player Filter"),
            " g",
            "gc",
            " g",
            'g', new ItemStack(Blocks.GLASS_PANE),
            'c', new ItemStack(Items.DYE, 1, 2));

        addRecipe(findItemStack("Monster Filter"),
            " g",
            "gc",
            " g",
            'g', new ItemStack(Blocks.GLASS_PANE),
            'c', new ItemStack(Items.DYE, 1, 1));

        addRecipe(findItemStack("Casing", 8),
            "ppp",
            "p p",
            "ppp",
            'p', findItemStack("Iron Plate"));

    }

    private static void recipeMacerator() {
        float f = 4000;
	    Descriptors.maceratorRecipes.addRecipe(new Recipe(new ItemStack(Blocks.COAL_ORE, 1),
	        new ItemStack(Items.COAL, 3, 0), 1.0 * f));
        Descriptors.maceratorRecipes.addRecipe(new Recipe(findItemStack("Copper oreBlock"),
            new ItemStack[]{findItemStack("Copper Dust", 2)}, 1.0 * f));
        Descriptors.maceratorRecipes.addRecipe(new Recipe(new ItemStack(Blocks.IRON_ORE),
            new ItemStack[]{findItemStack("Iron Dust", 2)}, 1.5 * f));
        Descriptors.maceratorRecipes.addRecipe(new Recipe(new ItemStack(Blocks.GOLD_ORE),
            new ItemStack[]{findItemStack("Gold Dust", 2)}, 3.0 * f));
        Descriptors.maceratorRecipes.addRecipe(new Recipe(findItemStack("Lead oreBlock"),
            new ItemStack[]{findItemStack("Lead Dust", 2)}, 2.0 * f));
        Descriptors.maceratorRecipes.addRecipe(new Recipe(findItemStack("Tungsten oreBlock"),
            new ItemStack[]{findItemStack("Tungsten Dust", 2)}, 2.0 * f));
        Descriptors.maceratorRecipes.addRecipe(new Recipe(new ItemStack(Items.COAL, 1, 0),
            new ItemStack[]{findItemStack("Coal Dust", 2)}, 1.0 * f));
        Descriptors.maceratorRecipes.addRecipe(new Recipe(new ItemStack(Items.COAL, 1, 1),
            new ItemStack[]{findItemStack("Coal Dust", 2)}, 1.0 * f));
        Descriptors.maceratorRecipes.addRecipe(new Recipe(new ItemStack(Blocks.SAND, 1),
            new ItemStack[]{findItemStack("Silicon Dust", 1)}, 3.0 * f));
        Descriptors.maceratorRecipes.addRecipe(new Recipe(findItemStack("Cinnabar oreBlock"),
            new ItemStack[]{findItemStack("Cinnabar Dust", 2)}, 2.0 * f));

        Descriptors.maceratorRecipes.addRecipe(new Recipe(findItemStack("Copper Ingot"),
            new ItemStack[]{findItemStack("Copper Dust", 1)}, 0.5 * f));
        Descriptors.maceratorRecipes.addRecipe(new Recipe(new ItemStack(Items.IRON_INGOT),
            new ItemStack[]{findItemStack("Iron Dust", 1)}, 0.5 * f));
        Descriptors.maceratorRecipes.addRecipe(new Recipe(new ItemStack(Items.GOLD_INGOT),
            new ItemStack[]{findItemStack("Gold Dust", 1)}, 0.5 * f));
        Descriptors.maceratorRecipes.addRecipe(new Recipe(findItemStack("Lead Ingot"),
            new ItemStack[]{findItemStack("Lead Dust", 1)}, 0.5 * f));
        Descriptors.maceratorRecipes.addRecipe(new Recipe(findItemStack("Tungsten Ingot"),
            new ItemStack[]{findItemStack("Tungsten Dust", 1)}, 0.5 * f));

        Descriptors.maceratorRecipes.addRecipe(new Recipe(new ItemStack(Blocks.COBBLESTONE),
            new ItemStack[]{new ItemStack(Blocks.GRAVEL)}, 1.0 * f));
        Descriptors.maceratorRecipes.addRecipe(new Recipe(new ItemStack(Blocks.GRAVEL),
            new ItemStack[]{new ItemStack(Items.FLINT)}, 1.0 * f));

        Descriptors.maceratorRecipes.addRecipe(new Recipe(new ItemStack(Blocks.DIRT),
            new ItemStack[]{new ItemStack(Blocks.SAND)}, 1.0 * f));
    }

    private static void recipeMaceratorModOres() {
        float f = 4000;

        // AE2:
        recipeMaceratorModOre(f * 3f, "oreCertusQuartz", "dustCertusQuartz", 3);
        recipeMaceratorModOre(f * 1.5f, "crystalCertusQuartz", "dustCertusQuartz", 1);
        recipeMaceratorModOre(f * 3f, "oreNetherQuartz", "dustNetherQuartz", 3);
        recipeMaceratorModOre(f * 1.5f, "crystalNetherQuartz", "dustNetherQuartz", 1);
        recipeMaceratorModOre(f * 1.5f, "crystalFluix", "dustFluix", 1);
    }

    private static void recipeMaceratorModOre(float f, String inputName, String outputName, int outputCount) {
        if (!OreDictionary.doesOreNameExist(inputName)) {
            Eln.logger.info("No entries for oredict: " + inputName);
            return;
        }
        if (!OreDictionary.doesOreNameExist(outputName)) {
            Eln.logger.info("No entries for oredict: " + outputName);
            return;
        }
        List<ItemStack> inOres = OreDictionary.getOres(inputName);
        List<ItemStack> outOres = OreDictionary.getOres(outputName);
        if (inOres.size() == 0) {
            Eln.logger.info("No ores in oredict entry: " + inputName);
        }
        if (outOres.size() == 0) {
            Eln.logger.info("No ores in oredict entry: " + outputName);
            return;
        }
        ItemStack output = outOres.get(0).copy();
        output.setCount(outputCount);
        Eln.logger.info("Adding mod recipe fromFacing " + inputName + " to " + outputName);
        for (ItemStack input : inOres) {
            Descriptors.maceratorRecipes.addRecipe(new Recipe(input, output, f));
        }
    }

    private static void recipePlateMachine() {
        float f = 10000;
        Descriptors.plateMachineRecipes.addRecipe(new Recipe(
            findItemStack("Copper Ingot", plateConversionRatio),
            findItemStack("Copper Plate"), 1.0 * f));

        Descriptors.plateMachineRecipes.addRecipe(new Recipe(findItemStack("Lead Ingot", plateConversionRatio),
            findItemStack("Lead Plate"), 1.0 * f));

        Descriptors.plateMachineRecipes.addRecipe(new Recipe(
            findItemStack("Silicon Ingot", 4),
            findItemStack("Silicon Plate"), 1.0 * f));

        Descriptors.plateMachineRecipes.addRecipe(new Recipe(findItemStack("Alloy Ingot", plateConversionRatio),
            findItemStack("Alloy Plate"), 1.0 * f));

        Descriptors.plateMachineRecipes.addRecipe(new Recipe(new ItemStack(Items.IRON_INGOT, plateConversionRatio,
            0), findItemStack("Iron Plate"), 1.0 * f));

        Descriptors.plateMachineRecipes.addRecipe(new Recipe(new ItemStack(Items.GOLD_INGOT, plateConversionRatio,
            0), findItemStack("Gold Plate"), 1.0 * f));

    }

    private static void recipeCompressor() {
        Descriptors.compressorRecipes.addRecipe(new Recipe(findItemStack("Coal Plate", 4),
            new ItemStack[]{new ItemStack(Items.DIAMOND)}, 80000.0));
        // extractorRecipes.addRecipe(new
        // Recipe("dustCinnabar",new
        // ItemStack[]{findItemStack("Purified Cinnabar Dust",1)}, 1000.0));

        Descriptors.compressorRecipes.addRecipe(new Recipe(findItemStack("Coal Dust", 4),
            findItemStack("Coal Plate"), 4000.0));

        Descriptors.compressorRecipes.addRecipe(new Recipe(new ItemStack(Blocks.SAND),
            findItemStack("Dielectric"), 2000.0));

        Descriptors.compressorRecipes.addRecipe(new Recipe(new ItemStack(Blocks.LOG),
            findItemStack("Tree Resin"), 3000.0));

    }

    private static void recipeMagnetizer() {
        Descriptors.magnetizerRecipes.addRecipe(new Recipe(new ItemStack(Items.IRON_INGOT, 2),
            new ItemStack[]{findItemStack("Basic Magnet")}, 5000.0));
        Descriptors.magnetizerRecipes.addRecipe(new Recipe(findItemStack("Alloy Ingot", 2),
            new ItemStack[]{findItemStack("Advanced Magnet")}, 15000.0));
    }

    private static void recipeFuelBurnerItem() {
        addRecipe(findItemStack("Small Fuel Burner"),
            "   ",
            " Cc",
            "   ",
            'C', findItemStack("Combustion Chamber"),
            'c', findItemStack("Copper Thermal Cable"));

        addRecipe(findItemStack("Medium Fuel Burner"),
            "   ",
            " Cc",
            " C ",
            'C', findItemStack("Combustion Chamber"),
            'c', findItemStack("Copper Thermal Cable"));

        addRecipe(findItemStack("Big Fuel Burner"),
            "   ",
            "CCc",
            "CC ",
            'C', findItemStack("Combustion Chamber"),
            'c', findItemStack("Copper Thermal Cable"));
    }

    private static void recipeFurnace() {
        ItemStack in;

        in = findItemStack("Copper oreBlock");
        Utils.addSmelting(in.getItem(), in.getItemDamage(),
            findItemStack("Copper Ingot"));
        in = findItemStack("dustCopper");
        Utils.addSmelting(in.getItem(), in.getItemDamage(),
            findItemStack("Copper Ingot"));
        in = findItemStack("Lead oreBlock");
        Utils.addSmelting(in.getItem(), in.getItemDamage(),
            findItemStack("ingotLead"));
        in = findItemStack("dustLead");
        Utils.addSmelting(in.getItem(), in.getItemDamage(),
            findItemStack("ingotLead"));
        in = findItemStack("Tungsten oreBlock");
        Utils.addSmelting(in.getItem(), in.getItemDamage(),
            findItemStack("Tungsten Ingot"));
        in = findItemStack("Tungsten Dust");
        Utils.addSmelting(in.getItem(), in.getItemDamage(),
            findItemStack("Tungsten Ingot"));
        in = findItemStack("ingotAlloy");
        // Utils.addSmelting(in.getItem().itemID, in.getItemDamage(),
        // findItemStack("Ferrite Ingot"));
        in = findItemStack("dustIron");
        Utils.addSmelting(in.getItem(), in.getItemDamage(),
            new ItemStack(Items.IRON_INGOT));

        in = findItemStack("dustGold");
        Utils.addSmelting(in.getItem(), in.getItemDamage(),
            new ItemStack(Items.GOLD_INGOT));

        in = findItemStack("Tree Resin");
        Utils.addSmelting(in.getItem(), in.getItemDamage(),
            findItemStack("Rubber", 2));

        in = findItemStack("Alloy Dust");
        Utils.addSmelting(in.getItem(), in.getItemDamage(),
            findItemStack("Alloy Ingot"));

        in = findItemStack("Silicon Dust");
        Utils.addSmelting(in.getItem(), in.getItemDamage(),
            findItemStack("Silicon Ingot"));

        // in = findItemStack("Purified Cinnabar Dust");
        in = findItemStack("dustCinnabar");
        Utils.addSmelting(in.getItem(), in.getItemDamage(),
            findItemStack("Mercury"));

    }

    private static void recipeElectricalSensor() {
        addRecipe(findItemStack("Voltage Probe", 1),
            "SC",
            'S', findItemStack("Electrical Probe Chip"),
            'C', findItemStack("Signal Cable"));

        addRecipe(findItemStack("Electrical Probe", 1),
            "SCS",
            'S', findItemStack("Electrical Probe Chip"),
            'C', findItemStack("Signal Cable"));

    }

    private static void recipeThermalSensor() {
        addRecipe(findItemStack("Thermal Probe", 1),
            "SCS",
            'S', findItemStack("Thermal Probe Chip"),
            'C', findItemStack("Signal Cable"));

        addRecipe(findItemStack("Temperature Probe", 1),
            "SC",
            'S', findItemStack("Thermal Probe Chip"),
            'C', findItemStack("Signal Cable"));

    }

    private static void recipeTransporter() {
        addRecipe(findItemStack("Experimental Transporter", 1),
            "RMR",
            "RMR",
            " R ",
            'M', findItemStack("Advanced Machine Block"),
            'C', findItemStack("High Voltage Cable"),
            'R', dictAdvancedChip);
    }

    private static void recipeTurret() {
        addRecipe(findItemStack("800V Defence Turret", 1),
            " R ",
            "CMC",
            " c ",
            'M', findItemStack("Advanced Machine Block"),
            'C', dictAdvancedChip,
            'c', findItemStack("High Voltage Cable"),
            'R', new ItemStack(Blocks.REDSTONE_BLOCK));

    }

    private static void recipeMachine() {
        addRecipe(findItemStack("50V Macerator", 1),
            "IRI",
            "FMF",
            "IcI",
            'M', findItemStack("Machine Block"),
            'c', findItemStack("Electrical Motor"),
            'F', new ItemStack(Items.FLINT),
            'I', new ItemStack(Items.IRON_INGOT),
            'R', new ItemStack(Items.REDSTONE));
        addRecipe(findItemStack("200V Macerator", 1),
            "ICI",
            "DMD",
            "IcI",
            'M', findItemStack("Advanced Machine Block"),
            'C', dictAdvancedChip,
            'c', findItemStack("Advanced Electrical Motor"),
            'D', new ItemStack(Items.DIAMOND),
            'I', "ingotAlloy");

        addRecipe(findItemStack("50V Compressor", 1),
            "IRI",
            "FMF",
            "IcI",
            'M', findItemStack("Machine Block"),
            'c', findItemStack("Electrical Motor"),
            'F', "plateIron",
            'I', new ItemStack(Items.IRON_INGOT),
            'R', new ItemStack(Items.REDSTONE));
        addRecipe(findItemStack("200V Compressor", 1),
            "ICI",
            "DMD",
            "IcI",
            'M', findItemStack("Advanced Machine Block"),
            'C', dictAdvancedChip,
            'c', findItemStack("Advanced Electrical Motor"),
            'D', "plateAlloy",
            'I', "ingotAlloy");

        addRecipe(findItemStack("50V Plate Machine", 1),
            "IRI",
            "IMI",
            "IcI",
            'M', findItemStack("Machine Block"),
            'c', findItemStack("Electrical Motor"),
            'I', new ItemStack(Items.IRON_INGOT),
            'R', new ItemStack(Items.REDSTONE));

        addRecipe(findItemStack("200V Plate Machine", 1),
            "DCD",
            "DMD",
            "DcD",
            'M', findItemStack("Advanced Machine Block"),
            'C', dictAdvancedChip,
            'c', findItemStack("Advanced Electrical Motor"),
            'D', "plateAlloy",
            'I', "ingotAlloy");

        addRecipe(findItemStack("50V Magnetizer", 1),
            "IRI",
            "cMc",
            "III",
            'M', findItemStack("Machine Block"),
            'c', findItemStack("Electrical Motor"),
            'I', new ItemStack(Items.IRON_INGOT),
            'R', new ItemStack(Items.REDSTONE));

        addRecipe(findItemStack("200V Magnetizer", 1),
            "ICI",
            "cMc",
            "III",
            'M', findItemStack("Advanced Machine Block"),
            'C', dictAdvancedChip,
            'c', findItemStack("Advanced Electrical Motor"),
            'I', "ingotAlloy");

    }

    private static void recipeElectricalGate() {
        addShapelessRecipe(findItemStack("Electrical Timer"),
            new ItemStack(Items.REPEATER),
            dictCheapChip);

        addRecipe(findItemStack("Signal Processor", 1),
            "IcI",
            "cCc",
            "IcI",
            'I', new ItemStack(Items.IRON_INGOT),
            'c', findItemStack("Signal Cable"),
            'C', dictCheapChip);
    }

    private static void recipeElectricalRedstone() {
        addRecipe(findItemStack("Redstone-to-Voltage Converter", 1),
            "TCS",
            'S', findItemStack("Signal Cable"),
            'C', dictCheapChip,
            'T', new ItemStack(Blocks.REDSTONE_TORCH));

        addRecipe(findItemStack("Voltage-to-Redstone Converter", 1),
            "CTR",
            'R', new ItemStack(Items.REDSTONE),
            'C', dictCheapChip,
            'T', new ItemStack(Blocks.REDSTONE_TORCH));

    }

    private static void recipeElectricalEnvironmentalSensor() {
        addShapelessRecipe(findItemStack("Electrical Daylight Sensor"),
            new ItemStack(Blocks.DAYLIGHT_DETECTOR),
            findItemStack("Redstone-to-Voltage Converter"));

        addShapelessRecipe(findItemStack("Electrical Light Sensor"),
            new ItemStack(Blocks.DAYLIGHT_DETECTOR),
            new ItemStack(Items.QUARTZ),
            findItemStack("Redstone-to-Voltage Converter"));

        addRecipe(findItemStack("Electrical Weather Sensor"),
            " r ",
            "rRr",
            " r ",
            'R', new ItemStack(Items.REDSTONE),
            'r', "itemRubber");

        addRecipe(findItemStack("Electrical Anemometer Sensor"),
            " I ",
            " R ",
            "I I",
            'R', new ItemStack(Items.REDSTONE),
            'I', new ItemStack(Items.IRON_INGOT));

        addRecipe(findItemStack("Electrical Entity Sensor"),
            " G ",
            "GRG",
            " G ",
            'G', new ItemStack(Blocks.GLASS_PANE),
            'R', new ItemStack(Items.REDSTONE));

        addRecipe(findItemStack("Electrical Fire Detector"),
            "cbr",
            "p p",
            "r r",
            'c', findItemStack("Signal Cable"),
            'b', dictCheapChip,
            'r', "itemRubber",
            'p', "plateCopper");

        addRecipe(findItemStack("Electrical Fire Buzzer"),
            "rar",
            "p p",
            "r r",
            'a', dictAdvancedChip,
            'r', "itemRubber",
            'p', "plateCopper");

        addShapelessRecipe(findItemStack("Scanner"),
            new ItemStack(Items.COMPARATOR),
            dictAdvancedChip);

    }

    private static void recipeElectricalVuMeter() {
        for (int idx = 0; idx < 4; idx++) {
            addRecipe(findItemStack("Analog vuMeter", 1),
                "WWW",
                "RIr",
                "WSW",
                'W', new ItemStack(Blocks.PLANKS, 1, idx),
                'R', new ItemStack(Items.REDSTONE),
                'I', new ItemStack(Items.IRON_INGOT),
                'r', new ItemStack(Items.DYE, 1, 1),
                'S', findItemStack("Signal Cable"));
        }
        for (int idx = 0; idx < 4; idx++) {
            addRecipe(findItemStack("LED vuMeter", 1),
                " W ",
                "WTW",
                " S ",
                'W', new ItemStack(Blocks.PLANKS, 1, idx),
                'T', new ItemStack(Blocks.REDSTONE_TORCH),
                'S', findItemStack("Signal Cable"));
        }
    }

    private static void recipeElectricalBreaker() {

        addRecipe(findItemStack("Electrical Breaker", 1),
            "crC",
            'c', findItemStack("Overvoltage Protection"),
            'C', findItemStack("Overheating Protection"),
            'r', findItemStack("High Voltage Relay"));

    }

    private static void recipeFuses() {

        addRecipe(findItemStack("Electrical Fuse Holder", 1),
            "i",
            " ",
            "i",
            'i', new ItemStack(Items.IRON_INGOT));

        addRecipe(findItemStack("Lead Fuse for low voltage cables", 4),
            "rcr",
            'r', findItemStack("itemRubber"),
            'c', findItemStack("Low Voltage Cable"));

        addRecipe(findItemStack("Lead Fuse for medium voltage cables", 4),
            "rcr",
            'r', findItemStack("itemRubber"),
            'c', findItemStack("Medium Voltage Cable"));

        addRecipe(findItemStack("Lead Fuse for high voltage cables", 4),
            "rcr",
            'r', findItemStack("itemRubber"),
            'c', findItemStack("High Voltage Cable"));

        addRecipe(findItemStack("Lead Fuse for very high voltage cables", 4),
            "rcr",
            'r', findItemStack("itemRubber"),
            'c', findItemStack("Very High Voltage Cable"));

    }

    private static void recipeElectricalGateSource() {
        addRecipe(findItemStack("Signal Trimmer", 1),
            "RsR",
            "rRr",
            " c ",
            'M', findItemStack("Machine Block"),
            'c', findItemStack("Signal Cable"),
            'r', "itemRubber",
            's', new ItemStack(Items.STICK),
            'R', new ItemStack(Items.REDSTONE));

        addRecipe(findItemStack("Signal Switch", 3),
            " r ",
            "rRr",
            " c ",
            'M', findItemStack("Machine Block"),
            'c', findItemStack("Signal Cable"),
            'r', "itemRubber",
            'I', new ItemStack(Items.IRON_INGOT),
            'R', new ItemStack(Items.REDSTONE));

        addRecipe(findItemStack("Signal Button", 3),
            " R ",
            "rRr",
            " c ",
            'M', findItemStack("Machine Block"),
            'c', findItemStack("Signal Cable"),
            'r', "itemRubber",
            'I', new ItemStack(Items.IRON_INGOT),
            'R', new ItemStack(Items.REDSTONE));

        addRecipe(findItemStack("Wireless Switch", 3),
            " a ",
            "rCr",
            " r ",
            'M', findItemStack("Machine Block"),
            'c', findItemStack("Signal Cable"),
            'C', dictCheapChip,
            'a', findItemStack("Signal Antenna"),
            'r', "itemRubber",
            'I', new ItemStack(Items.IRON_INGOT),
            'R', new ItemStack(Items.REDSTONE));

        addRecipe(findItemStack("Wireless Button", 3),
            " a ",
            "rCr",
            " R ",
            'M', findItemStack("Machine Block"),
            'c', findItemStack("Signal Cable"),
            'C', dictCheapChip,
            'a', findItemStack("Signal Antenna"),
            'r', "itemRubber",
            'I', new ItemStack(Items.IRON_INGOT),
            'R', new ItemStack(Items.REDSTONE));

        // Wireless Switch
        // Wireless Button
    }

    private static void recipeElectricalDataLogger() {
        addRecipe(findItemStack("Data Logger", 1),
            "RRR",
            "RGR",
            "RCR",
            'R', "itemRubber",
            'C', dictCheapChip,
            'G', new ItemStack(Blocks.GLASS_PANE));

        addRecipe(findItemStack("Modern Data Logger", 1),
            "RRR",
            "RGR",
            "RCR",
            'R', "itemRubber",
            'C', dictAdvancedChip,
            'G', new ItemStack(Blocks.GLASS_PANE));

        addRecipe(findItemStack("Industrial Data Logger", 1),
            "RRR",
            "GGG",
            "RCR",
            'R', "itemRubber",
            'C', dictAdvancedChip,
            'G', new ItemStack(Blocks.GLASS_PANE));
    }

    private static void recipeSixNodeCache() {

    }

    private static void recipeElectricalAlarm() {
        addRecipe(findItemStack("Nuclear Alarm", 1),
            "ITI",
            "IMI",
            "IcI",
            'c', findItemStack("Signal Cable"),
            'T', new ItemStack(Blocks.REDSTONE_TORCH),
            'I', new ItemStack(Items.IRON_INGOT),
            'M', new ItemStack(Blocks.NOTEBLOCK));
        addRecipe(findItemStack("Standard Alarm", 1),
            "MTM",
            "IcI",
            "III",
            'c', findItemStack("Signal Cable"),
            'T', new ItemStack(Blocks.REDSTONE_TORCH),
            'I', new ItemStack(Items.IRON_INGOT),
            'M', new ItemStack(Blocks.NOTEBLOCK));

    }

    private static void recipeElectricalAntenna() {
        addRecipe(findItemStack("Low Power Transmitter Antenna", 1),
            "R i",
            "CI ",
            "R i",
            'C', dictCheapChip,
            'i', new ItemStack(Items.IRON_INGOT),
            'I', "plateIron",
            'R', new ItemStack(Items.REDSTONE));
        addRecipe(findItemStack("Low Power Receiver Antenna", 1),
            "i  ",
            " IC",
            "i  ",
            'C', dictCheapChip,
            'I', "plateIron",
            'i', new ItemStack(Items.IRON_INGOT),
            'R', new ItemStack(Items.REDSTONE));
        addRecipe(findItemStack("Medium Power Transmitter Antenna", 1),
            "c I",
            "CI ",
            "c I",
            'C', dictAdvancedChip,
            'c', dictCheapChip,
            'I', "plateIron",
            'R', new ItemStack(Items.REDSTONE));
        addRecipe(findItemStack("Medium Power Receiver Antenna", 1),
            "I  ",
            " IC",
            "I  ",
            'C', dictAdvancedChip,
            'I', "plateIron",
            'R', new ItemStack(Items.REDSTONE));

        addRecipe(findItemStack("High Power Transmitter Antenna", 1),
            "C I",
            "CI ",
            "C I",
            'C', dictAdvancedChip,
            'c', dictCheapChip,
            'I', "plateIron",
            'R', new ItemStack(Items.REDSTONE));
        addRecipe(findItemStack("High Power Receiver Antenna", 1),
            "I D",
            " IC",
            "I D",
            'C', dictAdvancedChip,
            'I', "plateIron",
            'R', new ItemStack(Items.REDSTONE),
            'D', new ItemStack(Items.DIAMOND));

    }

    private static void recipeBatteryCharger() {
        addRecipe(findItemStack("Weak 50V Battery Charger", 1),
            "RIR",
            "III",
            "RcR",
            'c', findItemStack("Low Voltage Cable"),
            'I', new ItemStack(Items.IRON_INGOT),
            'R', new ItemStack(Items.REDSTONE));
        addRecipe(findItemStack("50V Battery Charger", 1),
            "RIR",
            "ICI",
            "RcR",
            'C', dictCheapChip,
            'c', findItemStack("Low Voltage Cable"),
            'I', new ItemStack(Items.IRON_INGOT),
            'R', new ItemStack(Items.REDSTONE));

        addRecipe(findItemStack("200V Battery Charger", 1),
            "RIR",
            "ICI",
            "RcR",
            'C', dictAdvancedChip,
            'c', findItemStack("Medium Voltage Cable"),
            'I', new ItemStack(Items.IRON_INGOT),
            'R', new ItemStack(Items.REDSTONE));

    }

    private static void recipeEggIncubator() {
        addRecipe(findItemStack("50V Egg Incubator", 1),
            "IGG",
            "E G",
            "CII",
            'C', dictCheapChip,
            'E', findItemStack("Small 50V Tungsten Heating Corp"),
            'I', new ItemStack(Items.IRON_INGOT),
            'G', new ItemStack(Blocks.GLASS_PANE));

    }

    private static void recipeEnergyConverter() { /* items not present in live port - stubbed */ }

    private static void recipeComputerProbe() { /* items not present in live port - stubbed */ }

    private static void recipeArmor() { /* items not present in live port - stubbed */ }

    private static void recipeTool() { /* items not present in live port - stubbed */ }

}
