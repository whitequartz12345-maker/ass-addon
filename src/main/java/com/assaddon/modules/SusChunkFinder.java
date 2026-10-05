package com.assaddon.modules;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.world.chunk.WorldChunk;

public class SusChunkFinder extends ChunkScanBase {
    public SusChunkFinder() {
        super("suschunkfinder", "Finds bases by flagging chunks with many storage/utility blocks.", 8);
    }

    @Override
    protected int score(WorldChunk chunk) {
        int s = 0;
        for (BlockEntity be : chunk.getBlockEntities().values()) s += baseWeight(be) > 0 ? 1 : 0;
        return s;
    }
}
