package net.gigabit101.redonestorage.networking;

import com.refinedmods.refinedstorage.api.network.impl.node.AbstractNetworkNode;
import com.refinedmods.refinedstorage.common.api.support.network.ConnectionStrategy;
import com.refinedmods.refinedstorage.common.api.support.network.item.NetworkItemPlayerValidator;
import com.refinedmods.refinedstorage.common.support.network.InWorldNetworkNodeContainerImpl;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

final class AdvancedWirelessTransmitterNetworkNodeContainer extends InWorldNetworkNodeContainerImpl
    implements NetworkItemPlayerValidator {
    private final AdvancedWirelessTransmitterBlockEntity blockEntity;
    private final AbstractNetworkNode node;

    AdvancedWirelessTransmitterNetworkNodeContainer(final AdvancedWirelessTransmitterBlockEntity blockEntity,
                                                     final AbstractNetworkNode node,
                                                     final ConnectionStrategy connectionStrategy) {
        super(blockEntity, node, "main", 0, connectionStrategy, null);
        this.blockEntity = blockEntity;
        this.node = node;
    }

    @Override
    public boolean isValid(final PlayerCoordinates coordinates) {
        final Level level = blockEntity.getLevel();
        if (level == null || level.dimension() != coordinates.dimension() || !node.isActive()) {
            return false;
        }
        final BlockPos pos = blockEntity.getBlockPos();
        final Vec3 playerPos = coordinates.position();
        final double distanceSquared = pos.distToCenterSqr(playerPos.x(), playerPos.y(), playerPos.z());
        final long range = blockEntity.getRange();
        return distanceSquared <= range * range;
    }
}
