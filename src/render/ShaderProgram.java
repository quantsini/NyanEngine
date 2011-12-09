package render;

import java.io.File;
import java.nio.FloatBuffer;
import java.util.Scanner;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL31;

public class ShaderProgram {
	private String name;
	private int shaderProgramID;
	private boolean linked;

	public ShaderProgram(String n) {
		name = n;
		shaderProgramID = GL20.glCreateProgram();
		linked = false;
	}

	public int getShaderProgramID() {
		return shaderProgramID;
	}

	public void attach(Shader sh) {
		int shid = sh.getShaderID();

		if (!linked) {
			sh.compile();
			if (GL20.glIsShader(shid)) {
				sh.attachTo(this);
			} else {
				throw new RuntimeException("Not a valid shader");
			}
		} else {
			throw new RuntimeException(
					"Trying to attach a shader to a linked program.");
		}
	}

	public void link() {
		if (!linked) {
			GL20.glBindAttribLocation(shaderProgramID, 0, "in_vertex");
			GL20.glBindAttribLocation(shaderProgramID, 1, "in_normal");
			GL20.glBindAttribLocation(shaderProgramID, 2, "in_texCoord");

			org.lwjgl.opengl.ARBGeometryShader4
					.glProgramParameteriARB(
							shaderProgramID,
							org.lwjgl.opengl.ARBGeometryShader4.GL_GEOMETRY_INPUT_TYPE_ARB,
							org.lwjgl.opengl.ARBGeometryShader4.GL_TRIANGLES_ADJACENCY_ARB);
			org.lwjgl.opengl.ARBGeometryShader4
					.glProgramParameteriARB(
							shaderProgramID,
							org.lwjgl.opengl.ARBGeometryShader4.GL_GEOMETRY_OUTPUT_TYPE_ARB,
							GL11.GL_TRIANGLE_STRIP);
			org.lwjgl.opengl.ARBGeometryShader4
					.glProgramParameteriARB(
							shaderProgramID,
							org.lwjgl.opengl.ARBGeometryShader4.GL_GEOMETRY_VERTICES_OUT_ARB,
							1024);

			GL20.glLinkProgram(shaderProgramID);
			linked = true;
		}
	}

	public boolean isLinked() {
		return linked;
	}

	public void bind() {
		if (!linked) {
			link();
		}
		GL20.glUseProgram(shaderProgramID);
	}

	public String getInfo(int num) {
		return name + "\n" + GL20.glGetProgramInfoLog(shaderProgramID, num);
	}

	protected void finalize() throws Throwable {
		try {
			// ShaderProgram.unbind();
			// GL20.glDeleteProgram(shaderProgramID);
		} finally {
			super.finalize();
		}
	}

	public void setUniformMat4(String name, FloatBuffer matrix) {
		int loc = GL20.glGetUniformLocation(shaderProgramID, name);
		GL20.glUniformMatrix4(loc, false, matrix);
	}

	public void setUniformMat3(String name, FloatBuffer matrix) {
		int loc = GL20.glGetUniformLocation(shaderProgramID, name);
		GL20.glUniformMatrix3(loc, false, matrix);
	}

	public void setUniform4f(String name, FloatBuffer vec) {
		int loc = GL20.glGetUniformLocation(shaderProgramID, name);
		GL20.glUniform4(loc, vec);
	}

	public void setUniform4f(String name, float v1, float v2, float v3, float v4) {
		int loc = GL20.glGetUniformLocation(shaderProgramID, name);
		GL20.glUniform4f(loc, v1, v2, v3, v4);
	}

	public void setUniform3f(String name, FloatBuffer vec) {
		int loc = GL20.glGetUniformLocation(shaderProgramID, name);
		GL20.glUniform3(loc, vec);
	}

	public void setUniform3f(String name, float v1, float v2, float v3) {
		int loc = GL20.glGetUniformLocation(shaderProgramID, name);
		GL20.glUniform3f(loc, v1, v2, v3);
	}

	public void setUniform2f(String name, float v1, float v2) {
		int loc = GL20.glGetUniformLocation(shaderProgramID, name);
		GL20.glUniform2f(loc, v1, v2);
	}

	public void setUniformf(String name, float val) {
		int loc = GL20.glGetUniformLocation(shaderProgramID, name);
		GL20.glUniform1f(loc, val);
	}

	public void setUniformi(String name, int val) {
		int loc = GL20.glGetUniformLocation(shaderProgramID, name);
		GL20.glUniform1i(loc, val);
	}

	public boolean isAttached() {
		int curProg = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);
		return curProg == shaderProgramID;
	}

	public static void unbindAll() {
		GL20.glUseProgram(0);
	}

	private static String getString(String fileName) {
		String toret = null;
		try {
			Scanner scanner = new Scanner(new File(fileName))
					.useDelimiter("\\Z");
			toret = scanner.next();

		} catch (Exception e) {
			System.out.println(e);
		}

		return toret;
	}

	public static ShaderProgram getPhongShader() {
		String vertex = getString("Shaders/PhongShader/vertex.shader");
		String fragment = getString("Shaders/PhongShader/fragment.shader");

		VertexShader vert = new VertexShader("Vertex Shader", vertex);
		FragmentShader frag = new FragmentShader("Fragment Shader", fragment);
		ShaderProgram shader = new ShaderProgram("Phong Shader");
		shader.attach(vert);
		shader.attach(frag);
		shader.link();
		System.out.println(shader.getInfo(9999999));

		return shader;
	}

	public static ShaderProgram getVolumeShadowShader() {
		String vertex = getString("Shaders/VolumeShadowShader/vertex.shader");
		String fragment = getString("Shaders/VolumeShadowShader/fragment.shader");
		String geometry = getString("Shaders/VolumeShadowShader/geometry.shader");
		VertexShader vert = new VertexShader("Vertex Shader", vertex);
		FragmentShader frag = new FragmentShader("Fragment Shader", fragment);
		GeometryShader geom = new GeometryShader("Geometry Shader", geometry);
		ShaderProgram shader = new ShaderProgram("Shadow Volume Shader");
		shader.attach(vert);
		shader.attach(geom);
		shader.attach(frag);
		shader.link();
		System.out.println(shader.getInfo(9999999));

		return shader;
	}

	public static ShaderProgram getDOFShader() {
		String vertex = getString("Shaders/DOFShader/vertex.shader");
		String fragment = getString("Shaders/DOFShader/fragment.shader");
		VertexShader vert = new VertexShader("Vertex Shader", vertex);
		FragmentShader frag = new FragmentShader("Fragment Shader", fragment);
		ShaderProgram shader = new ShaderProgram("Postprocessing Shader");
		shader.attach(vert);
		shader.attach(frag);
		shader.link();
		System.out.println(shader.getInfo(9999999));

		return shader;
	}

	public static ShaderProgram getPassThruShader() {
		String vertex = getString("Shaders/PassThruShader/vertex.shader");
		String fragment = getString("Shaders/PassThruShader/fragment.shader");
		VertexShader vert = new VertexShader("Vertex Shader", vertex);
		FragmentShader frag = new FragmentShader("Fragment Shader", fragment);
		ShaderProgram shader = new ShaderProgram("Passthru Shader");
		shader.attach(vert);
		shader.attach(frag);
		shader.link();
		System.out.println(shader.getInfo(9999999));

		return shader;
	}

	public static ShaderProgram getScreenSpaceNormalDepthShader() {
		String vertex = getString("Shaders/ScreenSpaceNormalDepthShader/vertex.shader");
		String fragment = getString("Shaders/ScreenSpaceNormalDepthShader/fragment.shader");
		VertexShader vert = new VertexShader("Vertex Shader", vertex);
		FragmentShader frag = new FragmentShader("Fragment Shader", fragment);
		ShaderProgram shader = new ShaderProgram(
				"Screen Space Normal Depth Shader");
		shader.attach(vert);
		shader.attach(frag);
		shader.link();
		System.out.println(shader.getInfo(9999999));

		return shader;
	}

	public static ShaderProgram getSSAOShader() {
		String vertex = getString("Shaders/SSAOShader/vertex.shader");
		String fragment = getString("Shaders/SSAOShader/fragment.shader");
		VertexShader vert = new VertexShader("Vertex Shader", vertex);
		FragmentShader frag = new FragmentShader("Fragment Shader", fragment);
		ShaderProgram shader = new ShaderProgram("SSAO Shader");
		shader.attach(vert);
		shader.attach(frag);
		shader.link();
		System.out.println(shader.getInfo(9999999));

		return shader;
	}

	public static ShaderProgram getBlurShader() {
		String vertex = getString("Shaders/BlurShader/vertex.shader");
		String fragment = getString("Shaders/BlurShader/fragment.shader");
		VertexShader vert = new VertexShader("Vertex Shader", vertex);
		FragmentShader frag = new FragmentShader("Fragment Shader", fragment);
		ShaderProgram shader = new ShaderProgram("Gaussian Blur Shader");
		shader.attach(vert);
		shader.attach(frag);
		shader.link();
		System.out.println(shader.getInfo(9999999));

		return shader;
	}

	public static ShaderProgram getScreenBlendShader() {
		String vertex = getString("Shaders/ScreenBlendShader/vertex.shader");
		String fragment = getString("Shaders/ScreenBlendShader/fragment.shader");
		VertexShader vert = new VertexShader("Vertex Shader", vertex);
		FragmentShader frag = new FragmentShader("Fragment Shader", fragment);
		ShaderProgram shader = new ShaderProgram("Screen Blend Shader Shader");
		shader.attach(vert);
		shader.attach(frag);
		shader.link();
		System.out.println(shader.getInfo(9999999));

		return shader;
	}

	public static ShaderProgram getGammaShader() {
		String vertex = getString("Shaders/GammaShader/vertex.shader");
		String fragment = getString("Shaders/GammaShader/fragment.shader");
		VertexShader vert = new VertexShader("Vertex Shader", vertex);
		FragmentShader frag = new FragmentShader("Fragment Shader", fragment);
		ShaderProgram shader = new ShaderProgram("Screen Blend Shader Shader");
		shader.attach(vert);
		shader.attach(frag);
		shader.link();
		System.out.println(shader.getInfo(9999999));

		return shader;
	}

	public static ShaderProgram getSix() {
		String vertex = getString("Shaders/Slisesix/vertex.shader");
		String fragment = getString("Shaders/Slisesix/fragment.shader");
		VertexShader vert = new VertexShader("Vertex Shader", vertex);
		FragmentShader frag = new FragmentShader("Fragment Shader", fragment);
		ShaderProgram shader = new ShaderProgram("Screen Blend Shader Shader");
		shader.attach(vert);
		shader.attach(frag);
		shader.link();
		System.out.println(shader.getInfo(9999999));

		return shader;
	}
}
