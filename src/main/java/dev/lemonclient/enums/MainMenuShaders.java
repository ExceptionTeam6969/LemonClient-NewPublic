package dev.lemonclient.enums;

public enum MainMenuShaders {
    Rise(0),
    Liquidbounce(1),
    Kevin(2),
    Both(3),
    None(4);

    public final int type;

    MainMenuShaders(int type) {
        this.type = type;
    }

    public static MainMenuShaders get(int type) {
        for (MainMenuShaders v : values()) {
            if (v.type == type) {
                return v;
            }
        }
        return MainMenuShaders.None;
    }
}
