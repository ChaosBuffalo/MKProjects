#version 150

#moj_import <fog.glsl>

uniform sampler2D Sampler0;

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;

in float vertexDistance;
in vec2 texCoord0;
in vec4 vertexColor;

out vec4 fragColor;

uniform float weight[5] = float[] (0.227027, 0.1945946, 0.1216216, 0.054054, 0.016216);
uniform float offsets[5] = float[] (0, 0.25, 0.5, 0.75, 1.0);

vec4 blur5(sampler2D image, vec2 uv, float mipLevel) {
    vec2 tex_offset = 1.0 / textureSize(image, 0); // gets size of single texel
    vec4 result = texture(image, uv).rgba * weight[0]; // current fragment's contribution
    for(int i = 1; i < 5; ++i)
    {
        result += texture(image, uv + vec2(tex_offset.x * offsets[i], 0.0), mipLevel).rgba * weight[i];
        result += texture(image, uv - vec2(tex_offset.x * offsets[i], 0.0), mipLevel).rgba * weight[i];
        //        result += texture(image, uv + vec2(tex_offset.x * offsets[i], tex_offset.y * offsets[i]), mipLevel).rgba * weight[i];
        //        result += texture(image, uv - vec2(tex_offset.x * offsets[i], tex_offset.y * offsets[i]), mipLevel).rgba * weight[i];
        result += texture(image, uv + vec2(0.0, tex_offset.y * offsets[i]), mipLevel).rgba * weight[i];
        result += texture(image, uv - vec2(0.0, tex_offset.y * offsets[i]), mipLevel).rgba * weight[i];
        //        result += texture(image, uv + vec2(tex_offset.x * offsets[i], -tex_offset.y * offsets[i]), mipLevel).rgba * weight[i];
        //        result += texture(image, uv + vec2(-tex_offset.x * offsets[i], tex_offset.y * offsets[i]), mipLevel).rgba * weight[i];
    }
    return result;
}


void main() {
    vec4 color = blur5(Sampler0, texCoord0, 1.0) * vertexColor * ColorModulator;
    if (color.a < 0.001) {
        discard;
    }
    fragColor = linear_fog(color, vertexDistance, FogStart, FogEnd, FogColor);
}