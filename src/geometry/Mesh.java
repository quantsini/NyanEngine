package geometry;

import org.lwjgl.opengl.GL15;

public abstract class Mesh {
	protected int vboID;
	protected int tboID;
	protected int nboID;
	private String name;

	public Mesh(String n) {
		name = n;

		vboID = GL15.glGenBuffers();

		nboID = GL15.glGenBuffers();
		
		tboID = GL15.glGenBuffers();
	}

	public abstract int generateVBO();

	public int vboID() {
		return vboID;
	}

	public int nboID() {
		return nboID;
	}

	public int tboID() {
		return tboID;
	}
	
	public String name() {
		return name;
	}
}
