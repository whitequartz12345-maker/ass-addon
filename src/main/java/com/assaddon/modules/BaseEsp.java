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
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;

import java.util.ArrayList;
import java.util.List;

public class BaseEsp extends Module {
    private final SettingGroup sg = settings.getDefaultGroup();
    private final Setting<List<Block>> blocks = sg.add(new BlockListSetting.Builder()
        .name("blocks").description("Base blocks to highlight.")
        .defaultValue(Blocks.CHEST, Blocks.TRAPPED_CHEST, Blocks.BARREL, Blocks.ENDER_CHEST, Blocks.HOPPER,
            Blocks.FURNACE, Blocks.BLAST_FURNACE, Blocks.SMOKER, Blocks.DISPENSER, Blocks.DROPPER)
        .build());
    private final Setting<Integer> range = sg.add(new IntSetting.Builder()
        .name("range").description("Chunk radius.").defaultValue(8).min(1).sliderMax(32).build());
    private final Setting<SettingColor> color = sg.add(new ColorSetting.Builder()
        .name("color").defaultValue(new SettingColor(255, 170, 0, 255)).build());

    private final List<BlockPos> found = new ArrayList<>();
    private int timer;

    public BaseEsp() {
        super(AssAddon.CATEGORY, "base-esp", "Highlights base blocks (storage, furnaces...) in loaded chunks.");
    }

    @Override
    public void onDeactivate() {
        found.clear();
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.world == null || mc.player == null) return;
        if (timer-- > 0) return;
        timer = 20;
        found.clear();
        ChunkPos pc = mc.player.getChunkPos();
        int r = range.get();
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                Chunk c = mc.world.getChunk(pc.x + dx, pc.z + dz, ChunkStatus.FULL, false);
                if (!(c instanceof WorldChunk wc)) continue;
                for (BlockEntity be : wc.getBlockEntities().values()) {
                    if (blocks.get().contains(be.getCachedState().getBlock())) found.add(be.getPos().toImmutable());
                }
            }
        }
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        SettingColor line = color.get();
        SettingColor side = new SettingColor(line.r, line.g, line.b, 40);
        for (BlockPos p : found) event.renderer.box(p, side, line, ShapeMode.Both, 0);
    }
}
