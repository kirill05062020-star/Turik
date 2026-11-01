#version 150

in vec4 vColor;

out vec4 fragColor;

void main() {
    vec4 c = vColor;
    if (c.a <= 0.002) {
        discard;
    }
    fragColor = vec4(c.rgb * c.a, c.a); // premultiplied
}
