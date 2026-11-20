package com.electrifiedded.herobrinemod;

import com.electrifiedded.archaiclib.legacy.world.LegacyBlockIds;
import com.electrifiedded.archaiclib.original.OriginalModAssets;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

/**
 * Places the original's building/trap files (assets/herobrinemod/buildings/{constructions,traps}/N.dat, read from
 * the original jar). Format: lines "tunnel", "depth", sizeX, sizeY, sizeZ, then for every x,y,z: 1.7 block id, meta.
 * Per-instance (the 1.7 code kept the loaded building in static fields shared by all builders).
 */
public class HerobrineBuilder {
    private static final Set<Integer> NON_SOLID = new HashSet<>();

    static {
        for (int id : new int[]{70, 72, 46, 6, 26, 27, 28, 30, 31, 32, 37, 38, 39, 40, 50, 51, 55, 59, 63, 64, 65, 66,
                68, 69, 71, 75, 76, 77, 78, 81, 83, 90, 93, 94, 96}) NON_SOLID.add(id);
    }

    private int tunnel = 0;
    private int depth = 0;
    private int[][][] ids;
    private int[][][] metas;

    /** @param type 0 = trap, 1 = construction */
    public boolean load(String file, int type) {
        String path = "assets/" + Reference.MOD_ID + "/buildings/" + (type == 0 ? "traps/" : "constructions/") + file;
        byte[] data = OriginalModAssets.readOriginal(Reference.MOD_ID, path);
        if (data == null) {
            HerobrineMod.LOG.warn("Herobrine didn't find the building/trap {} (original jar missing?)", path);
            return false;
        }
        try (BufferedReader in = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(data), StandardCharsets.US_ASCII))) {
            tunnel = Integer.parseInt(in.readLine().trim());
            depth = Integer.parseInt(in.readLine().trim());
            int a = Integer.parseInt(in.readLine().trim()), b = Integer.parseInt(in.readLine().trim()), c = Integer.parseInt(in.readLine().trim());
            ids = new int[a][b][c];
            metas = new int[a][b][c];
            for (int x = 0; x < a; x++)
                for (int y = 0; y < b; y++)
                    for (int z = 0; z < c; z++) {
                        ids[x][y][z] = Integer.parseInt(in.readLine().trim());
                        metas[x][y][z] = Integer.parseInt(in.readLine().trim());
                    }
            return true;
        } catch (IOException | RuntimeException e) {
            HerobrineMod.LOG.warn("Herobrine cannot read building {}: {}", path, e.toString());
            ids = null;
            return false;
        }
    }

    public boolean tryToPlace(World world, int x, int y, int z) {
        if (ids == null || ids.length == 0) return false;
        if (!areaIsEmpty(world, x, y, z)) return false;
        place(world, x, y + depth, z);
        return true;
    }

    private static boolean isGround(int id) {
        return id == 2 || id == 3 || id == 4 || id == 7 || id == 24;
    }

    private void set(World world, int x, int y, int z, int id, int meta) {
        IBlockState s = LegacyBlockIds.state(id, meta);
        BlockPos p = new BlockPos(x, y, z);
        if (world.isBlockLoaded(p)) world.setBlockState(p, s, 2);
    }

    private void place(World world, int x, int y, int z) {
        int a = ids.length, b = ids[0].length, c = ids[0][0].length;
        // pass 1: solid blocks (so torches, doors, ladders... have something to stick to)
        for (int i = 0; i < a; i++)
            for (int j = 0; j < b; j++)
                for (int k = 0; k < c; k++) {
                    int id = ids[i][j][k];
                    if (NON_SOLID.contains(id)) continue;
                    if (tunnel == 1 && isGround(id)) continue;
                    set(world, x + i + 1, y + j, z + k + 1, id, metas[i][j][k]);
                }
        // pass 2: everything, door tops
        for (int i = 0; i < a; i++)
            for (int j = 0; j < b; j++)
                for (int k = 0; k < c; k++) {
                    int id = ids[i][j][k];
                    if (!(tunnel == 1 && isGround(id))) set(world, x + i + 1, y + j, z + k + 1, id, metas[i][j][k]);
                    if (j < b - 1 && (id == 64 || id == 71)) set(world, x + i + 1, y + j + 1, z + k + 1, ids[i][j + 1][k], metas[i][j + 1][k]);
                }
    }

    private boolean areaIsEmpty(World world, int x, int y, int z) {
        int counter = 0;
        int a = ids.length, b = ids[0].length, c = ids[0][0].length;
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        if (tunnel == 0) {
            for (int l = 0; l < a; l++) {
                for (int i1 = 0; i1 < b; i1++)
                    for (int j1 = 0; j1 < c; j1++) {
                        p.setPos(x + l + 1, y + i1, z + j1 + 1);
                        if (!world.isBlockLoaded(p)) return false;
                        IBlockState s = world.getBlockState(p);
                        if (s.getBlock() != Blocks.GLASS_PANE && !world.isAirBlock(p) && s.getBlock() != Blocks.TALLGRASS) {
                            if (c * b / 2 < ++counter) return false;
                        }
                    }
                for (int l2 = 0; l2 < a; l2++)
                    for (int i2 = 0; i2 < c; i2++) {
                        p.setPos(x + l2 + 1, y - 1, z + i2);
                        if (world.isAirBlock(p)) return false;
                    }
            }
        } else {
            for (int l = 0; l < a; l++)
                for (int i1 = 0; i1 < b; i1++)
                    for (int j1 = 0; j1 < c; j1++) {
                        p.setPos(x + l + 1, y + depth + i1, z + j1 + 1);
                        if (!world.isBlockLoaded(p)) return false;
                        if (world.getBlockState(p).getBlock() == Blocks.SAND || world.isAirBlock(p)) {
                            if (5 < ++counter) return false;
                        }
                    }
        }
        return true;
    }
}
