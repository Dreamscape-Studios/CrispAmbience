package net.dreamscape.crisp.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.JumpControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WallClimberNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.material.WaterFluid;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.extensions.IForgeEntity;
import net.minecraftforge.common.extensions.IForgeLivingEntity;
import net.minecraftforge.fluids.FluidType;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.builder.ILoopType;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

import java.util.function.Predicate;

import static java.lang.Math.abs;

/**
 * TODO:
 *  - Find any other way to check if the entity is moving (The current way is horrid) {@link SnailEntity#predicate(AnimationEvent)}
 *  - Make an 'idle' animation
 *  - Make snails observe the ground and other objects randomly
 */

public class SnailEntity extends PathfinderMob implements IAnimatable {
    private static final double CLIMB_SPEED = 0.01D;
    // Geckolib
    private final AnimationFactory factory = new AnimationFactory(this);

    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        AnimationBuilder animations = new AnimationBuilder();

        if (this.isClimbing()) {
            animations.addAnimation("animation.snail.climb", ILoopType.EDefaultLoopTypes.LOOP);
        }

        // If the entity has moved from the last position
        if (!this.position().equals(new Vec3(this.xOld, this.yOld, this.zOld))) {
            event.getController().setAnimationSpeed(0.4D);
            animations.addAnimation("animation.snail.move", ILoopType.EDefaultLoopTypes.LOOP);
        } else if (!isClimbing()) {
            return PlayState.STOP;
        }

        event.getController().setAnimation(animations);
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

    private static final EntityDataAccessor<Byte> DATA_FLAGS_ID = SynchedEntityData.defineId(SnailEntity.class, EntityDataSerializers.BYTE);

    @Override
    protected @NotNull PathNavigation createNavigation(Level level) {
        return new WallClimberNavigation(this, level);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new PanicGoal(this, 0.18f));
        this.goalSelector.addGoal(1, new RandomStrollGoal(this, 0.1f));
        //this.goalSelector.addGoal(2, new RandomLookAroundGoal(this));
        //this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 6.0F));
        super.registerGoals();
    }

    public static AttributeSupplier setAttributes() {
        return Animal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 3.5f)
                .add(Attributes.ATTACK_DAMAGE, 0.0f)
                .add(Attributes.ATTACK_SPEED, 0.2f)
                .add(Attributes.MOVEMENT_SPEED, 1.2f)
                .add(Attributes.ARMOR, 8.0f)
                .add(Attributes.ARMOR_TOUGHNESS, 0.5f)
                .build();
    }

    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_FLAGS_ID, (byte)0);
    }

    @Override
    protected boolean isAffectedByFluids() {
        return false;
    }

    /**
     * Called to update the entity's position/logic.
     */
    public void tick() {
        super.tick();
        if (!this.level.isClientSide) {
            this.setClimbing(this.horizontalCollision);

            if (isClimbing()) {
                this.setDeltaMovement(this.getDeltaMovement().x, CLIMB_SPEED, this.getDeltaMovement().z);
                // Snail rotation during climbing
                if (abs(this.getDeltaMovement().x) > abs(this.getDeltaMovement().z)) {
                    this.setYRot((this.getDeltaMovement().x < 0) ? 180 : 0);
                    this.yRotO = -this.yBodyRotO;
                } else {
                    this.setYRot(90 + ((this.getDeltaMovement().z < 0) ? 180 : 0));
                    this.yRotO = -this.yBodyRotO;
                }
            }
        }
    }

    @Override
    protected float getJumpPower() {
        // NO JUMPING
        return 0.0f;
    }

    public boolean onClimbable() {
        return this.isClimbing();
    }

    /**
     * Returns true if the WatchableObject (Byte) is 0x01 otherwise returns false. The WatchableObject is updated using
     * setBesideClimableBlock.
     */
    public boolean isClimbing() {
        return (this.entityData.get(DATA_FLAGS_ID) & 1) != 0;
    }

    /**
     * Updates the WatchableObject (Byte) created in entityInit(), setting it to 0x01 if par1 is true or 0x00 if it is
     * false.
     */
    public void setClimbing(boolean pClimbing) {
        byte b0 = this.entityData.get(DATA_FLAGS_ID);
        if (pClimbing) {
            b0 = (byte)(b0 | 1);
        } else {
            b0 = (byte)(b0 & -2);
        }

        this.entityData.set(DATA_FLAGS_ID, b0);
    }


    @Override
    public boolean canDrownInFluidType(FluidType type) {
        // Snails can apparently breathe underwater now
        return false;
    }
}
