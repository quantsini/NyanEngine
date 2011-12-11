package debug;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;


public class HelperDebug {


	/**
	 */
	int lastErr = -1;

	public void checkOpenGLError(String msg) {
		int err;
		err = GL11.glGetError();
		if (lastErr != err) {
			if (err != 0) {
				System.out.println(msg + "OpenGL Error: " + GLU.gluErrorString(err));
				
				// throw new RuntimeException("OpenGL Error: "
				// + GLU.gluErrorString(err));

			} else {
				System.out.println(msg + "No error");
			}
		}
		lastErr = err;
	}
}
