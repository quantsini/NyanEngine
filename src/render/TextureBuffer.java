package render;

import java.nio.FloatBuffer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30;
import org.newdawn.slick.opengl.Texture;

import debug.HelperDebug;

public class TextureBuffer {
	private HelperDebug d = new HelperDebug();
	private int texID;
	private int boundTextureLocation;
	private int format;
	private int dataType;
	private int width;
	private int height;
	private static int boundTexturePointer = 0;
	private static int MAX_TEXTURE_BIND_POINTS = 30;

	public TextureBuffer(int sizew, int sizeh, int f, int d, boolean mipMaps,
			FloatBuffer data) {
		width = sizew;
		height = sizeh;
		format = f;
		dataType = d;
		texID = GL11.glGenTextures();
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texID);
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, 4, sizew, sizeh, 0, format,
				dataType, data);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S,
				GL11.GL_REPEAT);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T,
				GL11.GL_REPEAT);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER,
				GL11.GL_LINEAR);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER,
				GL11.GL_LINEAR);
		if (mipMaps) {
			GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);
		}

		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

		boundTextureLocation = -1;
	}

	public TextureBuffer(Texture tex, boolean mipMaps) {
		width = tex.getTextureWidth();
		height = tex.getTextureHeight();
		format = GL11.GL_RGB;
		dataType = GL11.GL_UNSIGNED_BYTE;
		texID = tex.getTextureID();
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texID);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S,
				GL11.GL_REPEAT);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T,
				GL11.GL_REPEAT);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER,
				GL11.GL_LINEAR);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER,
				GL11.GL_LINEAR);
		if (mipMaps) {
			GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);
		}

		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
	}

	public void copyPixels() {
		bindTexturePoint();

		GL11.glCopyTexImage2D(GL11.GL_TEXTURE_2D, 0, format, 0, 0, width,
				height, 0);
	}

	public int bindTexturePoint() {
		if (boundTextureLocation == -1) {
			boundTextureLocation = getNextAvailBindPoint();
			if (boundTextureLocation == -1) {
				throw new RuntimeException("oops no more texture bind points");
			}
		}

		GL13.glActiveTexture(GL13.GL_TEXTURE0 + boundTextureLocation);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texID);
		return boundTextureLocation;
	}

	public int getTextureBindPoint() {
		return boundTextureLocation;
	}

	public boolean isBound() {
		return boundTextureLocation != -1;
	}

	public void releaseTextureLocation() {
		if (boundTextureLocation != -1) {
			removeBindPoint(boundTextureLocation);
		}

		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

		boundTextureLocation = -1;
	}

	private static int getNextAvailBindPoint() {
		if (boundTexturePointer == MAX_TEXTURE_BIND_POINTS) {
			return -1;
		}

		boundTexturePointer++;
		return boundTexturePointer - 1;
	}

	private static void removeBindPoint(int bindPoint) {
		boundTexturePointer--;
	}
}
