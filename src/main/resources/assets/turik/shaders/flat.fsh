#version 150

// Turik flat shape shader: plain rects, rounded rects (SDF), gradients and
// SDF outline rings (for bordered controls).

in vec2 vPos;
in vec4 vColor;
in vec4 vAux1; // halfW, halfH, cornerRadius, outlineThickness
in vec4 vAux2; // centerX, centerY, unused, unused

out vec4 fragColor;

void main() {
    vec4 c = vColor;
    float radius = vAux1.z;
    float outline = vAux1.w;
    if (radius > 0.0 || outline > 0.0) {
        vec2 halfSize = vec2(vAux1.x, vAux1.y);
        vec2 center = vAux2.xy;
        vec2 d = abs(vPos - center) - halfSize + vec2(max(radius, 0.0));
        float dist = length(max(d, 0.0)) + min(max(d.x, d.y), 0.0) - max(radius, 0.0);
        float mask;
        if (outline > 0.0) {
            mask = 1.0 - smoothstep(-0.5, 0.5, abs(dist) - outline);
        } else {
            mask = 1.0 - smoothstep(-0.5, 0.5, dist);
        }
        c.a *= mask;
    }
    if (c.a <= 0.002) {
        discard;
    }
    fragColor = vec4(c.rgb * c.a, c.a); // premultiplied output
}
