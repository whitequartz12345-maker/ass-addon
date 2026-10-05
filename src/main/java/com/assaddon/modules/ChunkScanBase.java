package com.assaddon.modules;

import com.assaddon.AssAddon;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.RenderUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.meteorclient.events.world.TickEvent;
import net.minecraft.block.entity.*;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;

import java.util.HashMap;
import java.util.Map;

/** Shared scanner: scores loaded chunks by player-built block entities and highlights high scorers. */
public abstract class ChunkScanBase extends Module {
    private final SettingGroup sg = settings.getDefaultGroup();
    protected final Setting<Integer> threshold;
    protected final Setting<Integer> range = sg.add(new IntSetting.Builder()
        .name("range").description("Chunk radius to scan.").defaultValue(10).min(2).sliderMax(32).build());
    protected final Setting<Boolean> tracers = sg.add(new BoolSetting.Builder()
        .name("tracers").description("Draw lines to flagged chunks.").defaultValue(false).build());
    protected final Setting<SettingColor> color = sg.add(new ColorSetting.Builder()
        .name("color").defaultValue(new SettingColor(255, 60, 60, 255)).build());

    private final Map<ChunkPos, Integer> scores = new HashMap<>();
    private int timer;

    protected ChunkScanBase(String name, String description, int defaultThreshold) {
        super(AssAddon.CATEGORY, name, description);
        threshold = sg.add(new IntSetting.Builder().name("threshold")
            .description("Minimum score to flag a chunk.").defaultValue(defaultThreshold).min(1).sliderMax(100).build());
    }

    protected abstract int score(WorldChunk chunk);

    /** Weight of a single block entity for the basic score. */
    protected static int baseWeight(BlockEntity be) {
        if (be instanceof ShulkerBoxBlockEntity) return 3;
        if (be instanceof ChestBlockEntity || be instanceof BarrelBlockEntity || be instanceof EnderChestBlockEntity
            || be instanceof HopperBlockEntity || be instanceof AbstractFurnaceBlockEntity
            || be instanceof DispenserBlockEntity) return 1;
        if (be instanceof BedBlockEntity) return 2;
        return 0;
    }

    @Override
    public void onDeactivate() {
        scores.clear();
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.world == null || mc.player == null) return;
        if (++timer >= 200) { timer = 0; scores.clear(); }

        int r = range.get();
        ChunkPos pc = mc.player.getChunkPos();
        scores.keySet().removeIf(p -> Math.abs(p.x - pc.x) > r || Math.abs(p.z - pc.z) > r);

        int budget = 3;
        for (int dx = -r; dx <= r && budget > 0; dx++) {
            for (int dz = -r; dz <= r && budget > 0; dz++) {
                ChunkPos pos = new ChunkPos(pc.x + dx, pc.z + dz);
                if (scores.containsKey(pos)) continue;
                Chunk c = mc.world.getChunk(pos.x, pos.z, ChunkStatus.FULL, false);
                if (c instanceof WorldChunk wc) {
                    scores.put(pos, score(wc));
                    budget--;
                }
            }
        }
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (mc.world == null) return;
        double y1 = mc.world.getBottomY();
        double y2 = y1 + mc.world.getHeight();
        SettingColor line = color.get();
        SettingColor side = new SettingColor(line.r, line.g, line.b, 30);

        for (Map.Entry<ChunkPos, Integer> e : scores.entrySet()) {
            if (e.getValue() < threshold.get()) continue;
            ChunkPos p = e.getKey();
            event.renderer.box(p.getStartX(), y1, p.getStartZ(), p.getStartX() + 16, y2, p.getStartZ() + 16,
                side, line, ShapeMode.Both, 0);
            if (tracers.get()) {
                event.renderer.line(RenderUtils.center.x, RenderUtils.center.y, RenderUtils.center.z,
                    p.getCenterX(), mc.player.getY(), p.getCenterZ(), line);
            }
        }
    }

    @Override
    public String getInfoString() {
        long n = scores.values().stream().filter(s -> s >= threshold.get()).count();
        return String.valueOf(n);
    }
}
