package com.opentrack.jonasfuhrmann.opentrackplanner.Track;

import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Vector3;

public class TrackEdge {

    private static final float EPSILON = 0.03f; // TODO: change to good value

    private Vector3 normalOrig;
    private Vector3 normDir;

    public TrackEdge(Vector3 normalOrigin, Vector3 normalEnd) {
        normalOrig = normalOrigin;
        normDir = Vector3.subtract(normalEnd, normalOrig).normalized();
    }

    public static boolean checkInterval(TrackEdge edge1, Node node1, TrackEdge edge2, Node node2) {
        // translate to world coordinate system
        Vector3 normDir1 = node1.localToWorldDirection(edge1.normDir);
        Vector3 normDir2 = node2.localToWorldDirection(edge2.normDir);

        // check normal directions
        if(Vector3.dot(normDir1, normDir2) >= 0) {
            // track edges don't point against each other
            return  false;
        }

        // translate to world coordinate system
        Vector3 origin1 = node1.localToWorldPoint(edge1.normalOrig);
        Vector3 origin2 = node2.localToWorldPoint(edge2.normalOrig);

        // non-normalized direction vector of the origins of the edges
        Vector3 distanceDir = Vector3.subtract(origin2, origin1);
        float distance = distanceDir.length();

        // check if points are close enough
        return distance < EPSILON;
    }
}
