#version 120

varying float x_coord;
varying float y_coord;
varying float z_coord;

void main(){
    gl_TexCoord[0] = gl_MultiTexCoord0;
    gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;
    x_coord = gl_Position.x;
    y_coord = gl_Position.y;
    z_coord = gl_Position.z;
}