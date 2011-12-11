package geometry;

import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import org.jblas.DoubleMatrix;

public class Triangle {
	/**
	 */
	private Vector<Vertex> vertices;
	/**
	 */
	private Set<Line> lines;
	/**
	 */
	private DoubleMatrix faceNormal;
	/**
	 */
	private DoubleMatrix centerPosition;
	/**
	 */
	private Set<Triangle> adjacentTriangles;

	public Triangle(Vertex v1, Vertex v2, Vertex v3) {
		vertices = new Vector<Vertex>(3);
		vertices.add(v1);
		vertices.add(v2);
		vertices.add(v3);

		lines = new HashSet<Line>();
		lines.add(new Line(v1, v2));
		lines.add(new Line(v2, v3));
		lines.add(new Line(v3, v1));
		lines.add(new Line(v2, v1));
		lines.add(new Line(v3, v2));
		lines.add(new Line(v1, v3));

		DoubleMatrix S1 = v1.position().sub(v2.position());
		DoubleMatrix S2 = v1.position().sub(v3.position());
		faceNormal = new DoubleMatrix(3);

		faceNormal.put(0, S1.get(1) * S2.get(2) - S1.get(2) * S2.get(1));
		faceNormal.put(1, S1.get(2) * S2.get(0) - S1.get(0) * S2.get(2));
		faceNormal.put(2, S1.get(0) * S2.get(1) - S1.get(1) * S2.get(0));

		centerPosition = v1.position().add(v2.position().add(v3.position()));

		adjacentTriangles = new HashSet<Triangle>();

		v1.bindToTriangle(this);
		v2.bindToTriangle(this);
		v3.bindToTriangle(this);
	}

	public void addAdjacentTriangle(Triangle tri) {
		if (!adjacentTriangles.contains(tri)) {
			adjacentTriangles.add(tri);
		}
	}

	public Vector<Vertex> vertices() {
		return (Vector<Vertex>) vertices.clone();
	}

	public Set<Line> lines() {
		return new HashSet<Line>(lines);
	}

	public Set<Triangle> adjacentTriangles() {
		return new HashSet<Triangle>(adjacentTriangles);
	}
	
	public DoubleMatrix centerPosition() {
		return centerPosition.dup();
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof Triangle) {
			Triangle otherTri = (Triangle) o;
			return vertices.equals(otherTri.vertices);
		}

		return false;
	}

	@Override
	public int hashCode() {
		return vertices.hashCode();
	}
}
