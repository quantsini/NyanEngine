package geometry;

import java.util.*;
import org.jblas.*;

public class Vertex {
	/**
	 */
	private DoubleMatrix position;
	/**
	 */
	private DoubleMatrix normal;
	/**
	 */
	private Set<Triangle> boundTriangles;
	/**
	 */
	private boolean normalized;

	public Vertex(double x, double y, double z, double nx, double ny, double nz) {
		boundTriangles = new HashSet<Triangle>();
		position = new DoubleMatrix(3);
		position.put(0, x);
		position.put(1, y);
		position.put(2, z);

		normal = new DoubleMatrix(3);

		normal.put(0, nx);
		normal.put(1, ny);
		normal.put(2, nz);
		normal = org.jblas.Geometry.normalize(normal);
		normalized = true;
	}

	public boolean bindToTriangle(Triangle tri) {
		if (!boundTriangles.contains(tri)) {
			boundTriangles.add(tri);
			return false;
		}
		return true;
	}

	public Set<Triangle> boundTriangles() {
		return new HashSet<Triangle>(boundTriangles);
	}

	public DoubleMatrix position() {
		return position.dup();
	}

	public DoubleMatrix normal() {
		return normal.dup();
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof Vertex) {
			Vertex other = (Vertex) o;
			return position.equals(other.position);
			// && normal.equals(other.normal);
		}
		return false;
	}

	@Override
	public int hashCode() {
		Vector<Integer> temp = new Vector<Integer>();
		temp.add(position.hashCode());
		// temp.add(normal.hashCode());

		return temp.hashCode();
	}

	public void aggregateNormals(DoubleMatrix other) {
		normal = normal.add(other);
		normalized = false;
	}

	public void aggregateNormals(Vertex other) {
		aggregateNormals(other.normal);
	}

	public void normalizeNormals() {
		if (!normalized) {
			normal = org.jblas.Geometry.normalize(normal);
			normalized = true;
		}
	}
}
