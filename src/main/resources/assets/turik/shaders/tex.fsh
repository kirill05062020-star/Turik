#version 150

in vec2 vUv;
in vec4 vColor;

uniform sampler2D uTexture;

out vec4 fragColor;

void main() {
    vec4 tex = texture(uTexture, vUv);
    float a = tex.a * vColor.a;
    if (a <= 0.002) {
        discard;
    }
    fragColor = vec4(vColor.rgb * tex.a * vColor.a, a); // premultiplied
}
