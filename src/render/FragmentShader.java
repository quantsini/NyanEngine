package render;


import org.lwjgl.opengl.GL20;

public class FragmentShader extends Shader {
	public FragmentShader(String name, String source) {
		super(name);
		shaderID = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);
		setSource(source);
	}
}
