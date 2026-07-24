package net.gigabit101.redonestorage.multiblock;

import com.refinedmods.refinedstorage.api.autocrafting.task.Task;
import com.refinedmods.refinedstorage.api.autocrafting.task.TaskImpl;
import com.refinedmods.refinedstorage.api.autocrafting.task.TaskSnapshot;
import com.refinedmods.refinedstorage.api.network.energy.EnergyNetworkComponent;
import net.gigabit101.redonestorage.multiblock.persistence.RedoneTaskSnapshotPersistence;
import com.refinedmods.refinedstorage.api.network.impl.node.patternprovider.PatternProviderNetworkNode;
import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.support.network.AbstractNetworkNodeContainerBlockEntity;
import com.refinedmods.refinedstorage.common.api.support.network.InWorldNetworkNodeContainer;
import com.refinedmods.refinedstorage.common.support.network.SimpleConnectionStrategy;
import com.refinedmods.refinedstorage.common.util.ContainerUtil;
import net.gigabit101.redonestorage.RedoneStorageConfig;
import net.gigabit101.redonestorage.content.ModContent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.Containers;
import net.minecraft.world.level.block.state.BlockState;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;

public final class MultiblockCrafterBlockEntity
    extends AbstractNetworkNodeContainerBlockEntity<PatternProviderNetworkNode>
    implements MenuProvider {
    public static final int PATTERNS_PER_STORAGE = 78;
    private static final Logger LOGGER = LoggerFactory.getLogger(MultiblockCrafterBlockEntity.class);
    private static final String TAG_PATTERNS = "patterns";
    private static final String TAG_TASKS = "tasks";

    private final SimpleContainer patterns;
    private MultiblockStructure structure;
    private long lastStructureCheck = Long.MIN_VALUE;

    public MultiblockCrafterBlockEntity(final BlockPos pos, final BlockState state) {
        super(
            ModContent.MULTIBLOCK_CRAFTER_BLOCK_ENTITY.get(),
            pos,
            state,
            new PatternProviderNetworkNode(getEnergyUsage(state), isStorage(state) ? PATTERNS_PER_STORAGE : 0)
        );
        this.patterns = new SimpleContainer(isStorage(state) ? PATTERNS_PER_STORAGE : 0);
        this.patterns.addListener(container -> {
            refreshPatterns();
            setChanged();
        });
        this.mainNetworkNode.onAddedIntoContainer(new MultiblockPatternParentContainer(this));
        this.mainNetworkNode.setStepBehavior(new com.refinedmods.refinedstorage.api.autocrafting.task.StepBehavior() {
            @Override
            public int getSteps(final com.refinedmods.refinedstorage.api.autocrafting.Pattern pattern) {
                return Math.max(1, getStructure().cpuCount());
            }
        });
    }

    @Override
    protected InWorldNetworkNodeContainer createMainContainer(final PatternProviderNetworkNode networkNode) {
        final SimpleConnectionStrategy connectionStrategy = new SimpleConnectionStrategy(getBlockPos());
        if (isStorage(getBlockState())) {
            return new MultiblockAutocrafterNetworkNodeContainer(
                this,
                networkNode,
                "redone_multiblock_crafter",
                connectionStrategy
            );
        }
        return RefinedStorageApi.INSTANCE.createNetworkNodeContainer(this, networkNode)
            .name("redone_multiblock_crafter")
            .connectionStrategy(connectionStrategy)
            .build();
    }

    public static void serverTick(final Level level,
                                  final BlockPos pos,
                                  final BlockState state,
                                  final MultiblockCrafterBlockEntity blockEntity) {
        if (level.isClientSide()) {
            return;
        }
        final MultiblockStructure structure = blockEntity.getStructure();
        final long energyUsage = getEnergyUsage(state);
        blockEntity.mainNetworkNode.setEnergyUsage(energyUsage);
        boolean active = structure.valid() && blockEntity.mainNetworkNode.getNetwork() != null;
        if (active && RefinedStorageApi.INSTANCE.isEnergyRequired()) {
            active = blockEntity.mainNetworkNode.getNetwork()
                .getComponent(EnergyNetworkComponent.class)
                .getStored() >= energyUsage;
        }
        if (blockEntity.mainNetworkNode.isActive() != active) {
            blockEntity.mainNetworkNode.setActive(active);
            blockEntity.refreshPatterns();
        }
        if (active) {
            blockEntity.mainNetworkNode.doWork();
        }
    }

    public MultiblockStructure getStructure() {
        if (level == null) {
            return MultiblockStructure.scanPlaceholder(getBlockPos());
        }
        final long gameTime = level.getGameTime();
        if (structure == null || gameTime - lastStructureCheck >= 20) {
            structure = MultiblockStructure.scan(level, getBlockPos());
            lastStructureCheck = gameTime;
        }
        return structure;
    }

    public void invalidateStructure() {
        lastStructureCheck = Long.MIN_VALUE;
        structure = null;
    }

    public SimpleContainer getPatterns() {
        return patterns;
    }

    public boolean isPatternStorage() {
        return patterns.getContainerSize() > 0;
    }

    public void dropPatterns() {
        if (level != null && !level.isClientSide() && isPatternStorage()) {
            Containers.dropContents(level, worldPosition, patterns);
            patterns.clearContent();
        }
    }

    private void refreshPatterns() {
        if (level == null || level.isClientSide() || !isPatternStorage()) {
            return;
        }
        final boolean expose = getStructure().valid() && mainNetworkNode.isActive();
        for (int slot = 0; slot < patterns.getContainerSize(); ++slot) {
            final ItemStack stack = patterns.getItem(slot);
            mainNetworkNode.setPattern(slot, expose && !stack.isEmpty()
                ? RefinedStorageApi.INSTANCE.getPattern(stack, level).orElse(null)
                : null);
        }
    }

    @Override
    protected void saveAdditional(final CompoundTag tag, final HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        if (isPatternStorage()) {
            tag.put(TAG_PATTERNS, ContainerUtil.write(patterns, provider));
        }
        final ListTag tasks = new ListTag();
        for (final Task task : mainNetworkNode.getTasks()) {
            if (task instanceof TaskImpl taskImpl) {
                try {
                    tasks.add(RedoneTaskSnapshotPersistence.encodeSnapshot(taskImpl.createSnapshot()));
                } catch (final Exception e) {
                    LOGGER.error("Unable to save multiblock autocrafting task", e);
                }
            }
        }
        tag.put(TAG_TASKS, tasks);
    }

    @Override
    protected void loadAdditional(final CompoundTag tag, final HolderLookup.Provider provider) {
        if (isPatternStorage() && tag.contains(TAG_PATTERNS)) {
            ContainerUtil.read(tag.getCompound(TAG_PATTERNS), patterns, provider);
        }
        if (tag.contains(TAG_TASKS)) {
            final ListTag tasks = tag.getList(TAG_TASKS, Tag.TAG_COMPOUND);
            for (int i = 0; i < tasks.size(); ++i) {
                try {
                    final TaskSnapshot snapshot = RedoneTaskSnapshotPersistence.decodeSnapshot(tasks.getCompound(i));
                    mainNetworkNode.addTask(new TaskImpl(snapshot));
                } catch (final Exception e) {
                    LOGGER.error("Unable to load multiblock autocrafting task; skipping it", e);
                }
            }
        }
        super.loadAdditional(tag, provider);
        refreshPatterns();
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("gui.redonestorage.multiblock_crafter");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(final int containerId, final Inventory inventory, final Player player) {
        return new MultiblockCrafterMenu(containerId, inventory, this);
    }

    private static boolean isStorage(final BlockState state) {
        return state.getBlock() instanceof MultiblockCrafterBlock block
            && block.getPartType() == MultiblockCrafterBlock.PartType.STORAGE;
    }

    private static long getEnergyUsage(final BlockState state) {
        if (!(state.getBlock() instanceof MultiblockCrafterBlock block)) {
            return 0;
        }
        return switch (block.getPartType()) {
            case FRAME -> RedoneStorageConfig.FRAME_COST.get();
            case HEAT -> RedoneStorageConfig.HEAT_COST.get();
            case CPU -> RedoneStorageConfig.CPU_COST.get();
            case STORAGE -> RedoneStorageConfig.STORAGE_COST.get();
        };
    }
}
