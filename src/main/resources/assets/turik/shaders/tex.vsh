#version 150

// Turik textured quad shader: font glyphs and blur-composited panels.

in vec2 aPos;
in vec2 aUv;
in vec4 aColor;

uniform vec2 uView;

out vec2 vUv;
out vec4 vColor;

void main() {
    gl_Position = vec4(aPos.x / uView.x * 2.0 - 1.0, 1.0 - aPos.y / uView.y * 2.0, 0.0, 1.0);
    vUv = aUv;
    vColor = aColor;
}
