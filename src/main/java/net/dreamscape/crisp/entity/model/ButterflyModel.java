package net.dreamscape.crisp.entity.model;

import net.dreamscape.crisp.CrispAmbience;
import net.dreamscape.crisp.entity.ButterflyEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class ButterflyModel extends AnimatedGeoModel<ButterflyEntity> {
    @Override
    public ResourceLocation getModelResource(ButterflyEntity object) {
        return new ResourceLocation(CrispAmbience.MOD_ID, "geo/butterfly.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(ButterflyEntity object) {
        return new ResourceLocation(CrispAmbience.MOD_ID, "textures/entity/butterfly.png");
    }

    @Override
    public ResourceLocation getAnimationResource(ButterflyEntity animatable) {
        return new ResourceLocation(CrispAmbience.MOD_ID, "animations/butterfly.animation.json");
    }
}
