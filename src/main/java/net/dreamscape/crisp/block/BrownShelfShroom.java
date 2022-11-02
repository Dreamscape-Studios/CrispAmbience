package net.dreamscape.crisp.block;

import net.dreamscape.crisp.registry.CrispItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

/**
 * TODO:
 *  - Make the block drop items based on the current age
 */

public class BrownShelfShroom extends CropBlock implements BonemealableBlock {
    public BrownShelfShroom(Properties props) {
        super(props);
        this.registerDefaultState(this.stateDefinition.any().setValue(WATERLOGGED, false));
    }

    // LADDER CODE
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    protected static final VoxelShape EAST_AABB = Block.box(0.0D, 0.0D, 0.0D, 3.0D, 16.0D, 16.0D);
    protected static final VoxelShape WEST_AABB = Block.box(13.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    protected static final VoxelShape SOUTH_AABB = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 3.0D);
    protected static final VoxelShape NORTH_AABB = Block.box(0.0D, 0.0D, 13.0D, 16.0D, 16.0D, 16.0D);

    private boolean canAttachTo(BlockGetter pBlockReader, BlockPos pPos, Direction pDirection) {
        BlockState blockstate = pBlockReader.getBlockState(pPos);
        return blockstate.isFaceSturdy(pBlockReader, pPos, pDirection);
    }

    @Override
    protected int getBonemealAgeIncrease(Level pLevel) {
        return Mth.nextInt(pLevel.random, 1, 2);
    }

    public boolean canSurvive(BlockState pState, @NotNull LevelReader pLevel, BlockPos pPos) {
        Direction direction = pState.getValue(FACING);
        return this.canAttachTo(pLevel, pPos.relative(direction.getOpposite()), direction);
    }

    @Override
    public @NotNull VoxelShape getShape(BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext ctx) {
        Direction value = state.getValue(FACING);
        if (value == Direction.NORTH) {
            return NORTH_AABB;
        } else if (value == Direction.SOUTH) {
            return SOUTH_AABB;
        } else if (value == Direction.WEST) {
            return WEST_AABB;
        }
        return EAST_AABB;
    }


    public BlockState updateShape(BlockState pState, Direction pFacing, BlockState face, LevelAccessor levelAccessor, BlockPos currPos, BlockPos facingPos) {
        if (pFacing.getOpposite() == pState.getValue(FACING) && !pState.canSurvive(levelAccessor, currPos)) {
            return Blocks.AIR.defaultBlockState();
        } else {
            if (pState.getValue(WATERLOGGED)) {
                levelAccessor.scheduleTick(currPos, Fluids.WATER, Fluids.WATER.getTickDelay(levelAccessor));
            }

            return super.updateShape(pState, pFacing, face, levelAccessor, currPos, facingPos);
        }
    }

    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        if (!ctx.replacingClickedOnBlock()) {
            BlockState blockstate = ctx.getLevel().getBlockState(ctx.getClickedPos().relative(ctx.getClickedFace().getOpposite()));
            if (blockstate.is(this) && blockstate.getValue(FACING) == ctx.getClickedFace()) {
                return null;
            }
        }

        BlockState blockstate1 = this.defaultBlockState();
        LevelReader levelreader = ctx.getLevel();
        BlockPos blockpos = ctx.getClickedPos();
        FluidState fluidstate = ctx.getLevel().getFluidState(ctx.getClickedPos());

        for(Direction direction : ctx.getNearestLookingDirections()) {
            if (direction.getAxis().isHorizontal()) {
                blockstate1 = blockstate1.setValue(FACING, direction.getOpposite());
                if (blockstate1.canSurvive(levelreader, blockpos)) {
                    return blockstate1.setValue(WATERLOGGED, fluidstate.getType() == Fluids.WATER);
                }
            }
        }

        return null;
    }

    // CROP CODE
    public static final int MAX_AGE = 3;
    public static final IntegerProperty AGE = IntegerProperty.create("age", 0, MAX_AGE);

    public @NotNull IntegerProperty getAgeProperty() { return AGE; }
    public int getMaxAge() { return MAX_AGE; }

    @Override
    public boolean propagatesSkylightDown(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos) {
        return true;
    }

    public BlockState getStateForAge(int age, BlockState state) {
        return this.defaultBlockState().setValue(
                this.getAgeProperty(),
                Math.min(age, MAX_AGE)
        ).setValue(FACING, state.getValue(FACING));
    }

    @Override
    public void randomTick(BlockState state, ServerLevel pLevel, BlockPos pos, RandomSource random) {
        if (!pLevel.isAreaLoaded(pos, 1)) return; // Forge: prevent loading unloaded chunks when checking neighbor's light
        if (pLevel.getRawBrightness(pos, 0) >= 9) {
            int i = this.getAge(state);
            if (i < this.getMaxAge()) {
                float f = getGrowthSpeed(this, pLevel, pos);
                if (net.minecraftforge.common.ForgeHooks.onCropsGrowPre(pLevel, pos, state, random.nextInt((int)(25.0F / f) + 1) == 0)) {
                    pLevel.setBlock(pos, this.getStateForAge(i + 1, state), 2);
                    net.minecraftforge.common.ForgeHooks.onCropsGrowPost(pLevel, pos, state);
                }
            }
        }
    }

    @Override
    public void growCrops(Level level, BlockPos pos, BlockState state) {
        int i = this.getAge(state) + this.getBonemealAgeIncrease(level);
        int j = this.getMaxAge();
        if (i > j) {
            i = j;
        }

        level.setBlock(pos, this.getStateForAge(i + 1, state), 2);
    }

    @Override
    protected ItemLike getBaseSeedId() {
        return CrispItems.BROWN_SHELF_SHROOM.get();
    }

    // MISC
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE, FACING, WATERLOGGED);
    }
}
