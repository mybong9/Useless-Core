#version 150
uniform sampler2D Sampler0;
uniform vec2 Size;
uniform vec2 FramebufferSize;
uniform float Radius;
uniform float Border;
uniform float Softness;
uniform float Glass;
uniform vec4 TopColor;
uniform vec4 BottomColor;
uniform vec4 EdgeColor;
uniform vec4 ShadowColor;
in vec2 localPos;
out vec4 fragColor;
float distanceToBox(vec2 p) {
    vec2 q = abs(p - Size * 0.5) - Size * 0.5 + Radius;
    return length(max(q, 0.0)) + min(max(q.x, q.y), 0.0) - Radius;
}
void main() {
    float d = distanceToBox(localPos);
    float aa = max(fwidth(d), 0.005);
    float coverage = 1.0 - smoothstep(-aa * 0.5, aa * 0.5, d);
    vec4 fill = mix(TopColor, BottomColor, clamp(localPos.y / Size.y, 0.0, 1.0));
    if (Glass > 0.5) {
        vec3 scene = texture(Sampler0, gl_FragCoord.xy / FramebufferSize).rgb;
        fill = vec4(mix(scene, fill.rgb, fill.a), 1.0);
    }
    float edge = Border > 0.0 ? smoothstep(-Border - aa * 0.5, -Border + aa * 0.5, d) : 0.0;
    float ea = EdgeColor.a * edge;
    vec4 surface = vec4(mix(fill.rgb, EdgeColor.rgb, ea), fill.a + (1.0 - fill.a) * ea);
    float shadowDistance = max(distanceToBox(localPos - vec2(0.0, Softness * 0.3)), 0.0);
    float shadowAlpha = Softness > 0.0 ? ShadowColor.a * exp(-shadowDistance * shadowDistance / max(0.01, Softness * Softness)) : 0.0;
    float a = surface.a * coverage + shadowAlpha * (1.0 - coverage);
    if (a < 0.002) discard;
    fragColor = vec4((surface.rgb * surface.a * coverage + ShadowColor.rgb * shadowAlpha * (1.0 - coverage)) / a, a);
}
