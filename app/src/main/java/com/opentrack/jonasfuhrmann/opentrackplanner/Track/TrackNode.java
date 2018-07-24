package com.opentrack.jonasfuhrmann.opentrackplanner.Track;

import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Vector3;

import java.util.List;

public class TrackNode extends Node {

    private static final float EPSILON = 0.08f; // TODO: change to good value

    private static final float SCALE = 0.001f;

    private static final Vector3 EDGES[][] = {
            // straight
            {
                    new Vector3(0,0,-64f*SCALE),
                    new Vector3(0,0, 64f*SCALE)
            },
            // R104 TODO: corrections
            {
                    new Vector3(0,0,0),
                    new Vector3(0,0,0)
            },
            // R120 TODO: corrections
            {
                    new Vector3(0,0,0),
                    new Vector3(0,0,0)
            }
    };

    public void setNormalOrigins(Vector3... normalOrigins) {
        for (Vector3 normalOrigin : normalOrigins) {
            Node temp = new Node();
            temp.setLocalPosition(normalOrigin);
            temp.setParent(this);
        }
    }

    public static boolean checkConnection(Node origin1, Node origin2) {
        Node track1 = origin1.getParent();
        Node track2 = origin2.getParent();

        Vector3 normal1 = Vector3.subtract(track1.getWorldPosition(), origin1.getWorldPosition());
        Vector3 normal2 = Vector3.subtract(track2.getWorldPosition(), origin2.getWorldPosition());

        // check normal directions
        if(Vector3.dot(normal1, normal2) >= 0) {
            // track edges don't point against each other
            return  false;
        }

        // non-normalized direction vector of the origins of the edges
        Vector3 distanceDir = Vector3.subtract(origin2.getWorldPosition(), origin1.getWorldPosition());
        float distance = distanceDir.length();

        // check if points are close enough
        return distance < EPSILON;
    }

    public Vector3[] getLocalEdges(TrackType type) {
        return EDGES[type.ordinal()];
    }

    public Vector3[] getCurrentEdges() {
        Vector3 edges[] = new Vector3[getChildren().size()];
        for(int i = 0; i < getChildren().size(); i++) {
            edges[i] = getChildren().get(i).getWorldPosition();
        }
        return edges;
    }
}
