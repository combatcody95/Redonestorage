package net.gigabit101.redonestorage.networking;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;

public final class AdvancedWirelessTransmitterBlock extends Block implements EntityBlock {
    public AdvancedWirelessTransmitterBlock() {
        super(BlockBehaviour.Properties.of().strength(2.0F).requiresCorrectToolForDrops().noOcclusion());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(final BlockPos pos, final BlockState state) {
        return new AdvancedWirelessTransmitterBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(final Level level,
                                                                  final BlockState state,
                                                                  final BlockEntityType<T> type) {
        return level.isClientSide() ? null : (tickLevel, pos, tickState, blockEntity) -> {
            if (blockEntity instanceof AdvancedWirelessTransmitterBlockEntity transmitter) {
                AdvancedWirelessTransmitterBlockEntity.serverTick(tickLevel, pos, tickState, transmitter);
            }
        };
    }

    @Override
    public InteractionResult useWithoutItem(final BlockState state,
                                            final Level level,
                                            final BlockPos pos,
                                            final Player player,
                                            final BlockHitResult hitResult) {
        if (player instanceof ServerPlayer serverPlayer
            && level.getBlockEntity(pos) instanceof AdvancedWirelessTransmitterBlockEntity transmitter) {
            serverPlayer.openMenu(transmitter, buffer -> buffer.writeBlockPos(pos));
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public void onRemove(final BlockState state,
                         final Level level,
                         final BlockPos pos,
                         final BlockState newState,
                         final boolean moved) {
        if (!state.is(newState.getBlock())
            && level.getBlockEntity(pos) instanceof AdvancedWirelessTransmitterBlockEntity transmitter) {
            transmitter.dropUpgrades();
        }
        super.onRemove(state, level, pos, newState, moved);
    }
}
