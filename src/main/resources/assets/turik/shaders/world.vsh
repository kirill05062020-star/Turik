#version 150

// Turik world-space shader: tracers, ESP outlines and fills.

in vec3 aPos;
in vec4 aColor;

uniform mat4 uMVP;

out vec4 vColor;

void main() {
    gl_Position = uMVP * vec4(aPos, 1.0);
    vColor = aColor;
}
