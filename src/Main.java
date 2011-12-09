import java.awt.Color;
import java.nio.FloatBuffer;
import java.util.Scanner;

import modelLoader.ModelLoader;

import org.jblas.DoubleMatrix;
import org.jblas.FloatMatrix;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.input.Controller;
import org.lwjgl.input.Controllers;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.*;

import render.FragmentShader;
import render.ShaderContext;
import render.ShaderProgram;
import render.VertexShader;
import world.Camera;
import world.Light;
import world.PhysicsEntity;
import world.RenderableMesh;
import world.RenderableTriangleMesh;
import world.World;
import geometry.*;
import world.primitives.Box;

import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.collision.shapes.CylinderShape;
import com.bulletphysics.collision.shapes.SphereShape;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.linearmath.DefaultMotionState;
import com.bulletphysics.linearmath.Transform;

import javax.vecmath.Vector3f;

public class Main {
	debug.HelperDebug d = new debug.HelperDebug();
	PhysicsEntity<? extends RenderableMesh> obj;
	PhysicsEntity<? extends RenderableMesh> floor;

	TriangleMesh pillarMesh;
	Camera cam;
	World world;
	double x = 0;
	int maxFrameRate = 60;

	float cameraYRot = 0;
	float cameraXRot = 20;

	FloatBuffer temp = BufferUtils.createFloatBuffer(4);

	float[] lightAmbient = { 0.1f, 0.18f, 0.21f, 1f };
	float[] lightDiffuse = { 1f, 1f, 1f, 1f };
	float[] lightPos = new float[4];

	Transform trans = new Transform();
	DoubleMatrix camPos = new DoubleMatrix(3);
	DoubleMatrix camEuler = new DoubleMatrix(3);
	FloatMatrix force = new FloatMatrix(2);
	FloatMatrix rotMat = new FloatMatrix(2, 2);

	long lastFPS = getTime();
	long fps;
	private TriangleMesh cowMesh;
	private int selectedController = -1;

	public void gameLogicTick(long dt) {

		x += dt / 1000.0 * 20;
		if (x > 360) {
			x -= 360;
		}

		temp = BufferUtils.createFloatBuffer(4);
		for (int lcv = 0; lcv < lightPos.length; lcv++) {
			temp.put(lcv, lightPos[lcv]);
		}
		GL11.glLight(GL11.GL_LIGHT0, GL11.GL_POSITION, temp);

		float[] hsv = new float[3];
		Color.RGBtoHSB((int) (255 * lightDiffuse[0]),
				(int) (255 * lightDiffuse[1]), (int) (255 * lightDiffuse[2]),
				hsv);
		hsv[0] = (float) (x / 360.0);
		hsv[1] = 0.4f;
		hsv[2] = 0.8f;

		int col = Color.HSBtoRGB(hsv[0], hsv[1], hsv[2]);

		lightDiffuse[0] = (float) ((new Color(col)).getRed() / 255.0);
		lightDiffuse[1] = (float) ((new Color(col)).getGreen() / 255.0);
		lightDiffuse[2] = (float) ((new Color(col)).getBlue() / 255.0);

		hsv[1] = 0.005f;
		hsv[2] = 0.2f;
		col = Color.HSBtoRGB(hsv[0], hsv[1], hsv[2]);
		lightAmbient[0] = (float) ((new Color(col)).getRed() / 255.0);
		lightAmbient[1] = (float) ((new Color(col)).getGreen() / 255.0);
		lightAmbient[2] = (float) ((new Color(col)).getBlue() / 255.0);

		temp = BufferUtils.createFloatBuffer(4);
		for (int lcv = 0; lcv < lightPos.length; lcv++) {
			temp.put(lcv, lightDiffuse[lcv]);
		}
		GL11.glLight(GL11.GL_LIGHT0, GL11.GL_DIFFUSE, temp);

		temp = BufferUtils.createFloatBuffer(4);
		for (int lcv = 0; lcv < lightPos.length; lcv++) {
			temp.put(lcv, lightAmbient[lcv]);
		}
		GL11.glLight(GL11.GL_LIGHT0, GL11.GL_AMBIENT, temp);

		float xAxisComponent = 0;
		float zAxisComponent = 0;
		float jump = 0;

		if (selectedController != -1) {

			Controller con = Controllers.getController(selectedController);

			cameraYRot += dt / 10.0 * con.getAxisValue(1);
			cameraXRot -= dt / 10.0 * con.getAxisValue(0);
			force.put(0, con.getAxisValue(2));
			force.put(1, -con.getAxisValue(3));

			float angle = (float) (cameraYRot * Math.PI / 180.0);
			rotMat.put(0, 0, (float) Math.cos(angle));
			rotMat.put(0, 1, (float) -Math.sin(angle));
			rotMat.put(1, 0, (float) Math.sin(angle));
			rotMat.put(1, 1, (float) Math.cos(angle));
			force = rotMat.mmul(force);
			xAxisComponent = force.get(0);
			zAxisComponent = force.get(1);

			// jump = org.lwjgl.input.Controllers.getController(2).getPovX();
		} else {
			xAxisComponent = 0;
			zAxisComponent = 0;
			if (Keyboard.isKeyDown(Keyboard.KEY_W)) {
				xAxisComponent = -1;
			} else if (Keyboard.isKeyDown(Keyboard.KEY_S)) {
				xAxisComponent = 1;
			} else {
				xAxisComponent = 0;
			}

			if (Keyboard.isKeyDown(Keyboard.KEY_A)) {
				zAxisComponent = 1;
			} else if (Keyboard.isKeyDown(Keyboard.KEY_D)) {
				zAxisComponent = -1;
			} else {
				zAxisComponent = 0;
			}

			if (Keyboard.isKeyDown(Keyboard.KEY_SPACE)) {
				jump = 1;

			} else if (Keyboard.isKeyDown(Keyboard.KEY_C)) {
				jump = -1;
			} else if (Keyboard.isKeyDown(Keyboard.KEY_R)) {
				world.spawn(createCow(1, 10, 4, 7, -30, 50, 20, .3f));
			} else {
				jump = 0;
			}
		}

		if (Keyboard.isKeyDown(Keyboard.KEY_R)) {
			world.spawn(createCow(1, 10, 4, 7, -30, 50, 20, .3f));
		}

		if (cameraYRot > 360) {
			cameraYRot -= 360;
		}

		if (cameraYRot < 0) {
			cameraYRot += 360;
		}

		if (cameraXRot > 60) {
			cameraXRot = 60;
		}

		if (cameraXRot < 15) {
			cameraXRot = 15;
		}

		obj.getRigidBody().getWorldTransform(trans);

		camPos.put(0,
				(-trans.origin.x - 100 * -Math.sin(cameraYRot * Math.PI / 180)));
		camPos.put(1,
				-trans.origin.y - 150 * Math.sin(cameraXRot * Math.PI / 180));
		camPos.put(2,
				(-trans.origin.z - 100 * Math.cos(cameraYRot * Math.PI / 180)));

		camEuler.put(0, cameraXRot);
		camEuler.put(1, cameraYRot);
		camEuler.put(2, 0);

		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glPushMatrix();
		GL11.glLoadIdentity();
		GL11.glRotated(camEuler.get(0), 1, 0, 0);
		GL11.glRotated(camEuler.get(1), 0, 1, 0);
		GL11.glRotated(camEuler.get(2), 0, 0, 1);
		GL11.glTranslated(camPos.get(0), camPos.get(1), camPos.get(2));
		FloatBuffer buf = BufferUtils.createFloatBuffer(16);
		GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, buf);
		GL11.glPopMatrix();
		buf.get(dst);
		cam.setModelMatrix(dst);

		lightPos[0] = trans.origin.x
				+ (float) (30.0 * Math.cos(x * Math.PI / 180.0));
		lightPos[1] = trans.origin.y + 40f;
		lightPos[2] = trans.origin.z
				+ (float) (30.0 * Math.sin(x * Math.PI / 180.0));
		lightPos[3] = 1.0f;

		lightPos[0] = trans.origin.x;
		lightPos[2] = trans.origin.z;

		lightMarker.getRigidBody().getMotionState().getWorldTransform(trans);
		trans.origin.x = lightPos[0];
		trans.origin.y = lightPos[1];
		trans.origin.z = lightPos[2];
		lightMarker.getRigidBody().getMotionState().setWorldTransform(trans);

		float force = 3000;
		Vector3f torque = new Vector3f(force * xAxisComponent, 0, force
				* zAxisComponent);

		Vector3f jumpForce = new Vector3f(0, jump * 20, 0);
		obj.getRigidBody().applyTorque(torque);
		obj.getRigidBody().applyImpulse(jumpForce, new Vector3f(0, 0, 0));
		// obj.getRigidBody().applyCentralForce(torque);

	}

	float[] dst = new float[16];
	private PhysicsEntity<Light> lightMarker;

	public void physicsTick(long dt) {

		world.physicsTick(dt);
	}

	public void graphicsTick(long dt) {
		world.renderTick(dt);

		// swap buffers
		Display.update();
	}

	/**
	 * Get the time in milliseconds
	 * 
	 * @return The system time in milliseconds
	 */
	public long getTime() {
		return (Sys.getTime() * 1000) / Sys.getTimerResolution();
	}

	public PhysicsEntity<? extends RenderableMesh> createBox(int rn, float w,
			float h, float d, float x, float y, float z, float m) {
		Box floorBox = new Box("Box", rn, w, h, d);
		floorBox.setLocalOrigin(new FloatMatrix(new float[] { 0, 0, 0 }));

		CollisionShape groundShape = new BoxShape(new Vector3f(w, h, d));
		Transform groundTransform = new Transform();
		groundTransform.setIdentity();
		groundTransform.origin.set(new Vector3f(x, y, z));
		float mass = m;

		// rigidbody is dynamic if and only if mass is non zero,
		// otherwise static
		boolean isDynamic = (mass != 0f);

		Vector3f localInertia = new Vector3f(0, 0, 0);
		if (isDynamic) {
			groundShape.calculateLocalInertia(mass, localInertia);
		}

		// using motionstate is recommended, it provides interpolation
		// capabilities, and only synchronizes 'active' objects
		DefaultMotionState myMotionState = new DefaultMotionState(
				groundTransform);
		RigidBodyConstructionInfo rbInfo = new RigidBodyConstructionInfo(mass,
				myMotionState, groundShape, localInertia);
		rbInfo.friction = 1f;
		rbInfo.restitution = 0.2f;

		return new PhysicsEntity<Box>(floorBox, rbInfo);
	}

	public PhysicsEntity<? extends RenderableMesh> createCow(int rn, float w,
			float h, float d, float x, float y, float z, float mass) {
		Box floorBox = new Box("Box", rn, w, h, d);
		RenderableTriangleMesh mm = new RenderableTriangleMesh("Cow", cowMesh);
		floorBox.setLocalOrigin(new FloatMatrix(new float[] { 0, 0, 0 }));

		CollisionShape groundShape = new BoxShape(new Vector3f(w, h, d));
		Transform groundTransform = new Transform();
		groundTransform.setIdentity();
		groundTransform.origin.set(new Vector3f(x, y, z));

		// rigidbody is dynamic if and only if mass is non zero,
		// otherwise static
		boolean isDynamic = (mass != 0f);

		Vector3f localInertia = new Vector3f(0, 0, 0);
		if (isDynamic) {
			groundShape.calculateLocalInertia(mass, localInertia);
		}

		// using motionstate is recommended, it provides interpolation
		// capabilities, and only synchronizes 'active' objects
		DefaultMotionState myMotionState = new DefaultMotionState(
				groundTransform);
		RigidBodyConstructionInfo rbInfo = new RigidBodyConstructionInfo(mass,
				myMotionState, groundShape, localInertia);
		rbInfo.friction = 1f;
		rbInfo.restitution = 0.2f;

		return new PhysicsEntity<RenderableTriangleMesh>(mm, rbInfo);
	}

	public PhysicsEntity<? extends RenderableMesh> createPillar(float x,
			float y, float z) {
		RenderableTriangleMesh pillar = new RenderableTriangleMesh("Pillar",
				pillarMesh);
		pillar.setLocalOrigin(new FloatMatrix(new float[] { 0, 0, 0 }));

		CollisionShape collisionShape = new CylinderShape(
				new Vector3f(3, 20, 3));
		Transform shapeTransform = new Transform();
		shapeTransform.setIdentity();
		shapeTransform.origin.set(new Vector3f(x, y, z));
		float mass = 0;

		// rigidbody is dynamic if and only if mass is non zero,
		// otherwise static
		boolean isDynamic = (mass != 0f);

		Vector3f localInertia = new Vector3f(0, 0, 0);
		if (isDynamic) {
			collisionShape.calculateLocalInertia(mass, localInertia);
		}

		// using motionstate is recommended, it provides interpolation
		// capabilities, and only synchronizes 'active' objects
		DefaultMotionState myMotionState = new DefaultMotionState(
				shapeTransform);
		RigidBodyConstructionInfo rbInfo = new RigidBodyConstructionInfo(mass,
				myMotionState, collisionShape, localInertia);
		rbInfo.friction = 1f;
		rbInfo.restitution = 0.2f;

		return new PhysicsEntity<RenderableTriangleMesh>(pillar, rbInfo);
	}

	public void updateFPS() {
		if (getTime() - lastFPS > 1000) {
			Display.setTitle("FPS: " + fps);
			fps = 0; // reset the FPS counter
			lastFPS += 1000; // add one second
		}
		fps++;
	}

	public void start() {
		float size = 200;
		try {
			Display.setDisplayMode(new DisplayMode(800, 600));
			Display.create(new PixelFormat(8, 8, 8, 4));
			org.lwjgl.input.Controllers.create();
			while (org.lwjgl.input.Controllers.next())
				;
		} catch (LWJGLException e) {
			e.printStackTrace();
			System.exit(0);
		}
		world = new World();
		world.init();
		TriangleMesh mesh = ModelLoader.loadSTL("models/spherelow.stl", 1);
		cam = new Camera("Camera", 45.0f, 800f / 600f, 0.01f, 1000f);
		RenderableTriangleMesh sphere = new RenderableTriangleMesh(
				"Sphere Low", mesh);

		pillarMesh = ModelLoader.loadSTL("models/pillar.stl", 1);
		cowMesh = ModelLoader.loadSTL("models/cow.stl", 1);

		Light light = new Light("Point Light");

		floor = createBox(6, 400, 10, 400, 0, -20, 0, 0);
		{
			// create a dynamic rigidbody
			CollisionShape colShape = new SphereShape(0.1f);

			// Create Dynamic Objects
			Transform startTransform = new Transform();
			startTransform.setIdentity();

			float mass = 0f;

			// rigidbody is dynamic if and only if mass is non zero,
			// otherwise static
			boolean isDynamic = (mass != 0f);

			Vector3f localInertia = new Vector3f(0, 0, 0);
			if (isDynamic) {
				colShape.calculateLocalInertia(mass, localInertia);
			}

			startTransform.origin.set(new Vector3f(0, 300, 0));

			// using motionstate is recommended, it provides
			// interpolation capabilities, and only synchronizes
			// 'active' objects
			DefaultMotionState myMotionState = new DefaultMotionState(
					startTransform);

			RigidBodyConstructionInfo rbInfo = new RigidBodyConstructionInfo(
					mass, myMotionState, colShape, localInertia);
			rbInfo.friction = 1f;
			rbInfo.restitution = 2f;
			lightMarker = new PhysicsEntity<Light>(light, rbInfo);
		}

		world.spawn(lightMarker);

		{
			// create a dynamic rigidbody
			CollisionShape colShape = new SphereShape(5.f);

			// Create Dynamic Objects
			Transform startTransform = new Transform();
			startTransform.setIdentity();

			float mass = 10f;

			// rigidbody is dynamic if and only if mass is non zero,
			// otherwise static
			boolean isDynamic = (mass != 0f);

			Vector3f localInertia = new Vector3f(0, 0, 0);
			if (isDynamic) {
				colShape.calculateLocalInertia(mass, localInertia);
			}

			startTransform.origin.set(new Vector3f(0, 90, 0));

			// using motionstate is recommended, it provides
			// interpolation capabilities, and only synchronizes
			// 'active' objects
			DefaultMotionState myMotionState = new DefaultMotionState(
					startTransform);

			RigidBodyConstructionInfo rbInfo = new RigidBodyConstructionInfo(
					mass, myMotionState, colShape, localInertia);
			rbInfo.friction = 1f;
			rbInfo.restitution = 2f;
			obj = new PhysicsEntity<RenderableTriangleMesh>(sphere, rbInfo);
		}

		world.spawn(obj);
		world.spawn(floor);
		world.setCamera(cam);

		int num = 5;
		for (int lc = -num; lc < num; lc++) {
			for (int lcv = -num; lcv < num; lcv++) {
				world.spawn(createPillar(lc * 40,
						(float) (-20 + 35 * Math.random()), lcv * 40));
			}
		}
		long lastTime = getTime();
		org.lwjgl.input.Controllers.clearEvents();

		getController();
		while (!Display.isCloseRequested()) {
			long curTime = getTime();
			long delta = curTime - lastTime;
			lastTime = curTime;

			gameLogicTick(delta);

			physicsTick(delta);

			graphicsTick(delta);

			updateFPS();
			Display.sync(maxFrameRate);

			d.checkOpenGLError("main ");
		}

		Display.destroy();
	}

	private void getController() {
		if (Controllers.getControllerCount() > 0) {
			System.out.println("Choose a controller");
			int num = Controllers.getControllerCount();
			System.out.println(-1 + ": Keyboard");
			for (int lcv = 0; lcv < num; lcv++) {
				System.out.println(lcv + ": "
						+ Controllers.getController(lcv).getName());
				if (Controllers.getController(lcv).getName().equals("Logitech Dual Action"))
					selectedController = lcv;
			}

			Scanner scanner = new Scanner(System.in);

			//selectedController = scanner.nextInt();
		} else {
			System.out.println("No controllers detected, using keyboard");
		}
	}

	public static void main(String[] argv) {
		Main displayExample = new Main();
		displayExample.start();

	}

}
