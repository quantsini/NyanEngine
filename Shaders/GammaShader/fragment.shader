#version 330
precision highp float;

out vec4 outFragColor;

uniform sampler2D sceneMap;
uniform sampler2D normalDepthMap;
uniform sampler2D depthMap;
uniform vec2 resolution = vec2(800.0, 600.0);
uniform float time;
uniform float gamma = 1;

vec4 passThru(vec2 uv) {
	return texture2D(sceneMap, uv);
}

void main(void) {
	vec2 uv = gl_FragCoord.xy / resolution.xy;
	vec4 color = pow(passThru(uv), vec4(1.0/ gamma));
	color.a = 1;
    outFragColor = color;
}