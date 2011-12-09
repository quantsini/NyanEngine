package geometry;

import java.util.Vector;

public class Line {
	private Vector<Vertex> vertices;

	public Line(Vertex v1, Vertex v2) {
		vertices = new Vector<Vertex>(2);
		vertices.add(v1);
		vertices.add(v2);
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof Line) {
			Line other = (Line) o;
			return vertices.equals(other.vertices);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return vertices.hashCode();
	}

	public Vertex v1() {
		return vertices.get(0);
	}

	public Vertex v2() {
		return vertices.get(1);
	}
	
	public Line reverse() {
		return new Line(v2(), v1());
	}
}
