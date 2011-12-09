#version 330
precision highp float;

out vec4 outFragColor;

uniform sampler2D sceneMap;
uniform sampler2D normalDepthMap;
uniform sampler2D depthMap;
uniform sampler2D blurMap;
uniform float blend = 0.5;
uniform vec2 resolution = vec2(800.0, 600.0);
uniform float time;

float LinearizeDepth(vec2 uv)
{
  float n = 0.01; // camera z near
  float f = 1000.0; // camera z far
  float z = texture2D(depthMap, uv).x;
  //return z;
  return (2.0 * n) / (f + n - z * (f - n));
}

vec4 passThru(sampler2D map, vec2 uv) {
	return texture2D(map, uv);
}

void main(void) {
	vec2 uv = gl_FragCoord.xy / resolution.xy;
	outFragColor = 1-(1-((1)*passThru(sceneMap, uv)))*(1-((blend)*passThru(depthMap, uv)));
	//outFragColor = passThru(sceneMap, uv);
}