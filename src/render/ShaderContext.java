package render;

import java.nio.FloatBuffer;
import java.util.Hashtable;
import java.util.Map.Entry;

import world.Camera;

public class ShaderContext {
	private ShaderProgram mainProgram;
	// public final ArrayList<ShaderProgram> auxPrograms;
	private FloatBuffer projectionMatrix;
	private FloatBuffer modelViewMatrix;
	private FloatBuffer normalMatrix;
	private Hashtable<String, Float> floatUniforms;
	private Hashtable<String, Integer> integerUniforms;
	private Hashtable<String, float[]> floatvUniforms;
	private String name;
	private float time;

	public ShaderContext(String n, ShaderProgram shader) {
		name = n;
		mainProgram = shader;
		// auxPrograms = new ArrayList<ShaderProgram>();
		floatUniforms = new Hashtable<String, Float>();
		integerUniforms = new Hashtable<String, Integer>();
		floatvUniforms = new Hashtable<String, float[]>();
	}

	public void setCameraMatrix(Camera cam) {
		setProjectionMatrix(cam.getProjMatrix());
	}

	public void setProjectionMatrix(FloatBuffer m) {
		projectionMatrix = m;
		if (mainProgram.isAttached()) {
			if (projectionMatrix != null) {
				mainProgram.setUniformMat4("projMatrix", projectionMatrix);
				/*
				 * for (ShaderProgram sp : auxPrograms) {
				 * sp.setUniformMat4("projMatrix", projectionMatrix); }
				 */
			}
		}
	}

	public void setModelViewMatrix(FloatBuffer m) {
		modelViewMatrix = m;
		if (mainProgram.isAttached()) {
			if (modelViewMatrix != null) {
				mainProgram.setUniformMat4("modelViewMatrix", modelViewMatrix);
			}
		}
	}

	public void bind() {
		if (!mainProgram.isAttached()) {
			mainProgram.bind();
		}


		if (projectionMatrix != null) {
			mainProgram.setUniformMat4("projMatrix", projectionMatrix);
		}

		if (modelViewMatrix != null) {
			mainProgram.setUniformMat4("modelViewMatrix", modelViewMatrix);
		}

		for (Entry<String, Float> entry : floatUniforms.entrySet()) {
			mainProgram.setUniformf(entry.getKey(), entry.getValue());
		}
		
		for (Entry<String, Integer> entry : integerUniforms.entrySet()) {
			mainProgram.setUniformi(entry.getKey(), entry.getValue());
		}
		
		for (Entry<String, float[]> entry : floatvUniforms.entrySet()) {
			String n = entry.getKey();
			float[] val = entry.getValue();
			if (val.length == 2) {
				mainProgram.setUniform2f(n, val[0], val[1]);
			}
			if (val.length == 3) {
				mainProgram.setUniform3f(n, val[0], val[1], val[2]);
			}
			if (val.length == 4) {
				mainProgram.setUniform4f(n, val[0], val[1], val[2], val[3]);
			}
		}
		
		mainProgram.setUniformf("time", time);
	}

	public void uniform1f(String name, float val) {
		floatUniforms.put(name, val);
		if (mainProgram.isAttached()) {
			mainProgram.setUniformf(name, val);
		}
	}
	
	public void uniform1i(String name, int val) {
		integerUniforms.put(name, val);
		if (mainProgram.isAttached()) {
			mainProgram.setUniformi(name, val);
		}
	}
	
	public void uniformvf(String name, float[] val) {
		floatvUniforms.put(name, val);
		if (mainProgram.isAttached()) {
			if (val.length == 2) {
				mainProgram.setUniform2f(name, val[0], val[1]);
			}
			if (val.length == 3) {
				mainProgram.setUniform3f(name, val[0], val[1], val[2]);
			}
			if (val.length == 4) {
				mainProgram.setUniform4f(name, val[0], val[1], val[2], val[3]);
			}
		}
	}

	public void unbindShaders() {
		ShaderProgram.unbindAll();
	}

	public void updateTime(long t) {
		time = t;
		if (mainProgram.isAttached()) {
			mainProgram.setUniformf("time", time);
		}
	}
}
