#version 330
precision highp float;

// square kernel size
#define KERNEL_SIZE 3

out vec4 outFragColor;


uniform sampler2D sceneMap;
uniform sampler2D normalDepthMap;
uniform sampler2D depthMap;
uniform vec2 resolution = vec2(800.0, 600.0);
uniform float time;
uniform float kernel[KERNEL_SIZE*KERNEL_SIZE] = float[KERNEL_SIZE*KERNEL_SIZE]
(
0.05, 0.09, 0.12,
0.15, 0.16, 0.15,
0.12, 0.09, 0.05
);

uniform float blurSize = 3.0;

float LinearizeDepth(vec2 uv)
{
  float n = 0.01; // camera z near
  float f = 1000.0; // camera z far
  float z = texture2D(depthMap, uv).x;
  //return z;
  return (2.0 * n) / (f + n - z * (f - n));
}

vec4 passThru(vec2 uv) {
	return texture2D(sceneMap, uv);
}

void main(void) {
	vec2 uv = gl_FragCoord.xy / resolution.xy;
	float step_w = 1.0/resolution.x;
	float step_h = 1.0/resolution.y;
    int i = 0;
    int j = 0;
    int offsetX;
    int offsetY;
    vec4 color = vec4(0.0, 0.0, 0.0, 0.0);

	float k[] = kernel;
  	for( i = 0; i < KERNEL_SIZE; i++ ) {
  		for ( j = 0; j < KERNEL_SIZE; j++ ) {
  			vec2 offset = blurSize * vec2((i - KERNEL_SIZE/2)*1/resolution.x, (j - KERNEL_SIZE/2)*1/resolution.y);
  			color += passThru(uv + offset) * k[i + j];
  		}
  	}
   
   color.a = 1.0;
   outFragColor = color;

}