// main program

uniform sampler2D sceneMap;
uniform sampler2D depthMap;

void main(void)
{
    gl_Position = ftransform();
}