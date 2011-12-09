package world;

import geometry.Mesh;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import render.Material;
import render.ShaderContext;

public abstract class RenderableMesh extends RenderEntity {
	private Material mat;
	protected Mesh mesh;

	FloatBuffer diffuse = arrToFloatBuffer(new float[] { 0.3f, 0.9f, 0.8f, 1.0f });

	FloatBuffer specular = arrToFloatBuffer(new float[] { 1f, 1f, 1f, 1f });

	FloatBuffer ambient = arrToFloatBuffer(new float[] { 0.4f, 0.4f, 0.4f, 1f });

	float shininess = 30;

	private FloatBuffer tempBuffer;

	public RenderableMesh(String n, Mesh m) {
		super(n);
		castShadows = true;
		receiveShadows = true;
		mesh = m;
		tempBuffer = BufferUtils.createFloatBuffer(16);
	}

	private static FloatBuffer arrToFloatBuffer(float[] arr) {
		FloatBuffer fb;
		fb = BufferUtils.createFloatBuffer(4);
		fb.put(arr);
		fb.flip();
		return fb;
	}

	protected abstract boolean renderMesh();

	public boolean render(ShaderContext sc) {

		GL11.glMaterial(GL11.GL_FRONT, GL11.GL_DIFFUSE, diffuse);
		GL11.glMaterial(GL11.GL_FRONT, GL11.GL_SPECULAR, specular);
		GL11.glMaterial(GL11.GL_FRONT, GL11.GL_AMBIENT, ambient);
		GL11.glMaterialf(GL11.GL_FRONT, GL11.GL_SHININESS, shininess);

		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glPushMatrix();
		glTransform();

		GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, tempBuffer);
		sc.bind();
		sc.setModelViewMatrix(tempBuffer);
		renderMesh();
		sc.unbindShaders();

		GL11.glPopMatrix();

		return true;
	}
}
