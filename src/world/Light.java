package world;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import render.ShaderContext;

public class Light extends RenderEntity {

	public Light(String n) {
		super(n);
	}

	@Override
	public boolean render(ShaderContext shaderContext) {
		GL11.glColor3f(0, 1, 0);
		GL11.glDisable(GL11.GL_CULL_FACE);
		GL11.glPushMatrix();
		glTransform();

		GL11.glLineWidth(0.5f);
		GL11.glBegin(GL11.GL_LINES);
		float bigness = 1.5f;
		GL11.glVertex3f(-bigness, 0, 0);
		GL11.glVertex3f(bigness, 0, 0);

		GL11.glVertex3f(0, -bigness, 0);
		GL11.glVertex3f(0, bigness, 0);

		GL11.glVertex3f(0, 0, -bigness);
		GL11.glVertex3f(0, 0, bigness);
		GL11.glEnd();
		GL11.glPopMatrix();
		GL11.glEnable(GL11.GL_CULL_FACE);
		return false;
	}
}
