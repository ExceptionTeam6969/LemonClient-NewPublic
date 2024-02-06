package dev.lemonclient.renderer.text;

import java.awt.Font;

public record FontInfo(String family, Type type) {
    @Override
    public String toString() {
        return family + " " + type;
    }

    public boolean equals(FontInfo info) {
        if (this == info) return true;
        if (info == null || family == null || type == null) return false;
        return family.equals(info.family) && type == info.type;
    }

    public enum Type {
        Regular,
        Bold,
        Italic,
        BoldItalic;

        public static Type fromString(String str) {
            return switch (str) {
                case "Bold" -> Bold;
                case "Italic" -> Italic;
                case "Bold Italic", "BoldItalic" -> BoldItalic;
                default -> Regular;
            };
        }

        public int toJava() {
            return switch (this) {
                case Bold -> Font.BOLD;
                case Italic, BoldItalic -> Font.ITALIC;
                default -> Font.PLAIN;
            };
        }

        @Override
        public String toString() {
            return switch (this) {
                case Bold -> "Bold";
                case Italic -> "Italic";
                case BoldItalic -> "Bold Italic";
                default -> "Regular";
            };
        }
    }
}
