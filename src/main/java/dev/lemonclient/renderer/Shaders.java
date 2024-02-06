package dev.lemonclient.renderer;

import dev.lemonclient.utils.PreInit;

public class Shaders {
    private static boolean init;
    public static Shader POS_COLOR;
    public static Shader POS_TEX_COLOR;
    public static Shader TEXT;

    // core shaders
    public static Shader RECT;
    //public static Shader GRADIENT_GLOW_ROUND;
    //public static Shader GRADIENT_ROUND;
    //public static Shader ROUND_RECT;
    public static Shader TEXTURE_RECT;
    public static Shader MAIN_MENU;

    public static void initCore() {
        RECT = new Shader(ShaderSources.POSITION_COLOR_SOURCE, rect, true, false, true);
        //GRADIENT_GLOW_ROUND = new Shader(ShaderSources.POSITION_COLOR_SOURCE,gradient_glow_round,true,false,true);
        //GRADIENT_ROUND = new Shader(ShaderSources.POSITION_COLOR_SOURCE,gradient_round,true,false,true);
        // = new Shader(ShaderSources.POSITION_COLOR_SOURCE,ShaderSources.ROUND_RECT_SOURCE,true,false,false);
        MAIN_MENU = new Shader(ShaderSources.POSITION_COLOR_SOURCE, ShaderSources.MAIN_MENU_SOURCE, true, false, false);

        TEXTURE_RECT = new Shader(ShaderSources.POSITION_COLOR_TEX_SOURCE, texture_rect, true, false, true);
    }

    @PreInit
    public static void init() {
        if (!init) {
            POS_COLOR = new Shader("pos_color.vert", "pos_color.frag");
            POS_TEX_COLOR = new Shader("pos_tex_color.vert", "pos_tex_color.frag");
            TEXT = new Shader("text.vert", "text.frag");

            initCore();

            init = true;
        }
    }

    public static Shader posColor() {
        init();
        return POS_COLOR;
    }

    public static Shader posTexColor() {
        init();
        return POS_TEX_COLOR;
    }

    public static Shader text() {
        init();
        return TEXT;
    }

    private static final String path = "mesh/";

    private static final String rect = path + "rect.frag";
    private static final String gradient_glow_round = path + "gradient_glow_round.frag";
    private static final String gradient_round = path + "gradient_round.frag";
    private static final String round_rect = path + "round_rect.frag";
    private static final String texture_rect = path + "texture_rect.frag";

}
