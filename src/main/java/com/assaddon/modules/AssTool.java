package com.assaddon.modules;

import com.assaddon.AssAddon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;

public class AssTool extends Module {
    public AssTool() {
        super(AssAddon.CATEGORY, "ass-tool", "Selects the fastest hotbar tool for the block you are mining.");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.world == null || !mc.options.attackKey.isPressed()) return;
        if (!(mc.crosshairTarget instanceof BlockHitResult hit) || hit.getType() != HitResult.Type.BLOCK) return;

        BlockState state = mc.world.getBlockState(hit.getBlockPos());
        if (state.isAir()) return;

        int best = mc.player.getInventory().selectedSlot;
        float bestSpeed = 0;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            float speed = stack.getMiningSpeedMultiplier(state);
            if (speed > bestSpeed) { bestSpeed = speed; best = i; }
        }
        if (best != mc.player.getInventory().selectedSlot) InvUtils.swap(best, false);
    }
}
