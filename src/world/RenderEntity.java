package world;

import java.nio.FloatBuffer;
import java.util.HashSet;
import java.util.Set;

import org.jblas.FloatMatrix;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import render.ShaderContext;
import render.Material;

public abstract class RenderEntity {
	private FloatMatrix localOrigin;
	private FloatMatrix glMatrix;
	private String name;
	private Set<RenderEntity> anchorList;
	private boolean spawned;
	public boolean castShadows;
	public boolean receiveShadows;
	private FloatBuffer tempBuff;
	private Material material;

	public RenderEntity(String n) {
		name = n;
		anchorList = new HashSet<RenderEntity>();
		spawned = true;
		localOrigin = FloatMatrix.zeros(3);
		glMatrix = FloatMatrix.eye(4);
		tempBuff = BufferUtils.createFloatBuffer(16);
	}

	public void setModelMatrix(float[] nm) {
		glMatrix = new FloatMatrix(nm);
	}
	
	public void getModelMatrix(FloatBuffer toret) {
		toret.clear();
		toret.put(glMatrix.data);
		toret.flip();
	}

	private void glMoveOrigin() {
		GL11.glTranslated(-localOrigin.get(0), -localOrigin.get(1),
				-localOrigin.get(2));
	}

	protected void glTransform() {
		glMoveOrigin();
		tempBuff.put(glMatrix.data);
		tempBuff.flip();
		GL11.glMultMatrix(tempBuff);
	}

	protected void glTransformReverse() {
		tempBuff.put(glMatrix.data);
		tempBuff.flip();
		GL11.glMultMatrix(tempBuff);
		glMoveOrigin();
	}

	public void spawn() {
		spawned = true;
		// add events to the event queue
	}

	public void despawn() {
		spawned = false;
		// unadd events to the event queue
	}
	
	public boolean spawned() {
		return spawned;
	}

	public void registerEvent() {
		throw new RuntimeException("nope");
	}

	public void setLocalOrigin(FloatMatrix pos) {
		localOrigin = pos.dup();
	}
	
	public abstract boolean render(ShaderContext shaderContext);
}
