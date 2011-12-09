package render;

import org.lwjgl.opengl.GL20;

public class VertexShader extends Shader {

	public VertexShader(String name, String source) {
		super(name);
		shaderID = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
		setSource(source);
	}
}
