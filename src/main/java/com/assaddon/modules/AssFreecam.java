package com.assaddon.modules;

import com.assaddon.AssAddon;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.Freecam;

/** Thin wrapper: enables Meteor's built-in Freecam with your preferred speed. */
public class AssFreecam extends Module {
    private final SettingGroup sg = settings.getDefaultGroup();
    private final Setting<Double> speed = sg.add(new DoubleSetting.Builder()
        .name("speed").description("Camera speed.").defaultValue(1.5).min(0.1).sliderMax(10).build());

    public AssFreecam() {
        super(AssAddon.CATEGORY, "ass-freecam", "Move your camera freely (uses Meteor's Freecam).");
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onActivate() {
        Freecam fc = Modules.get().get(Freecam.class);
        Setting<Double> s = (Setting<Double>) fc.settings.get("speed");
        if (s != null) s.set(speed.get());
        if (!fc.isActive()) fc.toggle();
    }

    @Override
    public void onDeactivate() {
        Freecam fc = Modules.get().get(Freecam.class);
        if (fc.isActive()) fc.toggle();
    }
}
