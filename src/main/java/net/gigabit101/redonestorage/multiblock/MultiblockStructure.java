package net.gigabit101.redonestorage.multiblock;

import net.gigabit101.redonestorage.RedoneStorageConfig;
import net.gigabit101.redonestorage.content.ModContent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Describes one assembled Redone Storage multiblock crafter.
 *
 * <p>The layout intentionally matches the established multiblock rules:</p>
 * <ul>
 *     <li>Every edge and corner must be a crafting frame.</li>
 *     <li>The non-edge portions of all six outside faces must be heat conductors.</li>
 *     <li>Every interior position must be either a crafting CPU or crafting storage.</li>
 * </ul>
 */
public record MultiblockStructure(
    boolean valid,
    String error,
    BlockPos min,
    BlockPos max,
    List<BlockPos> parts,
    List<BlockPos> storages,
    int cpuCount,
    int heatCount
) {
    private static final Comparator<BlockPos> POSITION_ORDER = Comparator
        .comparingInt((BlockPos pos) -> pos.getY())
        .thenComparingInt(pos -> pos.getZ())
        .thenComparingInt(pos -> pos.getX());

    public static MultiblockStructure scanPlaceholder(final BlockPos start) {
        return invalid(start, "The level is not available yet.");
    }

    public static MultiblockStructure scan(final Level level, final BlockPos start) {
        if (!level.isLoaded(start) || !ModContent.isMultiblockPart(level.getBlockState(start).getBlock())) {
            return invalid(start, "This block is not part of a Redone Storage crafter.");
        }

        final int maximumDimension = RedoneStorageConfig.MAX_SIZE.get();
        final int maximumParts = maximumDimension * maximumDimension * maximumDimension;
        final Set<BlockPos> found = collectConnectedParts(level, start, maximumParts);
        if (found == null) {
            return invalid(start, "The connected crafter is larger than the configured maximum.");
        }

        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        int maxZ = Integer.MIN_VALUE;
        for (final BlockPos pos : found) {
            minX = Math.min(minX, pos.getX());
            minY = Math.min(minY, pos.getY());
            minZ = Math.min(minZ, pos.getZ());
            maxX = Math.max(maxX, pos.getX());
            maxY = Math.max(maxY, pos.getY());
            maxZ = Math.max(maxZ, pos.getZ());
        }

        final int xSize = maxX - minX + 1;
        final int ySize = maxY - minY + 1;
        final int zSize = maxZ - minZ + 1;
        final int minimumDimension = RedoneStorageConfig.MIN_SIZE.get();
        if (xSize < minimumDimension || ySize < minimumDimension || zSize < minimumDimension) {
            return invalid(start, "The crafter must be at least " + minimumDimension + " blocks on every axis. "
                + "Detected " + xSize + " x " + ySize + " x " + zSize + ".");
        }
        if (xSize > maximumDimension || ySize > maximumDimension || zSize > maximumDimension) {
            return invalid(start, "The crafter exceeds the configured maximum dimension of " + maximumDimension + ".");
        }

        final BlockPos min = new BlockPos(minX, minY, minZ);
        final BlockPos max = new BlockPos(maxX, maxY, maxZ);
        final List<BlockPos> parts = new ArrayList<>(xSize * ySize * zSize);
        final List<BlockPos> storages = new ArrayList<>();
        int cpuCount = 0;
        int heatCount = 0;

        for (int x = minX; x <= maxX; ++x) {
            for (int y = minY; y <= maxY; ++y) {
                for (int z = minZ; z <= maxZ; ++z) {
                    final BlockPos pos = new BlockPos(x, y, z);
                    final BlockState state = level.getBlockState(pos);
                    if (!(state.getBlock() instanceof MultiblockCrafterBlock block)) {
                        return invalid(start, "The crafter is missing a block at " + pos.toShortString() + ".");
                    }

                    final int extremes = countBoundaryAxes(pos, min, max);
                    final MultiblockCrafterBlock.PartType actual = block.getPartType();
                    if (extremes >= 2) {
                        if (actual != MultiblockCrafterBlock.PartType.FRAME) {
                            return invalid(start, "A Crafting Frame is required on the edge at "
                                + pos.toShortString() + ", but found " + partName(actual) + ".");
                        }
                    } else if (extremes == 1) {
                        if (actual != MultiblockCrafterBlock.PartType.HEAT) {
                            return invalid(start, "A Heat Conductor is required on the outside face at "
                                + pos.toShortString() + ", but found " + partName(actual) + ".");
                        }
                        ++heatCount;
                    } else {
                        if (actual == MultiblockCrafterBlock.PartType.CPU) {
                            ++cpuCount;
                        } else if (actual == MultiblockCrafterBlock.PartType.STORAGE) {
                            storages.add(pos.immutable());
                        } else {
                            return invalid(start, "The interior block at " + pos.toShortString()
                                + " must be a Crafting CPU or Crafting Storage, but found " + partName(actual) + ".");
                        }
                    }
                    parts.add(pos.immutable());
                }
            }
        }

        if (storages.isEmpty()) {
            return invalid(start, "The crafter needs at least one Crafting Storage block in its interior.");
        }
        if (cpuCount == 0) {
            return invalid(start, "The crafter needs at least one Crafting CPU block in its interior.");
        }

        parts.sort(POSITION_ORDER);
        storages.sort(POSITION_ORDER);
        return new MultiblockStructure(true, "", min, max, List.copyOf(parts), List.copyOf(storages),
            cpuCount, heatCount);
    }

    private static Set<BlockPos> collectConnectedParts(final Level level,
                                                        final BlockPos start,
                                                        final int maximumParts) {
        final Set<BlockPos> found = new HashSet<>();
        final ArrayDeque<BlockPos> open = new ArrayDeque<>();
        open.add(start.immutable());
        while (!open.isEmpty()) {
            final BlockPos pos = open.removeFirst();
            if (!found.add(pos)) {
                continue;
            }
            if (found.size() > maximumParts) {
                return null;
            }
            for (final Direction direction : Direction.values()) {
                final BlockPos next = pos.relative(direction);
                if (!found.contains(next)
                    && level.isLoaded(next)
                    && ModContent.isMultiblockPart(level.getBlockState(next).getBlock())) {
                    open.add(next.immutable());
                }
            }
        }
        return found;
    }

    private static int countBoundaryAxes(final BlockPos pos, final BlockPos min, final BlockPos max) {
        int extremes = 0;
        if (pos.getX() == min.getX() || pos.getX() == max.getX()) {
            ++extremes;
        }
        if (pos.getY() == min.getY() || pos.getY() == max.getY()) {
            ++extremes;
        }
        if (pos.getZ() == min.getZ() || pos.getZ() == max.getZ()) {
            ++extremes;
        }
        return extremes;
    }

    private static String partName(final MultiblockCrafterBlock.PartType type) {
        return switch (type) {
            case FRAME -> "Crafting Frame";
            case HEAT -> "Heat Conductor";
            case CPU -> "Crafting CPU";
            case STORAGE -> "Crafting Storage";
        };
    }

    private static MultiblockStructure invalid(final BlockPos start, final String message) {
        return new MultiblockStructure(false, message, start.immutable(), start.immutable(), List.of(), List.of(), 0, 0);
    }
}
