package net.dreamscape.crisp.registry;

import net.dreamscape.crisp.CrispAmbience;
import net.dreamscape.crisp.registry.CrispEntityTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class CrispItems {
    public static final DeferredRegister ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, CrispAmbience.MOD_ID);

    public static final RegistryObject<Item> BUTTERFLY_SPAWN_EGG = ITEMS.register("butterfly_spawn_egg",
            () -> new ForgeSpawnEggItem(CrispEntityTypes.BUTTERFLY, 0x2225f224, 0x194e513,
                    new Item.Properties().tab(CreativeModeTab.TAB_MISC)));

    public static final RegistryObject<Item> SNAIL_SPAWN_EGG = ITEMS.register("snail_spawn_egg",
            () -> new ForgeSpawnEggItem(CrispEntityTypes.SNAIL, 0x2345f624, 0x124ba133,
                    new Item.Properties().tab(CreativeModeTab.TAB_MISC)));

    public static final RegistryObject<Item> BROWN_SHELF_SHROOM = ITEMS.register("brown_shelf_shroom",
            () -> new Item(new Item.Properties().tab(CreativeModeTab.TAB_FOOD).food(new FoodProperties.Builder()
                    .nutrition(3)
                    .saturationMod(0.5f)
                    .effect(() -> new MobEffectInstance(MobEffects.POISON, 100, 0), 1.0F)
                    .build())));

    public static void register(IEventBus event) {
        ITEMS.register(event);
    }
}
