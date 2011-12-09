#version 120

uniform float totStrength = 1.38;
uniform float strength = 0.07;
uniform float offset = 18.0;
uniform float falloff = 0.000002;
uniform float rad = 0.006;

uniform float time;

uniform sampler2D normalDepthMap;
uniform sampler2D depthMap;
varying vec2 uv;
#define SAMPLES 16 // 10 is good
const float invSamples = 1.0/16.0;

float LinearizeDepth(vec2 uv)
{
  float n = 0.01; // camera z near
  float f = 1000.0; // camera z far
  float z = texture2D(depthMap, uv).x;
  //return z;
  return (2.0 * n) / (f + n - z * (f - n));
}
float rand(vec2 co){
    return fract(sin(dot(co.xy ,vec2(12.9898,78.233))) * 43758.5453);
}

vec3 randVec3(float seed) {
	float v1 = rand(vec2(time,seed));
	float v2 = rand(vec2(time,v1));
	float v3 = rand(vec2(time,v2));
	
	return vec3(v1, v2, v3);
}
