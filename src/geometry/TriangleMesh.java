package geometry;

import java.nio.FloatBuffer;
import java.util.*;

import org.lwjgl.*;
import org.lwjgl.opengl.*;

public class TriangleMesh extends Mesh {
	/**
	 */
	private Set<Vertex> vertices;
	/**
	 */
	private Set<Line> lines;
	/**
	 */
	private Set<Triangle> triangles;
	/**
	 */
	private boolean vboNeedsUpdate;
	/**
	 */
	private int bufferType = GL32.GL_TRIANGLES_ADJACENCY;
	/**
	 */
	float scale = 1;

	public Set<Line> lines() {
		return new HashSet<Line>(lines);
	}

	public Set<Triangle> triangles() {
		return new HashSet<Triangle>(triangles);
	}

	public Set<Vertex> vertices() {
		return new HashSet<Vertex>(vertices);
	}

	public TriangleMesh(String name) {
		super(name);
		vertices = new HashSet<Vertex>();
		lines = new HashSet<Line>();
		triangles = new HashSet<Triangle>();
		vboNeedsUpdate = true;
		scale = 0.01f;
	}

	public void addVertex(Vertex v) {
		vertices.add(v);
		vboNeedsUpdate = true;
	}

	public void addLine(Vertex v1, Vertex v2) {
		vertices.add(v1);
		vertices.add(v2);
		lines.add(new Line(v1, v2));
		lines.add(new Line(v2, v1));
		vboNeedsUpdate = true;
	}

	public void addLine(Line l) {
		lines.add(l);
	}

	public void addTriangle(Triangle tri) {
		vertices.addAll(tri.vertices());
		lines.addAll(tri.lines());
		triangles.add(tri);
		vboNeedsUpdate = true;
	}

	public int generateVBO() {
		if (vboNeedsUpdate) {
			ArrayList<Vertex> verts = new ArrayList<Vertex>();
			for (Triangle triangle : triangles) {
				Set<Triangle> adjacentTriangles = triangle.adjacentTriangles();

				// go through each vertex...
				// this is done instead of going through each edge
				// because we want to preserve vertex ordering
				for (int lcv = 0; lcv < 3; lcv++) {
					Vertex vertex = triangle.vertices().get(lcv);
					Vertex nextVertex = triangle.vertices().get((lcv + 1) % 3);

					// first add this vertex
					verts.add(vertex);

					// find the edge that is neighboring the two triangles
					Line edge = new Line(vertex, nextVertex);
					for (Triangle adjTriangle : adjacentTriangles) {
						if (adjTriangle.lines().contains(edge)) {
							Vector<Vertex> difference = adjTriangle.vertices();
							difference.removeAll(triangle.vertices());
							if (difference.size() > 0) {
								vertex = difference.firstElement();
							}
							break;
						}
					}

					// add the adjacent triangle coordinate
					verts.add(vertex);
				}
			}

			// to float buffer
			FloatBuffer vBuffer = BufferUtils.createFloatBuffer(6 * verts
					.size());
			FloatBuffer nBuffer = BufferUtils.createFloatBuffer(6 * verts
					.size());
			FloatBuffer tBuffer = BufferUtils.createFloatBuffer(4 * verts
					.size());

			for (int lcv = 0; lcv < verts.size(); lcv += 2) {
				vBuffer.put((float) verts.get(lcv).position().get(0));
				vBuffer.put((float) verts.get(lcv).position().get(1));
				vBuffer.put((float) verts.get(lcv).position().get(2));

				vBuffer.put((float) verts.get(lcv + 1).position().get(0));
				vBuffer.put((float) verts.get(lcv + 1).position().get(1));
				vBuffer.put((float) verts.get(lcv + 1).position().get(2));

				nBuffer.put((float) verts.get(lcv).normal().get(0));
				nBuffer.put((float) verts.get(lcv).normal().get(1));
				nBuffer.put((float) verts.get(lcv).normal().get(2));

				nBuffer.put((float) verts.get(lcv + 1).normal().get(0));
				nBuffer.put((float) verts.get(lcv + 1).normal().get(1));
				nBuffer.put((float) verts.get(lcv + 1).normal().get(2));

				tBuffer.put((float)verts.get(lcv).position().get(0)/5);
				tBuffer.put((float)verts.get(lcv).position().get(2)/5);
				tBuffer.put((float)verts.get(lcv+1).position().get(0)/5);
				tBuffer.put((float)verts.get(lcv+1).position().get(2)/5);
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
			vboNeedsUpdate = false;
		}

		return bufferType;
	}

	public int getNumTriangles() {
		return triangles.size();
	}

}
