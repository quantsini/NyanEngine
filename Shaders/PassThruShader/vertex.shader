#version 330

// inputs - normals, texture coordinates
in vec3 in_vertex;

// input uniforms used by this shader
uniform mat4 projMatrix;
uniform mat4 modelViewMatrix;

// main program
void main(void)
{
	gl_Position = projMatrix * modelViewMatrix * vec4(in_vertex, 1.0);
}