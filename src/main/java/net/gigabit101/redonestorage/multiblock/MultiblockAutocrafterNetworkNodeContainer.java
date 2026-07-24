package net.gigabit101.redonestorage.multiblock;

import com.refinedmods.refinedstorage.api.network.node.NetworkNode;
import com.refinedmods.refinedstorage.common.api.autocrafting.Autocrafter;
import com.refinedmods.refinedstorage.common.api.support.network.ConnectionStrategy;
import com.refinedmods.refinedstorage.common.support.network.InWorldNetworkNodeContainerImpl;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;

/**
 * Exposes each Crafting Storage block as an RS2 autocrafter page.
 *
 * <p>The RS2 Autocrafter Manager discovers autocrafters by looking for network
 * containers that implement {@link Autocrafter}. A normal generic network
 * container will craft correctly, but it will not appear in the manager.</p>
 */
final class MultiblockAutocrafterNetworkNodeContainer
    extends InWorldNetworkNodeContainerImpl
    implements Autocrafter {
    private final MultiblockCrafterBlockEntity blockEntity;

    MultiblockAutocrafterNetworkNodeContainer(final MultiblockCrafterBlockEntity blockEntity,
                                              final NetworkNode networkNode,
                                              final String name,
                                              final ConnectionStrategy connectionStrategy) {
        super(blockEntity, networkNode, name, 0, connectionStrategy, null);
        this.blockEntity = blockEntity;
    }

    @Override
    public Component getAutocrafterName() {
        return blockEntity.getDisplayName();
    }

    @Override
    public Container getPatternContainer() {
        return blockEntity.getPatterns();
    }

    @Override
    public boolean isVisibleToTheAutocrafterManager() {
        return true;
    }
}
