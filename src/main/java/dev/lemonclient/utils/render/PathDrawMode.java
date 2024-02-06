package dev.lemonclient.utils.render;

public enum PathDrawMode {
    Line,
    Box,
    CircleLine;

    @Override
    public String toString() {
        if (this == CircleLine)
            return "Circle&Line";
        return super.toString();
    }
}
