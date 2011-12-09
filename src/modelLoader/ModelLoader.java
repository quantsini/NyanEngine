package modelLoader;

import geometry.Line;
import geometry.TriangleMesh;
import geometry.Triangle;
import geometry.Vertex;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.jblas.DoubleMatrix;

public class ModelLoader {
	public static TriangleMesh loadSTL(String fileName, float size) {
		InputStream stream = null;
		int numTriangles = 0;
		DoubleMatrix v1;
		DoubleMatrix v2;
		DoubleMatrix v3;
		DoubleMatrix a;
		DoubleMatrix b;
		DoubleMatrix normal;
		DoubleMatrix inNorm;
		ByteBuffer bb;
		byte[] ba;
		try {
			File f = new File(fileName);
			stream = new FileInputStream(f);
		} catch (Exception e) {
			e.printStackTrace();
		}

		byte[] name = new byte[80];

		try {
			stream.read(name);
		} catch (Exception e) {
			System.out.println(e);
		}
		String strName = new String(name);

		TriangleMesh toRet = new TriangleMesh(strName);

		ba = new byte[4];
		try {
			stream.read(ba);
		} catch (Exception e) {
			System.out.println(e);
		}

		bb = ByteBuffer.wrap(ba);
		bb.order(ByteOrder.nativeOrder());
		try {
			numTriangles = bb.getInt();
		} catch (Exception e) {
			System.out.println(e);
		}

		// each face is 50 bytes, 4*4*3+2
		ba = new byte[50 * numTriangles];
		try {
			stream.read(ba);
		} catch (Exception e) {
			System.out.println(e);
		}
		bb = ByteBuffer.wrap(ba);
		bb.order(ByteOrder.nativeOrder());

		Vertex vertex;
		Vertex oldVert;
		Set<DoubleMatrix> vertSet = new HashSet<DoubleMatrix>();
		ArrayList<Vertex> verts = new ArrayList<Vertex>();
		HashMap<Vertex, Integer> vertIndices = new HashMap<Vertex, Integer>();

		for (int lcv = 0; lcv < numTriangles; lcv++) {
			inNorm = new DoubleMatrix(3);
			v1 = new DoubleMatrix(3);
			v2 = new DoubleMatrix(3);
			v3 = new DoubleMatrix(3);

			for (int i = 0; i < 3; i++) {
				inNorm.put(i, bb.getFloat());
			}

			for (int i = 0; i < 3; i++) {
				v1.put(i, bb.getFloat());
			}

			for (int i = 0; i < 3; i++) {
				v2.put(i, bb.getFloat());
			}

			for (int i = 0; i < 3; i++) {
				v3.put(i, bb.getFloat());
			}

			try {
				bb.get();
				bb.get();
			} catch (Exception e) {
				System.out.println(e);
			}

			a = v1.sub(v2);
			b = v1.sub(v3);
			normal = new DoubleMatrix(3);

			normal.put(0, a.get(1) * b.get(2) - a.get(2) * b.get(1));
			normal.put(1, a.get(2) * b.get(0) - a.get(0) * b.get(2));
			normal.put(2, a.get(0) * b.get(1) - a.get(1) * b.get(0));

			double n1 = (a.get(1) * b.get(2) - a.get(2) * b.get(1));
			double n2 = (a.get(2) * b.get(0) - a.get(0) * b.get(2));
			double n3 = (a.get(0) * b.get(1) - a.get(1) * b.get(0));

			Vertex V1 = new Vertex(v1.get(0) * size, v1.get(1) * size,
					v1.get(2) * size, n1, n2, n3);
			Vertex V2 = new Vertex(v2.get(0) * size, v2.get(1) * size,
					v2.get(2) * size, n1, n2, n3);
			Vertex V3 = new Vertex(v3.get(0) * size, v3.get(1) * size,
					v3.get(2) * size, n1, n2, n3);

			Vertex[] vv = { V1, V2, V3 };
			for (int vi = 0; vi < vv.length; vi++) {
				vertex = vv[vi];

				if (vertSet.contains(vertex.position())) {
					oldVert = verts.get(vertIndices.get(vertex)); // LOLOLOLOLOLOLOL

					oldVert.aggregateNormals(vv[vi]);

					vv[vi] = oldVert;
				} else {
					vertSet.add(vertex.position());
					verts.add(vertex);
					vertIndices.put(vertex, verts.size() - 1);
				}
			}

			Triangle tri = new Triangle(vv[0], vv[1], vv[2]);
			toRet.addTriangle(tri);
		}

		try {
			stream.close();
		} catch (Exception e) {
			System.out.println(e);
		}

		// for each edge, add adjacent triangles
		Set<Line> edges = toRet.lines();
		for (Line edge : edges) {
			Vertex ve1 = edge.v1();
			Vertex ve2 = edge.v2();

			Set<Triangle> v1Triangles = ve1.boundTriangles();
			Set<Triangle> v2Triangles = ve2.boundTriangles();

			// v1 & v2
			v1Triangles.retainAll(v2Triangles);

			for (Triangle t1 : v1Triangles) {
				for (Triangle t2 : v2Triangles) {
					if (!t1.equals(t2)) {
						t1.addAdjacentTriangle(t2);
						t2.addAdjacentTriangle(t1);
					}
				}
			}
		}
		/*
		 * System.out.println("Name: " + strName);
		 * System.out.println("Triangles: " + toRet.triangles().size());
		 * System.out.println("Vertices: " + toRet.vertices().size());
		 * System.out.println("Edges: " + toRet.lines().size());
		 */
		return toRet;
	}
}
