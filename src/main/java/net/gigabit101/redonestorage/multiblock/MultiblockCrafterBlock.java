package net.gigabit101.redonestorage.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;

public final class MultiblockCrafterBlock extends Block implements EntityBlock {
    public enum PartType {
        FRAME,
        HEAT,
        CPU,
        STORAGE
    }

    private final PartType partType;

    public MultiblockCrafterBlock(final PartType partType, final Properties properties) {
        super(properties);
        this.partType = partType;
    }

    public PartType getPartType() {
        return partType;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(final BlockPos pos, final BlockState state) {
        return new MultiblockCrafterBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(final Level level,
                                                                  final BlockState state,
                                                                  final BlockEntityType<T> type) {
        return level.isClientSide() ? null : (tickLevel, pos, tickState, blockEntity) -> {
            if (blockEntity instanceof MultiblockCrafterBlockEntity crafter) {
                MultiblockCrafterBlockEntity.serverTick(tickLevel, pos, tickState, crafter);
            }
        };
    }

    @Override
    public InteractionResult useWithoutItem(final BlockState state,
                                            final Level level,
                                            final BlockPos pos,
                                            final Player player,
                                            final BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof MultiblockCrafterBlockEntity crafter)) {
            return InteractionResult.PASS;
        }
        final MultiblockStructure structure = crafter.getStructure();
        if (!structure.valid()) {
            if (!level.isClientSide()) {
                player.displayClientMessage(Component.literal(structure.error()), true);
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(crafter, buffer -> buffer.writeBlockPos(pos));
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public void onRemove(final BlockState state,
                            final Level level,
                            final BlockPos pos,
                            final BlockState newState,
                            final boolean moved) {
        if (!state.is(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof MultiblockCrafterBlockEntity crafter
                && crafter.isPatternStorage()) {
                crafter.dropPatterns();
            }
            invalidateNearbyStructures(level, pos);
        }
        super.onRemove(state, level, pos, newState, moved);
    }

    @Override
    public void neighborChanged(final BlockState state,
                                final Level level,
                                final BlockPos pos,
                                final Block neighborBlock,
                                final BlockPos neighborPos,
                                final boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
        if (level.getBlockEntity(pos) instanceof MultiblockCrafterBlockEntity crafter) {
            crafter.invalidateStructure();
        }
    }

    private static void invalidateNearbyStructures(final Level level, final BlockPos pos) {
        for (final BlockPos nearby : BlockPos.betweenClosed(pos.offset(-1, -1, -1), pos.offset(1, 1, 1))) {
            if (level.getBlockEntity(nearby) instanceof MultiblockCrafterBlockEntity crafter) {
                crafter.invalidateStructure();
            }
        }
    }
}
