package dev.lemonclient.systems.modules.render;

import dev.lemonclient.MixinPlugin;
import dev.lemonclient.events.world.ChunkOcclusionEvent;
import dev.lemonclient.gui.GuiTheme;
import dev.lemonclient.gui.widgets.WWidget;
import dev.lemonclient.settings.*;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.irisshaders.iris.api.v0.IrisApi;
import net.minecraft.block.Block;

import java.util.List;

public class WallHack extends Module {
    public WallHack() {
        super(Categories.Render, "Wall Hack", "Makes blocks translucent.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    public final Setting<Integer> opacity = sgGeneral.add(new IntSetting.Builder()
        .name("opacity")
        .description("The opacity for rendered blocks.")
        .defaultValue(0)
        .range(0, 255)
        .sliderMax(255)
        .onChanged(onChanged -> {
            if (this.isActive()) {
                mc.worldRenderer.reload();
            }
        })
        .build()
    );

    public final Setting<List<Block>> blocks = sgGeneral.add(new BlockListSetting.Builder()
        .name("blocks")
        .description("What blocks should be targeted for Wall Hack.")
        .defaultValue()
        .onChanged(onChanged -> {
            if (isActive()) mc.worldRenderer.reload();
        })
        .build()
    );

    public final Setting<Boolean> occludeChunks = sgGeneral.add(new BoolSetting.Builder()
        .name("occlude-chunks")
        .description("Whether caves should occlude underground (may look wonky when on).")
        .defaultValue(false)
        .build()
    );

    @Override
    public void onActivate() {
        mc.worldRenderer.reload();
    }

    @Override
    public void onDeactivate() {
        mc.worldRenderer.reload();
    }

    @Override
    public WWidget getWidget(GuiTheme theme) {
        if (MixinPlugin.isSodiumPresent)
            return theme.label("Warning: Due to Sodium in use, opacity is overridden to 0.");
        if (MixinPlugin.isIrisPresent && IrisApi.getInstance().isShaderPackInUse())
            return theme.label("Warning: Due to shaders in use, opacity is overridden to 0.");

        return null;
    }

    @EventHandler
    private void onChunkOcclusion(ChunkOcclusionEvent event) {
        if (!occludeChunks.get()) event.cancel();
    }
}
