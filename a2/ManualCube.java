package a2;

import tage.*;
import tage.shapes.ManualObject;

public class ManualCube extends ManualObject{
    private float[] vertices = new float[]
    {   //36 vertices
        -1f, 1f, 1f,    1f, 1f, 1f,     -1f, -1f, 1f, 
        -1f, -1f, 1f,   1f, -1f, 1f,    1f, 1f, 1f, //Front
        1f, -1f, 1f,    1f, 1f, 1f,     1f, 1f, -1f, 
        1f, -1f, 1f,    1f, -1f, -1f,   1f, 1f, -1f, //Right Side
        -1f, 1f, 1f,    -1f, 1f, -1f,   1f, 1f, 1f, 
        1f, 1f, 1f,     1f, 1f, -1f,    -1f, 1f, -1f, //Top
        -1f, -1f, -1f,  -1f, 1f, -1f,   -1f, 1f, 1f, 
        -1f, -1f, -1f,  -1f, -1f, 1f,   -1f, 1f, 1f, //Left Side
        -1f, -1f, -1f,  1f, -1f, -1f,   1f, -1f, 1f, 
        -1f, -1f, -1f,  -1f, -1f, 1f,   1f, -1f, 1f, //Bottom
        -1f, 1f, -1f,   1f, 1f, -1f,    1f, -1f, -1f, 
        -1f, 1f, -1f,   -1f, -1f, -1f,  1f, -1f, -1f //Back Side
    };

    private float[] texCoords = new float[]
    {
        0f, 1f,     1f, 1f,     0f, 0f,
        0f, 0f,     1f, 0f,     1f, 1f, //Front
        0f, 0f,     0f, 0f,     0f, 0f,
        0f, 0f,     0f, 0f,     0f, 0f, //Right Side
        0f, 0f,     0f, 0f,     0f, 0f,
        0f, 0f,     0f, 0f,     0f, 0f, //Top
        0f, 0f,     0f, 0f,     0f, 0f, 
        0f, 0f,     0f, 0f,     0f, 0f, //Left Side
        0f, 0f,     0f, 0f,     0f, 0f,
        0f, 0f,     0f, 0f,     0f, 0f, //Bottom
        0f, 0f,     0f, 0f,     0f, 0f, 
        0f, 0f,     0f, 0f,     0f, 0f //Back Side

    };

    private float[] normals = new float[]
    {
        0f, 0f, 1f,     0f, 0f, 1f,     0f, 0f, 1f, 
        0f, 0f, 1f,     0f, 0f, 1f,     0f, 0f, 1f, //Front
        1f, 0f, 0f,     1f, 0f, 0f,     1f, 0f, 0f, 
        1f, 0f, 0f,     1f, 0f, 0f,     1f, 0f, 0f, //Right Side
        0f, 1f, 0f,     0f, 1f, 0f,     0f, 1f, 0f, 
        0f, 1f, 0f,     0f, 1f, 0f,     0f, 1f, 0f, //Top
        -1f, 0f, 0f,    -1f, 0f, 0f,    -1f, 0f, 0f, 
        -1f, 0f, 0f,    -1f, 0f, 0f,    -1f, 0f, 0f, //Left Side
        0f, -1f, 0f,    0f, -1f, 0f,    0f, -1f, 0f, 
        0f, -1f, 0f,    0f, -1f, 0f,    0f, -1f, 0f, //Bottom
        0f, 0f, -1f,     0f, 0f, -1f,     0f, 0f, -1f, 
        0f, 0f, -1f,     0f, 0f, -1f,     0f, 0f, -1f //Back Side
    };
    
    public ManualCube(){
        super();
        setNumVertices(36);
        setVertices(vertices);
        setTexCoords(texCoords);
        setNormals(normals);

        setMatAmb((new float [] {1f,  0.1275f, 0.0540f, 1} ));
        setMatDif((new float [] {1f,  0.1275f, 0.0540f, 1} ));
        setMatSpe((new float [] {1f,  0.1275f, 0.0540f, 1} ));
        setMatShi(25.6f);
    }
}