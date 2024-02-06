package dev.lemonclient.settings;

import dev.lemonclient.gui.GuiTheme;
import dev.lemonclient.gui.WidgetScreen;
import dev.lemonclient.utils.misc.IChangeable;
import dev.lemonclient.utils.misc.ICopyable;
import dev.lemonclient.utils.misc.ISerializable;
import net.minecraft.block.Block;

public interface IBlockData<T extends ICopyable<T> & ISerializable<T> & IChangeable & IBlockData<T>> {
    WidgetScreen createScreen(GuiTheme theme, Block block, BlockDataSetting<T> setting);
}
