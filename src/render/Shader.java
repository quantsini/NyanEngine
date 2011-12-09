package render;

import org.lwjgl.opengl.GL20;

public abstract class Shader {
	protected int shaderID;
	private boolean isCompiled;
	private boolean canCompile;
	private String name;

	public Shader(String n) {
		name = n;
		shaderID = -1;
		isCompiled = false;
	}
	
	public String getName() {
		return name;
	}

	public String getInfo(int len) {
		return GL20.glGetShaderInfoLog(shaderID, len);
	}

	protected void setSource(String source) {
		if (!isCompiled) {
			GL20.glShaderSource(shaderID, source);
			canCompile = true;
		} else {
			throw new RuntimeException(
					"Trying to set source of a compiled shader!");
		}
	}

	public void attachTo(ShaderProgram prog) {
		int spid = prog.getShaderProgramID();
		compile();
		if (GL20.glIsProgram(spid)) {
			if (!prog.isLinked()) {
				GL20.glAttachShader(spid, shaderID);
			} else {
				throw new RuntimeException("Trying to attach to linked program");
			}
		} else {
			throw new RuntimeException("Invalid program");
		}
		
	}

	public void compile() {
		if (!canCompile) {
			throw new RuntimeException("No source attached!");
		}
		if (!isCompiled) {
			GL20.glCompileShader(shaderID);
		}
	}

	public int getShaderID() {
		return shaderID;
	}

	protected void finalize() throws Throwable {
		try {
			// ShaderProgram.unbind();
			// GL20.glDeleteShader(shaderID);
		} finally {
			super.finalize();
		}
	}

}
