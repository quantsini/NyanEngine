#version 330

// inputs - normals, texture coordinates
in vec3 in_vertex;
in vec3 in_normal;

// outputs to the geometry shader - eye coordinate normal, and eye coordinate position
out vec3 ecNormal;

// input uniforms used by this shader
uniform mat4 projMatrix;
uniform mat4 modelViewMatrix;

// computes the normal matrix to transform normals to eye coordinates
mat3 computeNormalMatrix() {
	//return gl_NormalMatrix;
	return mat3(transpose(inverse(modelViewMatrix)));
}

// main program
void main(void)
{	
	ecNormal = normalize(computeNormalMatrix()*in_normal);
	gl_Position = projMatrix * modelViewMatrix * vec4(in_vertex, 1.0);
}