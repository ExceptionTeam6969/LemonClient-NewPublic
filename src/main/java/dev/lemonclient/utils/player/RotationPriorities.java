package dev.lemonclient.utils.player;

import dev.lemonclient.systems.modules.Modules;
import dev.lemonclient.systems.modules.combat.*;
import dev.lemonclient.systems.modules.movement.AntiAFK;
import dev.lemonclient.systems.modules.movement.Scaffold;
import dev.lemonclient.systems.modules.player.AutoMine;
import dev.lemonclient.systems.modules.settings.RotationPrioritySettings;

public class RotationPriorities {
    // Rotate优先级
    public static int get(Object module) {
        RotationPrioritySettings priority = Modules.get().get(RotationPrioritySettings.class);

        if (priority != null) {
            if (module instanceof AutoAnchor) return priority.autoAnchor.get();
            if (module instanceof AntiAim) return priority.antiAim.get();
            if (module instanceof AntiAFK) return priority.antiAFK.get();
            if (module instanceof AutoCrystal) return priority.autoCrystal.get();
            if (module instanceof AutoCrystalPlus) return priority.autoCrystalPlus.get();
            if (module instanceof AutoHoleFill) return priority.autoHoleFill.get();
            if (module instanceof AutoHoleFillPlus) return priority.autoHoleFillPlus.get();
            if (module instanceof AutoPearlClip) return priority.autoPearlClip.get();
            if (module instanceof AutoTrap) return priority.autoTrap.get();
            if (module instanceof AutoMine) return priority.autoMine.get();
            if (module instanceof AutoWeb) return priority.autoWeb.get();
            if (module instanceof BedBombV4) return priority.bedBombV4.get();
            if (module instanceof CevBreaker) return priority.cevBreaker.get();
            if (module instanceof KillAura) return priority.killAura.get();
            if (module instanceof PistonCrystal) return priority.pistonCrystal.get();
            if (module instanceof SelfWeb) return priority.selfWeb.get();
            if (module instanceof Scaffold) return priority.scaffold.get();
            if (module instanceof SelfTrap) return priority.selfTrap.get();
            if (module instanceof SurroundPlus) return priority.surroundPlus.get();
        }

        return 100;
    }
}
