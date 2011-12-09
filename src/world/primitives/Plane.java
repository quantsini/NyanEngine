package world.primitives;


import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;

import world.*;

public class Plane extends RenderableMesh {
	public Plane(String n, int sd, float w, float h) {
		super(n, new geometry.primitives.Plane(n, sd, w, h));
		castShadows = false;
	}

	@Override
	protected boolean renderMesh() {
		int vboID, nboID, tboID;
		mesh.generateVBO();
		vboID = mesh.vboID();
		nboID = mesh.nboID();
		tboID = mesh.tboID();

		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboID);
		GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 0, 0);
		GL20.glEnableVertexAttribArray(0);
		
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, nboID);
		GL20.glVertexAttribPointer(1, 3, GL11.GL_FLOAT, false, 0, 0);
		GL20.glEnableVertexAttribArray(1);
		
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, tboID);
		GL20.glVertexAttribPointer(2, 2, GL11.GL_FLOAT, false, 0, 0);
		GL20.glEnableVertexAttribArray(2);
		
		GL11.glDrawArrays(GL11.GL_QUADS, 0, 4);
		GL20.glDisableVertexAttribArray(0);
		GL20.glDisableVertexAttribArray(1);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		
		/*
		// vertices
		GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboID);
		GL11.glVertexPointer(3, GL11.GL_FLOAT, 0, 0);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

		// normals
		GL11.glEnableClientState(GL11.GL_NORMAL_ARRAY);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, nboID);
		GL11.glNormalPointer(GL11.GL_FLOAT, 0, 0);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

		// texture coordinate
		GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, tboID);
		GL13.glClientActiveTexture(GL13.GL_TEXTURE0);
		GL11.glTexCoordPointer(2, GL11.GL_FLOAT, 0, 0);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

		// draw the entire array
		GL11.glDrawArrays(GL11.GL_QUADS, 0, 4);

		//unbind everything
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		GL11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
		GL11.glDisableClientState(GL11.GL_NORMAL_ARRAY);
		GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
		*/
		return true;
	}

}
