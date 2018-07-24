package com.opentrack.jonasfuhrmann.opentrackplanner.Track;

import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;

public class TrackEdge {

    private static final float EPSILON = 0.04f; // TODO: change to good value

    // For mm to m conversion
    private static final float SCALE = 0.001f;
    private static final TrackEdge[][] EDGES = {
            {       // Straight track edges
                    new TrackEdge(new Vector3(0,0,-64f*SCALE), new Vector3(0,0,-74f*SCALE)),
                    new TrackEdge(new Vector3(0,0, 64f*SCALE), new Vector3(0,0, 74f*SCALE))
            },
            {       // R104 track edges
                    // TODO: create edges
            },
            {       // R120 track edges
                    // TODO: create edges
            }
    };

    public Vector3 localNormalOrig;
    private Vector3 localNormDir;

    public Vector3 normalOrig;
    private Vector3 normDir;

    public TrackEdge(Vector3 normalOrigin, Vector3 normalEnd) {
        normalOrig = normalOrigin;
        normDir = Vector3.subtract(normalEnd, normalOrig).normalized();
        localNormalOrig = normalOrig;
        localNormDir = normDir;
    }

    public TrackEdge(TrackEdge edge) {
        normalOrig = new Vector3(edge.normalOrig);
        normDir = new Vector3(edge.normDir);
        localNormalOrig = new Vector3(edge.localNormalOrig);
        localNormDir = new Vector3(edge.localNormDir);
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

    public static TrackEdge[] copyEdges(TrackType type) {
        TrackEdge edges[] = new TrackEdge[EDGES[type.ordinal()].length];
        for(int i = 0; i < EDGES[type.ordinal()].length; ++i) {
            edges[i] = new TrackEdge(EDGES[type.ordinal()][i]);
        }
        return edges;
    }
}
