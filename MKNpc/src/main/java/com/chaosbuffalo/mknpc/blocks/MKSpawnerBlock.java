package com.chaosbuffalo.mknpc.blocks;

import com.chaosbuffalo.mknpc.init.MKNpcTileEntityTypes;
import com.chaosbuffalo.mknpc.network.OpenMKSpawnerPacket;
import com.chaosbuffalo.mknpc.network.PacketHandler;
import com.chaosbuffalo.mknpc.tile_entities.MKSpawnerTileEntity;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;



import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.Rotation;
import net.minecraftforge.network.NetworkDirection;

public class MKSpawnerBlock extends BaseEntityBlock {


    public enum MKSpawnerOrientation implements StringRepresentable {
        EAST("east", Direction.EAST),
        WEST("west", Direction.WEST),
        SOUTH("south", Direction.SOUTH),
        NORTH("north", Direction.NORTH);

        private final String name;
        private final Direction direction;

        MKSpawnerOrientation(String name, Direction direction){
            this.name = name;
            this.direction = direction;
        }

        public Direction getDirection() {
            return direction;
        }

        public float getAngleInDegrees(){
            return switch (this) {
                case EAST -> 270;
                case WEST -> 90;
                case SOUTH -> 0;
                case NORTH -> 180;
            };
        }

        public MKSpawnerOrientation rotate(Rotation rot){
            return switch (rot) {
                case NONE -> this;
                case CLOCKWISE_90 -> switch (this) {
                    case EAST -> SOUTH;
                    case SOUTH -> WEST;
                    case WEST -> NORTH;
                    case NORTH -> EAST;
                };
                case CLOCKWISE_180 -> switch (this) {
                    case EAST -> WEST;
                    case SOUTH -> NORTH;
                    case WEST -> EAST;
                    case NORTH -> SOUTH;
                };
                case COUNTERCLOCKWISE_90 -> switch (this) {
                    case EAST -> NORTH;
                    case SOUTH -> EAST;
                    case WEST -> SOUTH;
                    case NORTH -> WEST;
                };
            };
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }
    public static final EnumProperty<MKSpawnerOrientation> ORIENTATION = EnumProperty.create("orientation", MKSpawnerOrientation.class);

    private final VoxelShape shape = Block.box(0, 0, 0, 16.0, 1.0, 16.0);
    public static final Material SPAWNER_MATERIAL = new Material(MaterialColor.NONE, false,
            false, true, false, false, false,
            PushReaction.IGNORE);

    public MKSpawnerBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(ORIENTATION, MKSpawnerOrientation.NORTH));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos p_153215_, BlockState p_153216_) {
        return new MKSpawnerTileEntity(p_153215_, p_153216_);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
        return level.isClientSide() ? null : createTickerHelper(blockEntityType,
                MKNpcTileEntityTypes.MK_SPAWNER_TILE_ENTITY_TYPE.get(), MKSpawnerTileEntity::spawnerTick);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return shape;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(ORIENTATION);
    }

    @Override
    public BlockState rotate(BlockState state, LevelAccessor world, BlockPos pos, Rotation direction) {
        return state.setValue(ORIENTATION, state.getValue(ORIENTATION).rotate(direction));
    }


    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(ORIENTATION, state.getValue(ORIENTATION).rotate(rot));
    }

//    @Override
//    public BlockState mirror(BlockState state, Mirror mirrorIn) {
//        return state.with(ORIENTATION, state.get(ORIENTATION).rotate(Rotation.CLOCKWISE_180));
//    }

    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player,
                                             InteractionHand handIn, BlockHitResult hit) {
        if (handIn.equals(InteractionHand.MAIN_HAND)){
            if (!worldIn.isClientSide() && player.isCreative()){
                if (player.isShiftKeyDown()){
                    worldIn.setBlockAndUpdate(pos, state.setValue(ORIENTATION, getNextOrientation(state.getValue(ORIENTATION))));
                    BlockEntity spawner = worldIn.getBlockEntity(pos);
                    if (spawner instanceof MKSpawnerTileEntity){
                        ((MKSpawnerTileEntity) spawner).clearSpawn();
                    }
                }
                else {
                    ((ServerPlayer) player).connection.send(
                            PacketHandler.getNetworkChannel().toVanillaPacket(
                                    new OpenMKSpawnerPacket((MKSpawnerTileEntity) worldIn.getBlockEntity(pos)),
                                    NetworkDirection.PLAY_TO_CLIENT));
                }

            }
            return InteractionResult.SUCCESS;
        } else {
            return InteractionResult.PASS;
        }
    }

    @Override
    public RenderShape getRenderShape(BlockState p_49232_) {
        return RenderShape.MODEL;
    }

    protected MKSpawnerOrientation getNextOrientation(MKSpawnerOrientation in){
        return switch (in) {
            case EAST -> MKSpawnerOrientation.SOUTH;
            case SOUTH -> MKSpawnerOrientation.WEST;
            case WEST -> MKSpawnerOrientation.NORTH;
            default -> MKSpawnerOrientation.EAST;
        };
    }

}
