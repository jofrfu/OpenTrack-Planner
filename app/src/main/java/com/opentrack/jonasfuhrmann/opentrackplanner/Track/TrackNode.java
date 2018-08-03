package com.opentrack.jonasfuhrmann.opentrackplanner.Track;

import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Vector3;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a track in space.
 */
public class TrackNode extends Node {

    private static final float EPSILON = 0.08f;

    private static final float SCALE = 0.001f;

    /**
     * Values for the connection edges of all track types
     */
    private static final Vector3 EDGES[][] = {
            // straight
            {
                    new Vector3(0,0,-64f*SCALE),
                    new Vector3(0,0, 64f*SCALE)
            },
            // R104 TODO: corrections
            {
                    new Vector3(0,0,-80f*SCALE),
                    new Vector3(15.607226f*SCALE,0,78.462822f*SCALE)
            },
            // R120 TODO: corrections
            {
                    new Vector3(0,0,0),
                    new Vector3(0,0,0)
            }
    };

    /**
     * Sets all origins of direction vectors for the connection edges.
     * @param directionOrigins The origins of the direction Vectors, end point is the origin of the track
     */
    public void setDirectionOrigins(Vector3... directionOrigins) {
        List<Node> childNodes = new ArrayList<>(getChildren());
        if(!childNodes.isEmpty()) {
            for(int i = 0; i < childNodes.size(); i++) {
                removeChild(childNodes.get(i));
            }
        }

        for(Vector3 normalOrigin : directionOrigins) {
            Node temp = new Node();
            temp.setLocalPosition(normalOrigin);
            temp.setParent(this);
        }
    }

    /**
     * Checks if an edge of a {@link TrackNode} should be connected to another edge.
     * @param origin1 The first edge origin
     * @param origin2 The second edge origin
     * @return Should be connected
     */
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

    /**
     * Getter for the edges.
     * @param type The desired type of edges
     * @return Connection edges for a {@link TrackNode}
     */
    public Vector3[] getLocalEdges(TrackType type) {
        return EDGES[type.ordinal()];
    }

    /**
     * Getter for the edges of this {@link TrackNode}.
     * @return The edges of this {@link TrackNode}
     */
    public Vector3[] getCurrentEdges() {
        Vector3 edges[] = new Vector3[getChildren().size()];
        for(int i = 0; i < getChildren().size(); i++) {
            edges[i] = getChildren().get(i).getWorldPosition();
        }
        return edges;
    }
}
