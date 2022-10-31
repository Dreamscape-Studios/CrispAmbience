package net.dreamscape.crisp.registry;

import net.dreamscape.crisp.CrispAmbience;
import net.dreamscape.crisp.block.BrownShelfShroom;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class CrispBlocks {
    public static final DeferredRegister BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, CrispAmbience.MOD_ID);

    public static final RegistryObject<BrownShelfShroom> BROWN_SHELF_SHROOM = BLOCKS.register("brown_shelf_shroom",
            () -> new BrownShelfShroom(BlockBehaviour.Properties.of(Material.PLANT).noCollission().instabreak()));

    public static void register(IEventBus event) {
        BLOCKS.register(event);
    }
}
