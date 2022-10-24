package net.dreamscape.crisp.registry;

import net.dreamscape.crisp.CrispAmbience;
import net.dreamscape.crisp.entity.ButterflyEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.RegistryObject;

public class CrispEntityTypes {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, CrispAmbience.MOD_ID);

    public static final RegistryObject<EntityType<ButterflyEntity>> BUTTERFLY =
            ENTITY_TYPES.register("butterfly",
                    () -> EntityType.Builder.of(ButterflyEntity::new, MobCategory.AMBIENT)
                            .sized(0.3f, 0.15f)
                            .build(new ResourceLocation(CrispAmbience.MOD_ID, "butterfly").toString()));

    public static void register(IEventBus event) {
        ENTITY_TYPES.register(event);
    }
}
