#version 150

// Turik sky shader: two-color vertical gradient with a horizon band,
// used by Ambience and the Atmosphere presets (Winter, Autumn, Mars, Saturn).

in vec2 vUv;
in vec4 vColor;

uniform vec3 uTop;
uniform vec3 uBottom;
uniform vec3 uHorizon;
uniform float uHorizonY;   // 0..1 screen-space horizon position
uniform float uHorizonSoft;
uniform float uIntensity;  // 0..1 blend into vanilla sky

out vec4 fragColor;

void main() {
    float t = clamp(vUv.y, 0.0, 1.0);
    vec3 grad = mix(uBottom, uTop, t);
    float band = 1.0 - smoothstep(0.0, uHorizonSoft, abs(t - uHorizonY));
    grad = mix(grad, uHorizon, band * 0.6);
    fragColor = vec4(grad * uIntensity, uIntensity);
}
