package net.gigabit101.redonestorage.wireless;

import com.refinedmods.refinedstorage.api.autocrafting.calculation.CancellationToken;
import com.refinedmods.refinedstorage.api.autocrafting.preview.Preview;
import com.refinedmods.refinedstorage.api.autocrafting.preview.TreePreview;
import com.refinedmods.refinedstorage.api.autocrafting.task.TaskId;
import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.api.network.autocrafting.AutocraftingNetworkComponent;
import com.refinedmods.refinedstorage.api.network.energy.EnergyNetworkComponent;
import com.refinedmods.refinedstorage.api.network.impl.node.grid.GridWatcherManager;
import com.refinedmods.refinedstorage.api.network.impl.node.grid.GridWatcherManagerImpl;
import com.refinedmods.refinedstorage.api.network.node.grid.EmptyGridOperations;
import com.refinedmods.refinedstorage.api.network.node.grid.GridOperations;
import com.refinedmods.refinedstorage.api.network.node.grid.GridWatcher;
import com.refinedmods.refinedstorage.api.network.storage.StorageNetworkComponent;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.NoopStorage;
import com.refinedmods.refinedstorage.api.storage.Storage;
import com.refinedmods.refinedstorage.api.storage.TrackedResourceAmount;
import com.refinedmods.refinedstorage.api.storage.root.RootStorage;
import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.security.PlatformSecurityNetworkComponent;
import com.refinedmods.refinedstorage.common.api.storage.PlayerActor;
import com.refinedmods.refinedstorage.common.api.storage.root.FuzzyRootStorage;
import com.refinedmods.refinedstorage.common.api.support.network.item.NetworkItemContext;
import com.refinedmods.refinedstorage.common.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage.common.api.support.resource.ResourceType;
import com.refinedmods.refinedstorage.common.grid.CraftingGrid;
import com.refinedmods.refinedstorage.common.grid.DirectCommitExtractTransaction;
import com.refinedmods.refinedstorage.common.grid.ExtractTransaction;
import com.refinedmods.refinedstorage.common.grid.FuzzyGridOperations;
import com.refinedmods.refinedstorage.common.grid.SecuredGridOperations;
import com.refinedmods.refinedstorage.common.grid.SnapshotExtractTransaction;
import com.refinedmods.refinedstorage.common.support.RecipeMatrix;
import com.refinedmods.refinedstorage.common.support.RecipeMatrixContainer;
import com.refinedmods.refinedstorage.common.support.resource.ItemResource;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.level.Level;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public final class RedoneWirelessCraftingGrid implements CraftingGrid {
    private static final String TAG_MATRIX = "redonestorage_crafting_matrix";

    private final NetworkItemContext context;
    private final ItemStack stack;
    private final Level level;
    private final RecipeMatrix<CraftingRecipe, CraftingInput> craftingRecipe;
    private final GridWatcherManager watchers = new GridWatcherManagerImpl();

    public RedoneWirelessCraftingGrid(final NetworkItemContext context,
                                      final ItemStack stack,
                                      final Level level) {
        this.context = context;
        this.stack = stack;
        this.level = level;
        this.craftingRecipe = RecipeMatrix.crafting(this::persistMatrix, () -> this.level);
        final CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData != null) {
            final CompoundTag tag = customData.copyTag();
            if (tag.contains(TAG_MATRIX)) {
                craftingRecipe.readFromTag(tag.getCompound(TAG_MATRIX), level.registryAccess());
                craftingRecipe.updateResult(level);
            }
        }
    }

    private Optional<Network> getNetwork() {
        return context.resolveNetwork();
    }

    private Optional<StorageNetworkComponent> getStorage() {
        return getNetwork().map(network -> network.getComponent(StorageNetworkComponent.class));
    }

    private Optional<PlatformSecurityNetworkComponent> getSecurity() {
        return getNetwork().map(network -> network.getComponent(PlatformSecurityNetworkComponent.class));
    }

    private Optional<AutocraftingNetworkComponent> getAutocrafting() {
        return getNetwork().map(network -> network.getComponent(AutocraftingNetworkComponent.class));
    }

    private void persistMatrix() {
        CustomData.update(DataComponents.CUSTOM_DATA, stack,
            tag -> tag.put(TAG_MATRIX, craftingRecipe.writeToTag(level.registryAccess())));
    }

    @Override
    public void addWatcher(final GridWatcher watcher, final Class<? extends Actor> actorType) {
        context.drainEnergy(Platform.INSTANCE.getConfig().getWirelessGrid().getOpenEnergyUsage());
        watchers.addWatcher(watcher, actorType, getStorage().orElse(null));
    }

    @Override
    public void removeWatcher(final GridWatcher watcher) {
        final StorageNetworkComponent storage = context.resolveNetwork(true)
            .map(network -> network.getComponent(StorageNetworkComponent.class))
            .orElse(null);
        watchers.removeWatcher(watcher, storage);
    }

    @Override
    public Storage getItemStorage() {
        return getStorage().map(Storage.class::cast).orElseGet(NoopStorage::new);
    }

    @Override
    public boolean isGridActive() {
        final boolean networkActive = getNetwork()
            .map(network -> !RefinedStorageApi.INSTANCE.isEnergyRequired()
                || network.getComponent(EnergyNetworkComponent.class).getStored() > 0)
            .orElse(false);
        return networkActive && context.isActive();
    }

    @Override
    public List<TrackedResourceAmount> getResources(final Class<? extends Actor> actorType) {
        return getStorage().map(storage -> storage.getResources(actorType)).orElse(Collections.emptyList());
    }

    @Override
    public Set<PlatformResourceKey> getAutocraftableResources() {
        return getAutocrafting()
            .map(AutocraftingNetworkComponent::getOutputs)
            .map(outputs -> outputs.stream()
                .filter(PlatformResourceKey.class::isInstance)
                .map(PlatformResourceKey.class::cast)
                .collect(Collectors.toSet()))
            .orElse(Collections.emptySet());
    }

    @Override
    public GridOperations createOperations(final ResourceType resourceType, final ServerPlayer player) {
        return getStorage()
            .flatMap(rootStorage -> getSecurity()
                .map(security -> createGridOperations(resourceType, player, rootStorage, security)))
            .map(operations -> (GridOperations) new RedoneWirelessGridOperations(operations, context, watchers))
            .orElse(EmptyGridOperations.INSTANCE);
    }

    private GridOperations createGridOperations(final ResourceType resourceType,
                                                final ServerPlayer player,
                                                final RootStorage rootStorage,
                                                final PlatformSecurityNetworkComponent securityNetworkComponent) {
        final PlayerActor playerActor = new PlayerActor(player);
        final GridOperations operations = resourceType.createGridOperations(rootStorage, playerActor);
        final SecuredGridOperations secured = new SecuredGridOperations(player, securityNetworkComponent, operations);
        if (rootStorage instanceof FuzzyRootStorage fuzzyRootStorage) {
            return new FuzzyGridOperations(player, fuzzyRootStorage, secured);
        }
        return secured;
    }

    @Override
    public boolean canMenuStayOpen(final Player player) {
        return true;
    }

    @Override
    public CompletableFuture<Optional<Preview>> getPreview(final ResourceKey resource,
                                                           final long amount,
                                                           final CancellationToken cancellationToken) {
        return getAutocrafting()
            .map(autocrafting -> autocrafting.getPreview(resource, amount, cancellationToken))
            .orElseGet(() -> CompletableFuture.completedFuture(Optional.empty()));
    }

    @Override
    public CompletableFuture<Optional<TreePreview>> getTreePreview(final ResourceKey resource,
                                                                   final long amount,
                                                                   final CancellationToken cancellationToken) {
        return getAutocrafting()
            .map(autocrafting -> autocrafting.getTreePreview(resource, amount, cancellationToken))
            .orElseGet(() -> CompletableFuture.completedFuture(Optional.empty()));
    }

    @Override
    public CompletableFuture<Long> getMaxAmount(final ResourceKey resource,
                                                final CancellationToken cancellationToken) {
        return getAutocrafting()
            .map(autocrafting -> autocrafting.getMaxAmount(resource, cancellationToken))
            .orElseGet(() -> CompletableFuture.completedFuture(0L));
    }

    @Override
    public Optional<TaskId> startTask(final ResourceKey resource,
                                      final long amount,
                                      final Actor actor,
                                      final boolean notify,
                                      final CancellationToken cancellationToken) {
        return getAutocrafting().flatMap(
            autocrafting -> autocrafting.startTask(resource, amount, actor, notify, cancellationToken));
    }

    @Override
    public RecipeMatrixContainer getCraftingMatrix() {
        return craftingRecipe.getMatrix();
    }

    @Override
    public ResultContainer getCraftingResult() {
        return craftingRecipe.getResult();
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(final Player player, final CraftingInput input) {
        return craftingRecipe.getRemainingItems(level, player, input);
    }

    @Override
    public ExtractTransaction startExtractTransaction(final Player player, final boolean directCommit) {
        return getStorage()
            .map(storage -> directCommit
                ? new DirectCommitExtractTransaction(storage)
                : new SnapshotExtractTransaction(player, storage, getCraftingMatrix()))
            .orElse(ExtractTransaction.NOOP);
    }

    @Override
    public boolean clearMatrix(final Player player, final boolean toPlayerInventory) {
        final boolean result = toPlayerInventory
            ? getCraftingMatrix().clearToPlayerInventory(player)
            : clearMatrixIntoStorage(player);
        persistMatrix();
        return result;
    }

    private boolean clearMatrixIntoStorage(final Player player) {
        return getStorage()
            .map(storage -> getCraftingMatrix().clearIntoStorage(storage, player))
            .orElse(false);
    }

    @Override
    public void transferRecipe(final Player player, final List<List<ItemResource>> recipe) {
        getCraftingMatrix().transferRecipe(player, getStorage().orElse(null), recipe);
        persistMatrix();
    }

    @Override
    public void acceptQuickCraft(final Player player, final ItemStack craftedStack) {
        if (player.getInventory().add(craftedStack)) {
            return;
        }
        final long inserted = getStorage()
            .map(storage -> storage.insert(
                ItemResource.ofItemStack(craftedStack),
                craftedStack.getCount(),
                Action.EXECUTE,
                new PlayerActor(player)
            ))
            .orElse(0L);
        if (inserted != craftedStack.getCount()) {
            player.drop(craftedStack.copyWithCount((int) (craftedStack.getCount() - inserted)), false);
        }
        persistMatrix();
    }
}
