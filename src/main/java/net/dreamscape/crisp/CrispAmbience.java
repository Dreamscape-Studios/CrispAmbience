package net.dreamscape.crisp;

import com.mojang.logging.LogUtils;
import net.dreamscape.crisp.entity.renderer.ButterflyRenderer;
import net.dreamscape.crisp.entity.renderer.SnailRenderer;
import net.dreamscape.crisp.registry.CrispItems;
import net.dreamscape.crisp.registry.CrispEntityTypes;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import software.bernie.geckolib3.GeckoLib;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(CrispAmbience.MOD_ID)
public class CrispAmbience
{
    // Define mod id in a common place for everything to reference
    public static final String MOD_ID = "crisp_ambience";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    public CrispAmbience()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        GeckoLib.initialize();

        // CrispAmbience Registries
        CrispEntityTypes.register(modEventBus);
        CrispItems.register(modEventBus);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
            EntityRenderers.register(CrispEntityTypes.BUTTERFLY.get(), ButterflyRenderer::new);
            EntityRenderers.register(CrispEntityTypes.SNAIL.get(), SnailRenderer::new);
        }
    }
}
