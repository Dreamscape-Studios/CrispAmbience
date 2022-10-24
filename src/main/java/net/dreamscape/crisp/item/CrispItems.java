package net.dreamscape.crisp.item;

import net.dreamscape.crisp.CrispAmbience;
import net.dreamscape.crisp.registry.CrispEntityTypes;
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

    public static void register(IEventBus event) {
        ITEMS.register(event);
    }
}
