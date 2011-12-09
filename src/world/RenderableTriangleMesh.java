package world;

import geometry.*;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL32;


public class RenderableTriangleMesh extends RenderableMesh {
	public RenderableTriangleMesh(String n, TriangleMesh m) {
		super(n, m);
	}

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

		GL11.glDrawArrays(GL32.GL_TRIANGLES_ADJACENCY, 0,
				6 * ((TriangleMesh) mesh).getNumTriangles());
		GL20.glDisableVertexAttribArray(0);
		GL20.glDisableVertexAttribArray(1);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

		/*
		 * GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
		 * GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboID);
		 * GL11.glVertexPointer(3, GL11.GL_FLOAT, 0, 0);
		 * GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		 * 
		 * GL11.glEnableClientState(GL11.GL_NORMAL_ARRAY);
		 * GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, nboID);
		 * GL11.glNormalPointer(GL11.GL_FLOAT, 0, 0);
		 * GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		 * 
		 * // GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
		 * 
		 * GL11.glDrawArrays(GL32.GL_TRIANGLES_ADJACENCY, 0, 6 * ((TriangleMesh)
		 * mesh).getNumTriangles());
		 * 
		 * GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0); //
		 * GL11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
		 * GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
		 * GL11.glDisableClientState(GL11.GL_NORMAL_ARRAY);
		 */
		return true;
	}
}
