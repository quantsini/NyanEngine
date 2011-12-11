package geometry.primitives;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL32;

import geometry.Vertex;
import geometry.Mesh;

public class Plane extends Mesh {
	/**
	 */
	private boolean vboNeedsUpdate;
	/**
	 */
	private int bufferType = GL11.GL_QUADS;
	/**
	 */
	private int subdivision;
	/**
	 */
	private float width;
	/**
	 */
	private float height;

	public Plane(String n, int sd, float w, float h) {
		super(n);
		vboNeedsUpdate = true;
		subdivision = sd;
		width = w;
		height = h;
	}

	@Override
	public int generateVBO() {
		if (vboNeedsUpdate) {
			ArrayList<Vertex> verts = new ArrayList<Vertex>();
			ArrayList<Vertex> tVerts = new ArrayList<Vertex>();

			verts.add(new Vertex(0, 0, height, 0, 1, 0));
			tVerts.add(new Vertex(0, 0, 1, 0, 0, 0));

			verts.add(new Vertex(width, 0, height, 0, 1, 0));
			tVerts.add(new Vertex(1, 0, 1, 0, 0, 0));

			verts.add(new Vertex(width, 0, 0, 0, 1, 0));
			tVerts.add(new Vertex(1, 0, 0, 0, 0, 0));

			verts.add(new Vertex(0, 0, 0, 0, 1, 0));
			tVerts.add(new Vertex(0, 0, 0, 0, 0, 0));

			FloatBuffer vBuffer = BufferUtils.createFloatBuffer(3 * verts
					.size());
			FloatBuffer nBuffer = BufferUtils.createFloatBuffer(3 * verts
					.size());
			FloatBuffer tBuffer = BufferUtils.createFloatBuffer(2 * tVerts
					.size());
			for (Vertex vertex : verts) {
				vBuffer.put((float) vertex.position().get(0));
				vBuffer.put((float) vertex.position().get(1));
				vBuffer.put((float) vertex.position().get(2));

				nBuffer.put((float) vertex.normal().get(0));
				nBuffer.put((float) vertex.normal().get(1));
				nBuffer.put((float) vertex.normal().get(2));
			}

			for (Vertex vertex : tVerts) {
				tBuffer.put((float) vertex.position().get(0));
				tBuffer.put((float) vertex.position().get(2));
			}

			vBuffer.flip();
			nBuffer.flip();
			tBuffer.flip();

			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboID);
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vBuffer,
					GL15.GL_STATIC_DRAW);
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, nboID);
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, nBuffer,
					GL15.GL_STATIC_DRAW);
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, tboID);
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, tBuffer,
					GL15.GL_STATIC_DRAW);
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
			// GL11.glTexCoordPointer(2, 0, tBuffer);
			vboNeedsUpdate = false;
		}
		return bufferType;
	}
}
