package dev.lemonclient.gui.tabs.builtin;

import dev.lemonclient.gui.GuiTheme;
import dev.lemonclient.gui.tabs.Tab;
import dev.lemonclient.gui.tabs.TabScreen;
import dev.lemonclient.gui.tabs.WindowTabScreen;
import dev.lemonclient.gui.widgets.containers.WHorizontalList;
import dev.lemonclient.gui.widgets.containers.WTable;
import dev.lemonclient.gui.widgets.input.WTextBox;
import dev.lemonclient.gui.widgets.pressable.WMinus;
import dev.lemonclient.gui.widgets.pressable.WPlus;
import dev.lemonclient.systems.friends.Friend;
import dev.lemonclient.systems.friends.Friends;
import dev.lemonclient.utils.misc.NbtUtils;
import dev.lemonclient.utils.network.Executor;
import net.minecraft.client.gui.screen.Screen;

public class FriendsTab extends Tab {
    public FriendsTab() {
        super("Friends");
    }

    @Override
    public TabScreen createScreen(GuiTheme theme) {
        return new FriendsScreen(theme, this);
    }

    @Override
    public boolean isScreen(Screen screen) {
        return screen instanceof FriendsScreen;
    }

    private static class FriendsScreen extends WindowTabScreen {
        public FriendsScreen(GuiTheme theme, Tab tab) {
            super(theme, tab);
        }

        @Override
        public void initWidgets() {
            WTable table = add(theme.table()).expandX().minWidth(400).widget();
            initTable(table);

            add(theme.horizontalSeparator()).expandX();

            // New
            WHorizontalList list = add(theme.horizontalList()).expandX().widget();

            WTextBox nameW = list.add(theme.textBox("", (text, c) -> c != ' ')).expandX().widget();
            nameW.setFocused(true);

            WPlus add = list.add(theme.plus()).widget();
            add.action = () -> {
                String name = nameW.get().trim();
                Friend friend = new Friend(name);

                if (Friends.get().add(friend)) {
                    nameW.set("");
                    reload();

                    Executor.execute(() -> {
                        friend.updateInfo();
                        reload();
                    });
                }
            };

            enterAction = add.action;
        }

        private void initTable(WTable table) {
            table.clear();
            if (Friends.get().isEmpty()) return;

            Friends.get().forEach(friend ->
                Executor.execute(() -> {
                    if (friend.headTextureNeedsUpdate()) {
                        friend.updateInfo();
                        reload();
                    }
                })
            );

            for (Friend friend : Friends.get()) {
                table.add(theme.texture(32, 32, friend.getHead().needsRotate() ? 90 : 0, friend.getHead()));
                table.add(theme.label(friend.getName()));

                WMinus remove = table.add(theme.minus()).expandCellX().right().widget();
                remove.action = () -> {
                    Friends.get().remove(friend);
                    reload();
                };

                table.row();
            }
        }

        @Override
        public boolean toClipboard() {
            return NbtUtils.toClipboard(Friends.get());
        }

        @Override
        public boolean fromClipboard() {
            return NbtUtils.fromClipboard(Friends.get());
        }
    }
}
