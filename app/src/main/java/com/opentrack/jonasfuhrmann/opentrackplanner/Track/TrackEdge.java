package com.opentrack.jonasfuhrmann.opentrackplanner.Track;

import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;

public class TrackEdge {

    private static final float EPSILON = 0.03f; // TODO: change to good value

    private Vector3 localNormalOrig;
    private Vector3 localNormDir;

    private Vector3 normalOrig;
    private Vector3 normDir;

    public TrackEdge(Vector3 normalOrigin, Vector3 normalEnd) {
        normalOrig = normalOrigin;
        normDir = Vector3.subtract(normalEnd, normalOrig).normalized();
        localNormalOrig = normalOrig;
        localNormDir = normDir;
    }

    public void transform(Vector3 worldPosition, Quaternion worldRotation) {
        normalOrig = Quaternion.rotateVector(worldRotation, localNormalOrig);
        normDir = Quaternion.rotateVector(worldRotation, localNormDir);

        normalOrig = Vector3.add(normalOrig, worldPosition);
    }

    public static boolean checkConnection(TrackEdge edge1, TrackEdge edge2) {
        // check normal directions
        if(Vector3.dot(edge1.normDir, edge2.normDir) >= 0) {
            // track edges don't point against each other
            return  false;
        }

        // non-normalized direction vector of the origins of the edges
        Vector3 distanceDir = Vector3.subtract(edge2.normalOrig, edge1.normalOrig);
        float distance = distanceDir.length();

        // check if points are close enough
        return distance < EPSILON;
    }
}
