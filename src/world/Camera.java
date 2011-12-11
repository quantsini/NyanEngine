package world;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

import render.ShaderContext;

public class Camera extends RenderEntity {
	/**
	 */
	public float fov;
	/**
	 */
	public float aspect;
	/**
	 */
	public float znear;
	/**
	 */
	public float zfar;
	/**
	 */
	private FloatBuffer projMatrix;

	public Camera(String n, float fov, float aspect, float znear, float zfar) {
		super(n);
		this.fov = fov;
		this.aspect = aspect;
		this.znear = znear;
		this.zfar = zfar;
		projMatrix = BufferUtils.createFloatBuffer(16);
	}

	public void commitCamera() {
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GLU.gluPerspective(fov, aspect, znear, zfar);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();
		glTransform();
		projMatrix.clear();
		GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, projMatrix);
	}

	/**
	 * @return
	 */
	public FloatBuffer getProjMatrix() {
		return projMatrix;
	}

	public boolean render(ShaderContext sc) {
		return false;
	}

}
