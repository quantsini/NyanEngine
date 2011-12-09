#version 120 //#version 330
precision highp float;

in vec3 ecNormal, ecPos;

void main(void) {
	vec3 N = normalize(ecNormal);
	gl_FragColor = vec4(N.x, N.y, N.z, 1.0);
}