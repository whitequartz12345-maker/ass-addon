package com.assaddon.modules;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.world.chunk.WorldChunk;

public class StarChunkFinder extends ChunkScanBase {
    public StarChunkFinder() {
        super("star-chunkfinder", "Finds bases hidden deep underground (counts only blocks below Y=0).", 3);
    }

    @Override
    protected int score(WorldChunk chunk) {
        int s = 0;
        for (BlockEntity be : chunk.getBlockEntities().values()) {
            if (be.getPos().getY() < 0) s += baseWeight(be);
        }
        return s;
    }
}
