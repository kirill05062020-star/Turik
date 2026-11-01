#version 150

in vec2 aPos;
in vec4 aColor;
in vec4 aAux1;
in vec4 aAux2;

uniform vec2 uView; // size of the viewport in GUI pixels

out vec2 vPos;
out vec4 vColor;
out vec4 vAux1;
out vec4 vAux2;

void main() {
    gl_Position = vec4(aPos.x / uView.x * 2.0 - 1.0, 1.0 - aPos.y / uView.y * 2.0, 0.0, 1.0);
    vPos = aPos;
    vColor = aColor;
    vAux1 = aAux1;
    vAux2 = aAux2;
}
