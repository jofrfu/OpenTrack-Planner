package com.opentrack.jonasfuhrmann.opentrackplanner;

import com.google.ar.sceneform.math.Vector3;

public class VectorHelper {
    public static Vector3 floatToVec(float[] vector) {
        Vector3 vec = new Vector3();
        vec.x = vector[0];
        vec.y = vector[1];
        vec.z = vector[2];
        return vec;
    }

    public static float[] vecToFloat(Vector3 vector) {
        return new float[]{vector.x, vector.y, vector.z};
    }
}
