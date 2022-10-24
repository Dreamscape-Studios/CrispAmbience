package net.dreamscape.crisp.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.dreamscape.crisp.CrispAmbience;
import net.dreamscape.crisp.entity.ButterflyEntity;
import net.dreamscape.crisp.entity.model.ButterflyModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

public class ButterflyRenderer extends GeoEntityRenderer<ButterflyEntity> {
    public ButterflyRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new ButterflyModel());
        this.shadowRadius = 0.25f;
    }


    @Override
    public ResourceLocation getTextureLocation(ButterflyEntity instance) {
        return new ResourceLocation(CrispAmbience.MOD_ID, "textures/entity/butterfly.png");
    }

    @Override
    public RenderType getRenderType(ButterflyEntity animatable,
                                    float partialTicks,
                                    PoseStack stack,
                                    @Nullable MultiBufferSource renderTypeBuffer,
                                    @Nullable VertexConsumer vertexBuilder,
                                    int packedLightIn,
                                    ResourceLocation textureLocation) {
        stack.scale(0.8f, 0.8f, 0.8f);

        return super.getRenderType(animatable, partialTicks, stack, renderTypeBuffer, vertexBuilder, packedLightIn, textureLocation);
    }
}
