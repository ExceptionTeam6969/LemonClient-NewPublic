#version 150

uniform vec4 color;

out vec4 fragColor;

void main() {
    fragColor = vec4(color.rgba);
}

