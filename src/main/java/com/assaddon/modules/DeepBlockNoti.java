package com.assaddon.modules;

import com.assaddon.AssAddon;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;

import java.util.*;

public class DeepBlockNoti extends Module {
    private final SettingGroup sg = settings.getDefaultGroup();
    private final Setting<List<Block>> blocks = sg.add(new BlockListSetting.Builder()
        .name("blocks").description("Blocks to look for.")
        .defaultValue(Blocks.CHEST, Blocks.TRAPPED_CHEST, Blocks.BARREL, Blocks.ENDER_CHEST, Blocks.SPAWNER)
        .build());
    private final Setting<Integer> maxY = sg.add(new IntSetting.Builder()
        .name("max-y").description("Only report blocks at or below this Y.").defaultValue(-10)
        .sliderRange(-64, 64).build());
    private final Setting<Integer> range = sg.add(new IntSetting.Builder()
        .name("range").defaultValue(10).min(1).sliderMax(32).build());
    private final Setting<SettingColor> color = sg.add(new ColorSetting.Builder()
        .name("color").defaultValue(new SettingColor(0, 200, 255, 255)).build());

    private final Set<ChunkPos> done = new HashSet<>();
    private final List<BlockPos> found = new ArrayList<>();

    public DeepBlockNoti() {
        super(AssAddon.CATEGORY, "deep-block-noti",
            "Notifies when loaded chunks contain chosen blocks deep underground and highlights them.");
    }

    @Override
    public void onDeactivate() {
        done.clear();
        found.clear();
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.world == null || mc.player == null) return;
        ChunkPos pc = mc.player.getChunkPos();
        int r = range.get();
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                ChunkPos pos = new ChunkPos(pc.x + dx, pc.z + dz);
                if (done.contains(pos)) continue;
                Chunk c = mc.world.getChunk(pos.x, pos.z, ChunkStatus.FULL, false);
                if (!(c instanceof WorldChunk wc)) continue;
                done.add(pos);
                scan(wc, pos);
                return; // one chunk per tick
            }
        }
    }

    private void scan(WorldChunk chunk, ChunkPos pos) {
        BlockPos.Mutable mp = new BlockPos.Mutable();
        int count = 0;
        int bottom = mc.world.getBottomY();
        int top = Math.min(maxY.get(), mc.world.getBottomY() + mc.world.getHeight() - 1);
        BlockPos first = null;
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = bottom; y <= top; y++) {
                    mp.set(pos.getStartX() + x, y, pos.getStartZ() + z);
                    if (blocks.get().contains(chunk.getBlockState(mp).getBlock())) {
                        count++;
                        if (found.size() < 2000) found.add(mp.toImmutable());
                        if (first == null) first = mp.toImmutable();
                    }
                }
            }
        }
        if (count > 0) info("%d deep block(s) in chunk [%d, %d], e.g. at %d %d %d",
            count, pos.x, pos.z, first.getX(), first.getY(), first.getZ());
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        SettingColor line = color.get();
        SettingColor side = new SettingColor(line.r, line.g, line.b, 40);
        for (BlockPos p : found) event.renderer.box(p, side, line, ShapeMode.Both, 0);
    }
}
