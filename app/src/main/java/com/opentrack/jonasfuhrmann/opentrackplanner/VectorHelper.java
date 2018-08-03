package com.opentrack.jonasfuhrmann.opentrackplanner;

import com.google.ar.sceneform.math.MathHelper;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;

/**
 * A simple helper class to simplify vector operations and conversions.
 */
public class VectorHelper {

    /**
     * Creates a {@link Vector3} representation from a float array representation.
     * @param vector float vector
     * @return {@link Vector3} vector
     */
    public static Vector3 floatToVec(float[] vector) {
        Vector3 vec = new Vector3();
        vec.x = vector[0];
        vec.y = vector[1];
        vec.z = vector[2];
        return vec;
    }

    /**
     * Creates a float array representation from a {@link Vector3} representation.
     * @param vector {@link Vector3} vector
     * @return float vector
     */
    public static float[] vecToFloat(Vector3 vector) {
        return new float[]{vector.x, vector.y, vector.z};
    }

    /**
     * Creates a {@link Quaternion} representation from a float array representation.
     * @param quaternion float quaternion
     * @return {@link Quaternion} quaternion
     */
    public static Quaternion floatToQuat(float[] quaternion) {
        Quaternion quat = new Quaternion();
        quat.x = quaternion[0];
        quat.y = quaternion[0];
        quat.z = quaternion[0];
        quat.w = quaternion[0];
        return quat;
    }

    /**
     * Creates a float array representation from a {@link Quaternion} representation.
     * @param quaternion {@link Quaternion} quaternion
     * @return float quaternion
     */
    public static float[] quatToFloat(Quaternion quaternion) {
        return new float[]{quaternion.x, quaternion.y, quaternion.z, quaternion.w};
    }

    /**
     * Checks if two vectors are almost equal.
     * @param vector3 First vector
     * @param vector31 second vector
     * @return true, if almost equal
     */
    public static boolean almostEquals(Vector3 vector3, Vector3 vector31) {
        return  MathHelper.almostEqualRelativeAndAbs(vector3.x, vector31.x) &&
                MathHelper.almostEqualRelativeAndAbs(vector3.y, vector31.y) &&
                MathHelper.almostEqualRelativeAndAbs(vector3.z, vector31.z);
    }
}
