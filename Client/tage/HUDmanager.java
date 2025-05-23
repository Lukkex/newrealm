package tage;
import static com.jogamp.opengl.GL4.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.gl2.GLUT;
import org.joml.*;

/**
* Manages up to two HUD strings, implemented as GLUT strings.
* This class is instantiated automatically by the engine.
* Note that this class utilizes deprectated OpenGL functionality.
* <p>
* The available fonts are:
* <ul>
* <li> GLUT.BITMAP_8_BY_13
* <li> GLUT.BITMAP_9_BY_15
* <li> GLUT.BITMAP_TIMES_ROMAN_10
* <li> GLUT.BITMAP_TIMES_ROMAN_24
* <li> GLUT.BITMAP_HELVETICA_10
* <li> GLUT.BITMAP_HELVETICA_12
* <li> GLUT.BITMAP_HELVETICA_18
* </ul>
* @author Scott Gordon
*/

public class HUDmanager
{	private GLCanvas myCanvas;
	private GLUT glut = new GLUT();
	private Engine engine;

	private TextureImage BGImage;
	private String HUD1string, HUD2string;
	private float[] HUD1color, HUD2color, BGvertices;
	private int HUD1font = GLUT.BITMAP_TIMES_ROMAN_24;
	private int HUD2font = GLUT.BITMAP_TIMES_ROMAN_24;
	private int HUD1x, HUD1y, HUD2x, HUD2y;

	// The constructor is called by the engine, and should not be called by the game application.
	// It initializes the two HUDs to empty strings.

	protected HUDmanager(Engine e)
	{	engine = e;
		HUD1string = "";
		HUD2string = "";
		HUD1color = new float[3];
		HUD2color = new float[3];
		setupVertices();
	}
	
	protected void setGLcanvas(GLCanvas g) { myCanvas = g; }

	protected void drawHUDs()
	{	GL4 gl4 = (GL4) GLContext.getCurrentGL();
		GL4bc gl4bc = (GL4bc) gl4;

		gl4.glUseProgram(0);

		//BG Image
		

		gl4bc.glColor3f(HUD1color[0], HUD1color[1], HUD1color[2]);
		gl4bc.glWindowPos2d (HUD1x, HUD1y);
		glut.glutBitmapString(HUD1font, HUD1string);

		gl4bc.glColor3f(HUD2color[0], HUD2color[1], HUD2color[2]);
		gl4bc.glWindowPos2d (HUD2x, HUD2y);
		glut.glutBitmapString (HUD2font, HUD2string);
	}

	/** sets HUD #1 to the specified text string, color, and location */
	public void setHUD1(String string, Vector3f color, int x, int y)
	{	HUD1string = string;
		HUD1color[0]=color.x(); HUD1color[1]=color.y(); HUD1color[2]=color.z();
		HUD1x = x;
		HUD1y = y;
	}

	/** sets HUD #2 to the specified text string, color, and location */
	public void setHUD2(String string, Vector3f color, int x, int y)
	{	HUD2string = string;
		HUD2color[0]=color.x(); HUD2color[1]=color.y(); HUD2color[2]=color.z();
		HUD2x = x;
		HUD2y = y;
	}

	/** sets HUD #1 font - available fonts are listed above. */
	public void setHUD1font(int font) { HUD1font = font; }

	/** sets HUD #2 font - available fonts are listed above. */
	public void setHUD2font(int font) { HUD2font = font; }

	/** Sets the BG image for the HUD (transparent by default) */
	public void setBGImage(TextureImage BG){
		BGImage = BG;
	}

	private void setupVertices(){
		BGvertices = new float[]{
			-((engine.getRenderSystem()).getWidth())/2.0f, -((engine.getRenderSystem()).getHeight())/2.0f, 0.0f, 
			((engine.getRenderSystem()).getWidth())/2.0f, ((engine.getRenderSystem()).getHeight())/2.0f, 0.0f
		};
	}
}