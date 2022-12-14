package net.dreamscape.crisp.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.ai.util.AirAndWaterRandomPos;
import net.minecraft.world.entity.ai.util.HoverRandomPos;
import net.minecraft.world.entity.ambient.AmbientCreature;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.core.AnimationState;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.builder.ILoopType;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

import java.util.EnumSet;
import java.util.UUID;

/**
 * TODO:
 *  Fix the jittering of the 'idle' and 'flap' animations {@link #predicate(AnimationEvent)}
 *  Add a behavior that lets the butterflies rest on flowers;
 *  Add a behavior where the butterflies prefer resting on leaves and flower bushes;
 */

public class ButterflyEntity extends AmbientCreature implements IAnimatable, NeutralMob, FlyingAnimal {

    // Geckolib
    private final AnimationFactory factory = new AnimationFactory(this);
    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        /** Play 'fly' animation whenever the butterfly is not resting */
        if (!isResting()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.butterfly.fly", ILoopType.EDefaultLoopTypes.LOOP));
            event.getController().setAnimationSpeed(5.0D);
        }
        /** Otherwise choose 'idle' or 'flap' */
        else {
            event.getController().setAnimationSpeed(1.0D);
            if (this.random.nextInt(200) == 0) {
                event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.butterfly.flap", ILoopType.EDefaultLoopTypes.PLAY_ONCE));
            } else if (event.getController().getCurrentAnimation() != null) {
                if (event.getController().getCurrentAnimation().animationName == "animation.butterfly.flap") {
                    event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.butterfly.flap", ILoopType.EDefaultLoopTypes.PLAY_ONCE));
                } else {
                    event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.butterfly.idle", ILoopType.EDefaultLoopTypes.LOOP));
                }
            }
        }
        return PlayState.CONTINUE;
    }
    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController(this, "controller", 0, this::predicate));
    }
    @Override
    public AnimationFactory getFactory() {
        return factory;
    }

    /** Initialization */
    @Nullable
    private BlockPos targetPosition;
    private static final EntityDataAccessor<Byte> DATA_ID_FLAGS = SynchedEntityData.defineId(ButterflyEntity.class, EntityDataSerializers.BYTE);
    private static final TargetingConditions BUTTERFLY_RESTING_TARGETING = TargetingConditions.forNonCombat().range(4.0D);
    private final double horizontalSpeed = 0.25D;
    private final double verticalSpeed = 0.7D;


    public ButterflyEntity(EntityType<? extends AmbientCreature> entityType, Level level) {
        super(entityType, level);
        this.moveControl = new FlyingMoveControl(this, 20, true);
        this.navigation = createNavigation(level);
        this.lookControl = this.getLookControl();
        this.setPathfindingMalus(BlockPathTypes.DANGER_FIRE, -1.0F);
        this.setPathfindingMalus(BlockPathTypes.WATER, -1.0F);
        this.setPathfindingMalus(BlockPathTypes.COCOA, -1.0F);
        this.setPathfindingMalus(BlockPathTypes.FENCE, -1.0F);
    }

    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_ID_FLAGS, (byte)0);
    }


    public static AttributeSupplier setAttributes() {
        return Animal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 1.5f)
                .add(Attributes.ATTACK_DAMAGE, 0.0f)
                .add(Attributes.ATTACK_SPEED, 0.2f)
                .add(Attributes.FLYING_SPEED, 0.1f)
                .build();
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
    }

    /** Logic */
    @Override
    public boolean isFlying() {
        return !this.onGround;
    }
    @Override
    public int getRemainingPersistentAngerTime() {
        return 0;
    }
    @Override
    public void setRemainingPersistentAngerTime(int pRemainingPersistentAngerTime) {

    }
    @Nullable @Override
    public UUID getPersistentAngerTarget() {
        return null;
    }
    @Override
    public void setPersistentAngerTarget(@Nullable UUID pPersistentAngerTarget) {

    }
    @Override
    public void startPersistentAngerTimer() {

    }

    /**
     * Bat Logic
     */
    public boolean isResting() {
        return (this.entityData.get(DATA_ID_FLAGS) & 1) != 0;
    }

    public void setResting(boolean resting) {
        byte flags = this.entityData.get(DATA_ID_FLAGS);
        if (resting) {
            this.entityData.set(DATA_ID_FLAGS, (byte)(flags | 1));
        } else {
            this.entityData.set(DATA_ID_FLAGS, (byte)(flags & -2));
        }

    }
    public void tick() {
        super.tick();
        /**
         * Gently rests the butterfly on the ground if they're slightly above it
         */
        if (this.isResting()) {
            if (isOnGround()) {
                this.setDeltaMovement(Vec3.ZERO);
            } else {
                this.setDeltaMovement(0.0D, -1.0D, 0.0D);
            }
        } else {
            /**
             * Damping the Y velocity
             */
            this.setDeltaMovement(this.getDeltaMovement().multiply(1.0D, 0.6D, 1.0D));
        }

    }
    protected void customServerAiStep() {
        super.customServerAiStep();
        BlockPos currPos = this.blockPosition();
        BlockPos belowPos = currPos.below();

        if (this.isResting()) {

            boolean flag = this.isSilent();
            if (this.level.getBlockState(belowPos).isRedstoneConductor(this.level, currPos)) {
                if (this.random.nextInt(5000) == 0) {
                    setResting(false);
                }

                if (this.level.getNearestPlayer(BUTTERFLY_RESTING_TARGETING, this) != null) {
                    this.setResting(false);
                    if (!flag) {
                        this.level.levelEvent((Player)null, 1025, currPos, 0);
                    }
                }
            } else {
                this.setResting(false);
                if (!flag) {
                    this.level.levelEvent((Player)null, 1025, currPos, 0);
                }
            }
        } else {
            if (this.targetPosition != null && (!this.level.isEmptyBlock(this.targetPosition) || this.targetPosition.getY() <= this.level.getMinBuildHeight())) {
                this.targetPosition = null;
            }

            /**
             * If there is no targeted block,
             * a random check is met,
             * or the targeted block is closer to the center than the butterfly
             */
            if (this.targetPosition == null || this.random.nextInt(30) == 0 || this.targetPosition.closerToCenterThan(this.position(), 2.0D)) {
                this.targetPosition = new BlockPos(
                        this.getX() + (double)this.random.nextInt(7) - (double)this.random.nextInt(7),
                        this.getY() + (double)this.random.nextInt(6) - 2.0D,
                        this.getZ() + (double)this.random.nextInt(7) - (double)this.random.nextInt(7));
            }

            double targetX = (double)this.targetPosition.getX() + 0.5D - this.getX();
            double targetY = (double)this.targetPosition.getY() + 0.1D - this.getY();
            double targetZ = (double)this.targetPosition.getZ() + 0.5D - this.getZ();

            Vec3 currVelocity = this.getDeltaMovement();
            Vec3 newVelocity = currVelocity.add(
                    (Math.signum(targetX) * horizontalSpeed - currVelocity.x) * (double)0.1F,
                    (Math.signum(targetY) * verticalSpeed - currVelocity.y) * (double)0.1F,
                    (Math.signum(targetZ) * horizontalSpeed - currVelocity.z) * (double)0.1F);

            this.setDeltaMovement(newVelocity);

            /**
             * Sets the rotation of the butterfly
             */
            float f = (float)(Mth.atan2(newVelocity.z, newVelocity.x) * (double)(180F / (float)Math.PI)) - 90.0F;
            float f1 = Mth.wrapDegrees(f - this.getYRot());
            this.setYRot(this.getYRot() + f1);

            if (this.random.nextInt(100) == 0
                    // Ensures the block below is not air or glass etc
                    && this.level.getBlockState(belowPos).isRedstoneConductor(this.level, currPos)
                    // Checks if the block below is not a liquid
                    && this.level.getFluidState(belowPos).isEmpty()
                    // Makes sure there are no players nearby
                    && (this.level.getNearestPlayer(BUTTERFLY_RESTING_TARGETING, this) == null)) {
                this.setResting(true);
            }
        }

    }

    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    protected void checkFallDamage(double pY, boolean pOnGround, BlockState state, BlockPos pos) {
    }

    /**
     * Return whether this entity should NOT trigger a pressure plate or a tripwire.
     */
    public boolean isIgnoringBlockTriggers() {
        return true;
    }

    /**
     * Called when the entity is attacked.
     */
    public boolean hurt(DamageSource source, float amount) {
        if (this.isInvulnerableTo(source)) {
            return false;
        } else {
            if (!this.level.isClientSide && this.isResting()) {
                this.setResting(false);
            }

            return super.hurt(source, amount);
        }
    }

    /** NBT Data */
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(DATA_ID_FLAGS, tag.getByte("ButterflyFlags"));
    }

    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putByte("ButterflyFlags", this.entityData.get(DATA_ID_FLAGS));
    }

}