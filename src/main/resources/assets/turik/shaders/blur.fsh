#version 150

// Turik separable gaussian blur. Run twice: horizontal, then vertical.

in vec2 vUv;
in vec4 vColor;

uniform sampler2D uTexture;
uniform vec2 uDirection; // (1/w, 0) or (0, 1/h)

out vec4 fragColor;

void main() {
    float weights[5] = float[](0.227027, 0.1945946, 0.1216216, 0.054054, 0.016216);
    vec3 result = texture(uTexture, vUv).rgb * weights[0];
    for (int i = 1; i < 5; i++) {
        vec2 off = uDirection * float(i);
        result += texture(uTexture, vUv + off).rgb * weights[i];
        result += texture(uTexture, vUv - off).rgb * weights[i];
    }
    fragColor = vec4(result, 1.0);
}
