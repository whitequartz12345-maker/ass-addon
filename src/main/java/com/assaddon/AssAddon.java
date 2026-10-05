package com.assaddon;

import com.assaddon.modules.*;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;

public class AssAddon extends MeteorAddon {
    public static final Category CATEGORY = new Category("Ass");

    @Override
    public void onInitialize() {
        Modules m = Modules.get();
        m.add(new SusChunkFinder());
        m.add(new SusChunkV2());
        m.add(new StarChunkFinder());
        m.add(new AssFreecam());
        m.add(new DeepBlockNoti());
        m.add(new BaseEsp());
        m.add(new AssTool());
    }

    @Override
    public void onRegisterCategories() {
        Modules.registerCategory(CATEGORY);
    }

    @Override
    public String getPackage() {
        return "com.assaddon";
    }
}
