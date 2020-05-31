#version 120

varying float x_coord;
varying float y_coord;
varying float z_coord;

uniform int fog_mode;
uniform float deep;
uniform float fog_density;
uniform vec2 center;
uniform vec2 tex_size;
uniform vec3 main_color;
uniform vec3 fog_color;
uniform float offset;
uniform float alpha;
uniform float fog_smooth;

uniform sampler2D DiffuseSampler;

const float pi = 3.14159;
const vec3 lightColor = vec3(1.0, 1.0, 1.0);

float calculate_fog() {
    float f;
    float distance = x_coord * x_coord + y_coord * y_coord + z_coord * z_coord;
    distance = sqrt(distance);
    if (fog_mode == 0) {f = exp(-(distance * fog_density));}
    else { f = distance * fog_density; f = exp(-(f * f)); }
    f = clamp(f, 0, 1);
    return 1 - f;
}

vec4 cubic(float v) {
    vec4 n = vec4(1.0, 2.0, 3.0, 4.0) - v;
    vec4 s = n * n * n;
    float x = s.x;
    float y = s.y - 4.0 * s.x;
    float z = s.z - 4.0 * s.y + 6.0 * s.x;
    float w = 6.0 - x - y - z;
    return vec4(x, y, z, w) * (1.0/6.0);
}

#if __VERSION__ < 130
#define TEXTURE2D texture2D
#else
#define TEXTURE2D texture
#endif

vec4 textureBicubic(sampler2D sampler, vec2 texCoords) {

    vec2 invTexSize = 1.0 / tex_size;

    texCoords = texCoords * tex_size - 0.5;

    vec2 fxy = fract(texCoords);
    texCoords -= fxy;

    vec4 xcubic = cubic(fxy.x);
    vec4 ycubic = cubic(fxy.y);

    vec4 c = texCoords.xxyy + vec2(-0.5, 1.5).xyxy;

    vec4 s = vec4(xcubic.xz + xcubic.yw, ycubic.xz + ycubic.yw);
    vec4 off = c + vec4(xcubic.yw, ycubic.yw) / s;

    off *= invTexSize.xxyy;

    vec4 sample0 = TEXTURE2D(sampler, off.xz);
    vec4 sample1 = TEXTURE2D(sampler, off.yz);
    vec4 sample2 = TEXTURE2D(sampler, off.xw);
    vec4 sample3 = TEXTURE2D(sampler, off.yw);

    //sample0.g = sample0.g * sample0.g;
    //sample1.g = sample1.g * sample1.g;
    //sample2.g = sample2.g * sample2.g;
    //sample3.g = sample3.g * sample3.g;

    float sx = s.x/(s.x + s.y);
    float sy = s.z/(s.z + s.w);

    if(fog_smooth == 1) {
        // cosin
        //sx = -cos(sx * pi)/2 + 0.5;
        //sy = -cos(sy * pi)/2 + 0.5;
    
        // quntic
        sx = sx * sx * sx * (sx * (sx * 6 - 15) + 10);
        sy = sy * sy * sy * (sy * (sy * 6 - 15) + 10);
    }

    sample0 = mix(sample1, sample0, sx);
    sample2 = mix(sample3, sample2, sx);

    return mix(sample2, sample0, sy);
}

void main() {
    vec2 texcoord = vec2(gl_TexCoord[0]);
    vec4 mask = textureBicubic(DiffuseSampler, texcoord);

    vec3 color = main_color * (deep < 0 ? mask.b : mask.r) + offset;

    float m = mix(mask.a, mask.g, clamp(deep, 0, 1));
    color = mix(color, lightColor, m);

    float f = calculate_fog();
    color = mix(color, fog_color, f);

    float d = (texcoord.x - center.x) * (texcoord.x - center.x) + (texcoord.y - center.y) * (texcoord.y - center.y);
    d = clamp(d * 5, 0, 1);
    d = 1 - clamp(d * 2 - 1, 0, 1);

    gl_FragColor = vec4(color, alpha * d);
}