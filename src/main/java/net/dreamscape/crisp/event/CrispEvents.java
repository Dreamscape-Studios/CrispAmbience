package net.dreamscape.crisp.event;

import net.dreamscape.crisp.CrispAmbience;
import net.dreamscape.crisp.entity.ButterflyEntity;
import net.dreamscape.crisp.registry.CrispEntityTypes;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

public class CrispEvents {
    @Mod.EventBusSubscriber(modid = CrispAmbience.MOD_ID)
    public class ForgeEvent {

    }

    @Mod.EventBusSubscriber(modid = CrispAmbience.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public class ModEventBusEvents {
        @SubscribeEvent
        public static void entityAttributeEvent(EntityAttributeCreationEvent event) {
            event.put(CrispEntityTypes.BUTTERFLY.get(), ButterflyEntity.setAttributes());
        }
    }
}
