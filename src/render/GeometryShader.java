package render;

import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL32;

public class GeometryShader extends Shader {

	public GeometryShader(String name, String source) {
		super(name);
		shaderID = GL20.glCreateShader(GL32.GL_GEOMETRY_SHADER);
		setSource(source);
	}
}
