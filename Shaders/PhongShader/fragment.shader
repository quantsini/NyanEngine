#version 120 //#version 330
precision highp float;

in vec3 ecNormal, ecPos;
//out varying vec4 fragColor;

in vec2 texCoord;

//uniform vec4 l_pos;
uniform mat4 projMatrix;
uniform mat4 modelViewMatrix;
uniform float ambientOnly;
uniform sampler2D diffuseMap;
uniform sampler2D normalMap;
uniform sampler2D specularMap;
uniform sampler2D bumpMap;
uniform float time;
uniform sampler2D depthMap;
uniform sampler2D normalDepthMap;


/*
uniform vec4 lightDiffuse;
uniform vec4 lightAmbient;
uniform vec4 lightSpecular;

uniform vec4 materialSpecular;
uniform float materialShininess;
uniform vec4 materialDiffuse;
uniform vec4 materialAmbient;
uniform float attenuationConstant;
uniform float attenuationLinear;
uniform float attenuationQuadratic;
*/





float average(vec4 inp) {
	return (inp.r + inp.g + inp.b + inp.a)/4.0;
}


vec4 phongShade() {

	vec4 l_pos = gl_LightSource[0].position;

	vec4 lightDiffuse = gl_LightSource[0].diffuse;
	vec4 lightAmbient =  gl_LightSource[0].ambient;
	vec4 lightSpecular = gl_LightSource[0].specular;
	
	vec4 materialSpecular = gl_FrontMaterial.specular;
	float materialShininess = gl_FrontMaterial.shininess;
	vec4 materialDiffuse =  texture2D(diffuseMap, texCoord);
	vec4 materialAmbient =  gl_FrontMaterial.ambient;
	float specularModifier = average(texture2D(diffuseMap, texCoord))-0.2;


	//gl_LightSource[0].position
	vec3 R;
	vec3 N;
	vec3 V;
	vec3 L;
	vec3 lightDir;
	vec4 diffuse;
	vec4 diffuseComponent;
	vec4 specularComponent;
	vec4 ambientComponent;
	vec4 color = vec4(0.0,0.0,0.0,0.0);
	
	vec3 Rvector;
	vec3 viewVector;
	float NdotL;
	float RdotV;
	
	viewVector = -ecPos;
	
	lightDir = vec3(l_pos) - ecPos;
	
	N = normalize(ecNormal);
	V = normalize(viewVector);
	L = normalize(lightDir);
	

	ambientComponent = materialDiffuse * lightAmbient;
	NdotL = max(dot(N,L),0.0);

	diffuseComponent = vec4(0.0,0.0,0.0,0.0);
	specularComponent = vec4(0.0,0.0,0.0,0.0);
		if (NdotL > 0.0) {
				// compute diffuse
				diffuseComponent = NdotL * materialDiffuse * lightDiffuse;
				
				// compute specular
				Rvector = 2.0*(dot(L, N))*N-L;
				R = normalize(Rvector);
				RdotV = max(dot(R,V),0.0);
				specularComponent = materialSpecular *
						lightSpecular *
						pow(RdotV, materialShininess) * specularModifier;
				
		}

	// compute attenuation
	float dist = length(lightDir);
	float luminosity = 1.0/(0.2 + 0.010*dist + 0.0001*dist*dist);
	color = luminosity*(diffuseComponent + specularComponent);
	color.a = 1.0;
	
	// interpolate the colors
	color = (1-ambientComponent)*color+ambientComponent;
	
	if (ambientOnly == 1) {
		color = ambientComponent;
	}
	if (ambientOnly == 2) {
		color = luminosity*specularComponent;
	}
	if (ambientOnly == 3) {
		color = luminosity*diffuseComponent;
	}
	return color;
}
void main(void)
{
	gl_FragColor = phongShade();
}

