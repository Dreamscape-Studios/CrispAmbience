package net.dreamscape.crisp.entity;

import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.WaterFluid;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.extensions.IForgeEntity;
import net.minecraftforge.common.extensions.IForgeLivingEntity;
import net.minecraftforge.fluids.FluidType;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.builder.ILoopType;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

import java.util.function.Predicate;

/**
 * TODO:
 *  - Find any other way to check if the entity is moving (The current way is horrid) {@link SnailEntity#predicate(AnimationEvent)}
 *  - Make an 'idle' animation
 *  - Make snails observe the ground and other objects randomly
 */

public class SnailEntity extends PathfinderMob implements IAnimatable {
    // Geckolib
    private final AnimationFactory factory = new AnimationFactory(this);

    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        // If the entity has moved from the last position
        if (!this.position().equals(new Vec3(this.xOld, this.yOld, this.zOld))) {
            event.getController().setAnimationSpeed(0.6D);
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.snail.move", ILoopType.EDefaultLoopTypes.LOOP));
        } else {
            return PlayState.STOP;
        }
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimationData data) { data.addAnimationController(new AnimationController(this, "controller", 0, this::predicate)); }
    @Override
    public AnimationFactory getFactory() { return factory; }

    // Initialization
    public SnailEntity(EntityType<? extends PathfinderMob> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.moveControl = new MoveControl(this);
        this.navigation = createNavigation(level);
        this.lookControl = this.getLookControl();
        this.setPathfindingMalus(BlockPathTypes.DANGER_FIRE, -1.0F);
        this.setPathfindingMalus(BlockPathTypes.COCOA, -1.0F);
        this.setPathfindingMalus(BlockPathTypes.FENCE, -1.0F);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new PanicGoal(this, 0.18f));
        this.goalSelector.addGoal(1, new RandomStrollGoal(this, 0.1f));
        this.goalSelector.addGoal(2, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 6.0F));
        super.registerGoals();
    }

    public static AttributeSupplier setAttributes() {
        return Animal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 3.5f)
                .add(Attributes.ATTACK_DAMAGE, 0.0f)
                .add(Attributes.ATTACK_SPEED, 0.2f)
                .add(Attributes.MOVEMENT_SPEED, 0.6f)
                .build();
    }

    @Override
    public boolean canDrownInFluidType(FluidType type) {
        // Snails can apparently breathe underwater now
        return false;
    }
}
