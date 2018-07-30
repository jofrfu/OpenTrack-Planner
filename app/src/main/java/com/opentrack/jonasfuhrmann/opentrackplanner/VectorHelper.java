package com.opentrack.jonasfuhrmann.opentrackplanner;

import com.google.ar.sceneform.math.MathHelper;
import com.google.ar.sceneform.math.Quaternion;
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

    public static Quaternion floatToQuat(float[] quaternion) {
        Quaternion quat = new Quaternion();
        quat.x = quaternion[0];
        quat.y = quaternion[0];
        quat.z = quaternion[0];
        quat.w = quaternion[0];
        return quat;
    }

    public static float[] quatToFloat(Quaternion quaternion) {
        return new float[]{quaternion.x, quaternion.y, quaternion.z, quaternion.w};
    }

    public static boolean almostEquals(Vector3 vector3, Vector3 vector31) {
        return  MathHelper.almostEqualRelativeAndAbs(vector3.x, vector31.x) &&
                MathHelper.almostEqualRelativeAndAbs(vector3.y, vector31.y) &&
                MathHelper.almostEqualRelativeAndAbs(vector3.z, vector31.z);
    }
}
