package net.dreamscape.crisp.entity.model;

import net.dreamscape.crisp.CrispAmbience;
import net.dreamscape.crisp.entity.ButterflyEntity;
import net.dreamscape.crisp.entity.SnailEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class SnailModel extends AnimatedGeoModel<SnailEntity> {
    @Override
    public ResourceLocation getModelResource(SnailEntity object) {
        return new ResourceLocation(CrispAmbience.MOD_ID, "geo/snail.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(SnailEntity object) {
        return new ResourceLocation(CrispAmbience.MOD_ID, "textures/entity/snail.png");
    }

    @Override
    public ResourceLocation getAnimationResource(SnailEntity animatable) {
        return new ResourceLocation(CrispAmbience.MOD_ID, "animations/snail.animation.json");
    }
}
