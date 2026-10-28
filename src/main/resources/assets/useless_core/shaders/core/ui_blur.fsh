#version 150
uniform sampler2D Sampler0;
uniform vec2 Step;
in vec2 texCoord;
out vec4 fragColor;
void main() {
    vec3 col = texture(Sampler0, texCoord).rgb * 0.2270270270;
    col += texture(Sampler0, clamp(texCoord + Step * 1.3846153846, 0.0, 1.0)).rgb * 0.3162162162;
    col += texture(Sampler0, clamp(texCoord - Step * 1.3846153846, 0.0, 1.0)).rgb * 0.3162162162;
    col += texture(Sampler0, clamp(texCoord + Step * 3.2307692308, 0.0, 1.0)).rgb * 0.0702702703;
    col += texture(Sampler0, clamp(texCoord - Step * 3.2307692308, 0.0, 1.0)).rgb * 0.0702702703;
    fragColor = vec4(col, 1.0);
}
