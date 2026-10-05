package com.assaddon.modules;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.world.chunk.WorldChunk;

public class SusChunkV2 extends ChunkScanBase {
    public SusChunkV2() {
        super("suschunk-v2", "Weighted sus chunk finder (shulkers/beds count more, deep blocks count double). "
            + "Same idea as Krypton's sus chunk from the CHUKL addon, original implementation.", 10);
    }

    @Override
    protected int score(WorldChunk chunk) {
        int s = 0;
        for (BlockEntity be : chunk.getBlockEntities().values()) {
            int w = baseWeight(be);
            if (be.getPos().getY() < 0) w *= 2;
            s += w;
        }
        return s;
    }
}
