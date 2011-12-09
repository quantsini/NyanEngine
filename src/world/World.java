package world;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import javax.vecmath.Vector3f;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL30;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;
import org.newdawn.slick.util.ResourceLoader;

import render.ShaderContext;
import render.ShaderProgram;
import render.TextureBuffer;

import com.bulletphysics.collision.broadphase.AxisSweep3;
import com.bulletphysics.collision.broadphase.BroadphaseInterface;
import com.bulletphysics.collision.broadphase.Dispatcher;
import com.bulletphysics.collision.dispatch.CollisionConfiguration;
import com.bulletphysics.collision.dispatch.CollisionDispatcher;
import com.bulletphysics.collision.dispatch.DefaultCollisionConfiguration;
import com.bulletphysics.dynamics.DiscreteDynamicsWorld;
import com.bulletphysics.dynamics.DynamicsWorld;
import com.bulletphysics.dynamics.constraintsolver.ConstraintSolver;
import com.bulletphysics.dynamics.constraintsolver.SequentialImpulseConstraintSolver;

import debug.HelperDebug;

public class World {
	private ArrayList<PhysicsEntity<? extends RenderEntity>> entities;
	private Camera currentCam;
	private CollisionConfiguration collisionConfiguration;
	private Dispatcher dispatcher;
	private DynamicsWorld dynamicsWorld;

	private ShaderContext phongShadowContext;
	private ShaderContext volumeShadowContext;
	private ShaderContext dofContext;
	private ShaderContext passThruContext;
	private ShaderContext screenSpaceNormalDepthContext;
	private ShaderContext screenContext;
	private ShaderContext blurContext;
	private ShaderContext gammaContext;

	private TextureBuffer screenBuffer;
	private TextureBuffer depthBuffer;
	private TextureBuffer normalBuffer;
	private TextureBuffer tempMarble;
	private TextureBuffer blurBuffer;
	long time = 0;

	private HelperDebug debug = new HelperDebug();
	private TextureBuffer dofBuffer;

	public World() {
		entities = new ArrayList<PhysicsEntity<? extends RenderEntity>>();
		currentCam = null;
	}

	public void initPhysics() {
		collisionConfiguration = new DefaultCollisionConfiguration();
		dispatcher = new CollisionDispatcher(collisionConfiguration);

		Vector3f worldAabbMin = new Vector3f(-10000, -10000, -10000);
		Vector3f worldAabbMax = new Vector3f(10000, 10000, 10000);
		int maxProxies = 1024;
		BroadphaseInterface overlappingPairCache = new AxisSweep3(worldAabbMin,
				worldAabbMax, maxProxies);

		ConstraintSolver solver = new SequentialImpulseConstraintSolver();
		dynamicsWorld = new DiscreteDynamicsWorld(dispatcher,
				overlappingPairCache, solver, collisionConfiguration);

		dynamicsWorld.setGravity(new Vector3f(0.0f, -98f, 0.0f));
	}

	public void init() {
		initPhysics();
		initGL();
	}

	public void initGL() {
		GL11.glViewport(0, 0, 800, 600);
		GL11.glClearColor(0.1f, 0.18f, 0.21f, 1.0f);
		GL11.glClearDepth(1.0);
		GL11.glClearStencil(0);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glDepthFunc(GL11.GL_LEQUAL);
		GL11.glCullFace(GL11.GL_BACK);
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glHint(GL11.GL_PERSPECTIVE_CORRECTION_HINT, GL11.GL_NICEST);
		GL11.glEnable(GL11.GL_DITHER);

		Texture texture = null;
		try {
			texture = TextureLoader.getTexture("JPG",
					ResourceLoader.getResourceAsStream("textures/marble.jpg"));
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		phongShadowContext = new ShaderContext("Phong Shader",
				ShaderProgram.getPhongShader());

		volumeShadowContext = new ShaderContext("Volume Shadow Shader",
				ShaderProgram.getVolumeShadowShader());

		blurContext = new ShaderContext("Blur Screen Shader",
				ShaderProgram.getBlurShader());

		dofContext = new ShaderContext("DOF Shader",
				ShaderProgram.getDOFShader());

		passThruContext = new ShaderContext("Pass Thru Shader",
				ShaderProgram.getPassThruShader());

		gammaContext = new ShaderContext("Gamma Shader",
				ShaderProgram.getGammaShader());

		screenSpaceNormalDepthContext = new ShaderContext(
				"Screen Space Normal Shader",
				ShaderProgram.getScreenSpaceNormalDepthShader());

		screenContext = new ShaderContext("Screen Blend Shader",
				ShaderProgram.getScreenBlendShader());

		tempMarble = new TextureBuffer(texture, true);
		screenBuffer = new TextureBuffer(800, 600, GL11.GL_RGBA,
				GL11.GL_UNSIGNED_BYTE, false, (FloatBuffer) null);
		blurBuffer = new TextureBuffer(800, 600, GL11.GL_RGBA,
				GL11.GL_UNSIGNED_BYTE, false, (FloatBuffer) null);
		depthBuffer = new TextureBuffer(800, 600, GL11.GL_DEPTH_COMPONENT,
				GL11.GL_UNSIGNED_BYTE, false, (FloatBuffer) null);

		normalBuffer = new TextureBuffer(800, 600, GL11.GL_RGBA,
				GL11.GL_UNSIGNED_BYTE, false, (FloatBuffer) null);

		dofBuffer = new TextureBuffer(800, 600, GL11.GL_RGBA,
				GL11.GL_UNSIGNED_BYTE, false, (FloatBuffer) null);

		screenBuffer.bindTexturePoint();
		depthBuffer.bindTexturePoint();
		normalBuffer.bindTexturePoint();
		tempMarble.bindTexturePoint();
		blurBuffer.bindTexturePoint();
	}

	public void spawn(PhysicsEntity<? extends RenderEntity> o) {
		entities.add(o);
		if (o.getWorldEntity().spawned()) {
			dynamicsWorld.addRigidBody(o.getRigidBody());
		}
	}

	public void physicsTick(long dtMilli) {
		float dt = dtMilli / 1000.0f;

		dynamicsWorld.stepSimulation(dt, 7);

		// update the opengl state for each object
		for (PhysicsEntity<? extends RenderEntity> e : entities) {
			if (!e.getWorldEntity().spawned()) {
				dynamicsWorld.removeRigidBody(e.getRigidBody());
			}
			e.updateOpenGLState();
		}
	}

	public void setCamera(Camera cam) {
		currentCam = cam;
	}

	private void renderToDepthmap() {
		// RENDER ALL GEOMETRY TO GENERATE A DEPTH MAP
		GL11.glColorMask(false, false, false, false);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT
				| GL11.GL_STENCIL_BUFFER_BIT);
		for (PhysicsEntity<? extends RenderEntity> e : entities) {
			RenderEntity re = e.getWorldEntity();
			re.render(passThruContext);
		}

		depthBuffer.copyPixels();
	}

	private void renderToNormalMap() {
		// RENDER ALL GEOMETRY TO GENERATE A SCREEN SPACE NORMAL MAP
		GL11.glColorMask(true, true, true, true);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT
				| GL11.GL_STENCIL_BUFFER_BIT);

		for (PhysicsEntity<? extends RenderEntity> e : entities) {
			RenderEntity re = e.getWorldEntity();
			re.render(screenSpaceNormalDepthContext);
		}

		normalBuffer.copyPixels();
	}

	private void generateShadowVolumeStencil() {
		// PUT INTO THE DEPTH BUFFER ALL OBJECTS THAT RECEIVE SHADOWS
		GL11.glColorMask(false, false, false, false);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT
				| GL11.GL_STENCIL_BUFFER_BIT);
		for (PhysicsEntity<? extends RenderEntity> e : entities) {
			RenderEntity re = e.getWorldEntity();
			re.render(passThruContext);
		}

		// PERFORM STENCIL TEST OF OBJECTS THAT CAST SHADOWS
		GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);
		GL11.glEnable(GL11.GL_STENCIL_TEST);
		GL11.glStencilFunc(GL11.GL_ALWAYS, 1, 0xff);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glDepthFunc(GL11.GL_LESS);
		GL11.glDepthMask(false);
		GL11.glEnable(GL11.GL_CULL_FACE);

		// pass 1 frontface culling
		GL11.glCullFace(GL11.GL_BACK);
		GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_INCR);

		for (PhysicsEntity<? extends RenderEntity> e : entities) {
			RenderEntity re = e.getWorldEntity();
			if (re.castShadows) {
				re.render(volumeShadowContext);
			}
		}

		// pass 2 backface culling
		GL11.glCullFace(GL11.GL_FRONT);
		GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_DECR);
		for (PhysicsEntity<? extends RenderEntity> e : entities) {
			RenderEntity re = e.getWorldEntity();
			if (re.castShadows) {
				re.render(volumeShadowContext);
			}
		}
	}

	private void finalRenderToTexture() {
		// NEXT RENDER THE ACTUAL ITEMS - AMBIENT, THEN DIFFUSE
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		GL11.glCullFace(GL11.GL_BACK);
		GL11.glColorMask(true, true, true, true);
		GL11.glDepthFunc(GL11.GL_LEQUAL);
		GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
		GL11.glDepthMask(false);
		GL11.glEnable(GL11.GL_STENCIL_TEST);

		// ambient pass
		GL11.glStencilFunc(GL11.GL_NOTEQUAL, 0, 0xff);
		phongShadowContext.uniform1f("ambientOnly", 1);
		for (PhysicsEntity<? extends RenderEntity> e : entities) {
			RenderEntity re = e.getWorldEntity();
			re.render(phongShadowContext);
		}

		// diffuse pass
		GL11.glStencilFunc(GL11.GL_EQUAL, 0, 0xff);
		phongShadowContext.uniform1f("ambientOnly", 0);
		for (PhysicsEntity<? extends RenderEntity> e : entities) {
			RenderEntity re = e.getWorldEntity();
			re.render(phongShadowContext);
		}

		GL11.glDisable(GL11.GL_BLEND);
		GL11.glDepthFunc(GL11.GL_LEQUAL);
		GL11.glDepthMask(true);
		GL11.glDisable(GL11.GL_STENCIL_TEST);

		// SCREEN POST PROCESSING
		screenBuffer.copyPixels();
	}

	private void postprocessScreen(TextureBuffer screenBuff,
			TextureBuffer depthBuff, TextureBuffer normalBuff, ShaderContext sc) {
		// render to screen
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT
				| GL11.GL_STENCIL_BUFFER_BIT);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glDisable(GL11.GL_STENCIL_TEST);
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glPushMatrix();
		GL11.glLoadIdentity();
		GL11.glOrtho(0, 800, 600, 0, -1, 1);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glPushMatrix();
		GL11.glLoadIdentity();

		screenBuff.bindTexturePoint();
		sc.uniform1i("sceneMap", screenBuff.getTextureBindPoint());
		sc.uniform1i("depthMap", depthBuff.getTextureBindPoint());
		sc.uniform1i("normalDepthMap", normalBuff.getTextureBindPoint());
		sc.bind();
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glColor4f(1, 1, 1, 1);
		GL11.glTexCoord2f(0, 1);
		GL11.glVertex2f(0, 0);

		GL11.glTexCoord2f(0, 0);
		GL11.glVertex2f(0, 600);

		GL11.glTexCoord2f(1, 0);
		GL11.glVertex2f(800, 600);

		GL11.glTexCoord2f(1, 1);
		GL11.glVertex2f(800, 0);
		GL11.glEnd();
		sc.unbindShaders();

		GL11.glPopMatrix();
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glPopMatrix();

		GL11.glEnable(GL11.GL_STENCIL_TEST);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
	}

	private void drawPostProcessing() {
		dofContext.uniform1f("focus", 0.16f);
		// dofContext.uniform1f("focus", (float)(0.15f * Math.sin(time/300.0)));
		dofContext.uniform1f("blurclamp", 0.015f);
		dofContext.uniform1f("bias", 0.05f);
		dofContext.uniform1f("totStrength", 1.38f);
		dofContext.uniform1f("strength", 0.07f);
		dofContext.uniform1f("offset", 18.0f);
		dofContext.uniform1f("falloff", 0.2f);
		dofContext.uniform1f("rad", 0.006f);

		postprocessScreen(screenBuffer, depthBuffer, normalBuffer, dofContext);
		dofBuffer.copyPixels();

		blurContext.uniform1f("blurSize", 5);
		postprocessScreen(dofBuffer, depthBuffer, normalBuffer, blurContext);
		blurBuffer.copyPixels();

		screenContext.uniform1f("blend", 0.75f);
		postprocessScreen(dofBuffer, blurBuffer, normalBuffer, screenContext);
		screenBuffer.copyPixels();

		gammaContext.uniform1f("gamma", 1.2f);
		postprocessScreen(dofBuffer, depthBuffer, normalBuffer, gammaContext);
	}

	public void renderTick(long dtMilli) {
		time += dtMilli;
		if (currentCam == null) {
			throw new RuntimeException("Camera cannot be null!");
		}

		currentCam.commitCamera();

		// set the camera for the shaders
		phongShadowContext.setCameraMatrix(currentCam);
		volumeShadowContext.setCameraMatrix(currentCam);
		passThruContext.setCameraMatrix(currentCam);
		screenSpaceNormalDepthContext.setCameraMatrix(currentCam);

		// update the time for each shader
		phongShadowContext.updateTime(time);
		volumeShadowContext.updateTime(time);
		dofContext.updateTime(time);
		passThruContext.updateTime(time);
		screenSpaceNormalDepthContext.updateTime(time);

		int dbBindPoint = depthBuffer.getTextureBindPoint();
		int nmBindPoint = normalBuffer.getTextureBindPoint();
		int diffuseMapTemp = tempMarble.getTextureBindPoint();

		// set the texture for the depth
		phongShadowContext.uniform1i("depthMap", dbBindPoint);
		volumeShadowContext.uniform1i("depthMap", dbBindPoint);
		dofContext.uniform1i("depthMap", dbBindPoint);
		screenSpaceNormalDepthContext.uniform1f("depthMap", dbBindPoint);

		// set the texture for the normalmap
		phongShadowContext.uniform1i("normalDepthMap", nmBindPoint);
		volumeShadowContext.uniform1i("normalDepthMap", nmBindPoint);
		dofContext.uniform1i("normalDepthMap", nmBindPoint);

		// START RENDER
		renderToDepthmap();

		renderToNormalMap();

		generateShadowVolumeStencil();

		tempMarble.bindTexturePoint();
		// TODO: abstract this into a material
		phongShadowContext.uniform1i("diffuseMap", diffuseMapTemp);
		finalRenderToTexture();

		drawPostProcessing();

	}
}
