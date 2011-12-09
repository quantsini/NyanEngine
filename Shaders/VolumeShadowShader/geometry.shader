#version 120  
#extension GL_EXT_geometry_shader4: enable   

//layout(triangles_adjacency) in;
//layout(triangle_strip, max_vertices = 1024) out;

uniform vec4 light_pos;
uniform mat4 lightModelViewMatrix;
uniform mat4 projMatrix;
uniform mat4 modelViewMatrix;

// robust = 1 means we keep the triangle if it doesn't face the light
const int robust = 0;

// if we're using carmack's algorithm or not (zpass=0 means carmacks)
const int zpass = 1;

void main()  
{
	// light position in eye space
	vec4 l_pos = gl_LightSource[0].position; //lightModelViewMatrix * light_pos;
    
    // vector holding the normal
   	vec3 ns[3];
   	
   	// directions toward light  
  	vec3 d[3];
  	
  	//temp vertices
    vec4 v[4];  
    
    // triangle oriented towards light (counter-clock-wise rendering)
  	vec4 or_pos[3]; 
  	
  	// set the first triangle to be this triangle
   	or_pos[0] = gl_PositionIn[0];
    or_pos[1] = gl_PositionIn[2];
    or_pos[2] = gl_PositionIn[4];
   
    // compute normal at each vertex.  
    ns[0] = cross(gl_PositionIn[2].xyz - gl_PositionIn[0].xyz, gl_PositionIn[4].xyz - gl_PositionIn[0].xyz );  
    ns[1] = cross(gl_PositionIn[4].xyz - gl_PositionIn[2].xyz, gl_PositionIn[0].xyz - gl_PositionIn[2].xyz );  
    ns[2] = cross(gl_PositionIn[0].xyz - gl_PositionIn[4].xyz, gl_PositionIn[2].xyz - gl_PositionIn[4].xyz );  
     
    // Compute direction from vertices to light.  
    d[0] = l_pos.xyz-l_pos.w*gl_PositionIn[0].xyz;  
    d[1] = l_pos.xyz-l_pos.w*gl_PositionIn[2].xyz;  
    d[2] = l_pos.xyz-l_pos.w*gl_PositionIn[4].xyz;  
   
    // compute the triangle face normal
    vec3 triangleNorm = (ns[0]+ns[1]+ns[2])/3.0;
    
    // compute the triangle position
    vec3 trianglePos = (gl_PositionIn[0].xyz + gl_PositionIn[2].xyz + gl_PositionIn[4].xyz)/3.0;
    
    // compute the distance from light to triangle
    vec3 dLightToTriangle = l_pos.xyz - l_pos.w * trianglePos;
    
    // normalize the vector from light to triangle
    vec3 L = normalize(dLightToTriangle);
    
    // normalize the triangle normal
    vec3 N = normalize(triangleNorm);
    
    if (length(dLightToTriangle) > 150) {
    	return;
    }
    // compute the dot product between normal and light, and check if it faces light
    float NdotL = dot(N,L);
    
    // check if the main triangle faces the light.  
    bool faces_light = true; 
    
    // increasing this value will yield more triangles
    float facingLightThreshold = 0.0;
    
    if ( !(NdotL > facingLightThreshold) ) {  
     	// not facing the light and not robust, ignore.  
   		if ( robust == 0 ) return; 
    
    	
     	// since it doesn't face the light, flip the order so it does face the light
    	or_pos[1] = gl_PositionIn[4];  
     	or_pos[2] = gl_PositionIn[2];  
     	faces_light = false;  
  	}  
  

  	// render the caps, this is needed if we're using carmack's reverse
   	if ( zpass == 0 ) {  
		// near cap: pass thru   
    	color = vec4(1.0, 0.0, 0.0, 1.0);
     	gl_Position = projMatrix*or_pos[0];  
     	EmitVertex();  
     	gl_Position = projMatrix*or_pos[1];  
     	EmitVertex();  
     	gl_Position = projMatrix*or_pos[2];  
     	EmitVertex();
     	EndPrimitive(); 
     
     	// far cap: extrude positions to infinity. 
     	v[0] =vec4(l_pos.w*or_pos[0].xyz-l_pos.xyz,0);  
     	v[1] =vec4(l_pos.w*or_pos[2].xyz-l_pos.xyz,0);  
     	v[2] =vec4(l_pos.w*or_pos[1].xyz-l_pos.xyz,0);  
     	gl_Position = projMatrix*v[0];  
     	EmitVertex();  
     	gl_Position = projMatrix*v[1];  
     	EmitVertex();  
     	gl_Position = projMatrix*v[2];  
     	EmitVertex();
     	EndPrimitive();  
  	}  
 
  	// loop over all edges and extrude if needed
  	for ( int i=0; i<3; i++ ) {  
    	// Compute indices of neighbor triangles 
   		int v0 = i*2;  
     	int nb = (i*2+1);  
     	int v1 = (i*2+2) % 6;  
     
     	// compute normals again normals again
     	ns[0] = cross(gl_PositionIn[nb].xyz-gl_PositionIn[v0].xyz, gl_PositionIn[v1].xyz-gl_PositionIn[v0].xyz);  
     	ns[1] = cross(gl_PositionIn[v1].xyz-gl_PositionIn[nb].xyz, gl_PositionIn[v0].xyz-gl_PositionIn[nb].xyz);  
     	ns[2] = cross(gl_PositionIn[v0].xyz-gl_PositionIn[v1].xyz, gl_PositionIn[nb].xyz-gl_PositionIn[v1].xyz);  
       
     	// compute direction to light, again as above.  
     	d[0] =l_pos.xyz-l_pos.w*gl_PositionIn[v0].xyz;  
     	d[1] =l_pos.xyz-l_pos.w*gl_PositionIn[nb].xyz;  
     	d[2] =l_pos.xyz-l_pos.w*gl_PositionIn[v1].xyz;  
     
      	// compute the triangle normal again
    	triangleNorm = (ns[0] + ns[1] + ns[2])/3.0;
    	
    	// compute triangle center position
    	trianglePos = (gl_PositionIn[v0].xyz+gl_PositionIn[nb].xyz+gl_PositionIn[v1].xyz)/3.0;
    	
    	// compute distance from light to triangle again
    	dLightToTriangle = l_pos.xyz-l_pos.w*trianglePos;
    	
    	// normalize the normal and distance
    	N = normalize(triangleNorm);
    	L = normalize(dLightToTriangle);
    	
    	// see if this has a neighbor that doesn't face the light
    	NdotL = dot(N,L);
    
     	// extrude the edge if it does not have a  
   		// neighbor, or if it's a possible silhouette.  
   		if ( faces_light != (NdotL>facingLightThreshold) ) {  
       		// Make sure sides are oriented correctly.
       		int i0 = faces_light ? v0 : v1;  
       		int i1 = faces_light ? v1 : v0;  
       		v[0] = gl_PositionIn[i0];  
       		v[1] = vec4(l_pos.w*gl_PositionIn[i0].xyz - l_pos.xyz, 0);  
       		v[2] = gl_PositionIn[i1];  
       		v[3] = vec4(l_pos.w*gl_PositionIn[i1].xyz - l_pos.xyz, 0);  
       
       		// Emit a quad as a triangle strip.  
       		gl_Position = projMatrix*v[0];  
       		EmitVertex();  
       		gl_Position = projMatrix*v[1];  
       		EmitVertex();  
       		gl_Position = projMatrix*v[2];  
       		EmitVertex();  
       		gl_Position = projMatrix*v[3];  
       		EmitVertex();
       		EndPrimitive();  
     	}  
  }  
} 