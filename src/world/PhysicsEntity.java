package world;

import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.linearmath.Transform;

public class PhysicsEntity<WE extends RenderEntity> {
	private WE theEntity;
	private RigidBody bodyEntity;
	float[] m = new float[16];
	Transform trans = new Transform();

	public PhysicsEntity(WE ent, RigidBodyConstructionInfo rbInfo) {
		theEntity = ent;

		bodyEntity = null;

		if (rbInfo != null) {
			bodyEntity = new RigidBody(rbInfo);
		}
	}

	public void updateOpenGLState() {
		
		if (bodyEntity != null) {
			bodyEntity.getMotionState().getWorldTransform(trans);
			trans.getOpenGLMatrix(m);
			theEntity.setModelMatrix(m);
		}
		
	}

	public WE getWorldEntity() {
		return theEntity;
	}
	
	public RigidBody getRigidBody() {
		return bodyEntity;
	}
}
