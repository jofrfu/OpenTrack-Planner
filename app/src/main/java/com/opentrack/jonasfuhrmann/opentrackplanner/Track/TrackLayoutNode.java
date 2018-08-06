package com.opentrack.jonasfuhrmann.opentrackplanner.Track;

import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;

import java.util.ArrayList;
import java.util.List;

/**
 * This class holds all corresponding {@link TrackNode}s and can create a spline across all of them.
 */
public class TrackLayoutNode extends AnchorNode {
    private List<Node> openEdges;
    private int size;

    /**
     * Creates a new {@link TrackLayoutNode}.
     * @param track Consists of at least one {@link TrackNode}
     */
    public TrackLayoutNode(TrackNode track) {
        openEdges = new ArrayList<>();
        addChild(track);
        openEdges.addAll(track.getChildren());
        size = 1;
    }

    /**
     * Tries to connect a {@link TrackNode} to this {@link TrackLayoutNode}.
     * The {@link TrackNode} is saved on success.
     * @param track The track to be connected
     * @return True, if the connection was successful
     */
    public boolean connect(TrackNode track) {
        List<Node> nodes = track.getChildren();
        for(Node edge1 : nodes) {
            for(Node edge2 : openEdges) {
                if(TrackNode.checkConnection(edge1, edge2)) {
                    Vector3 position = track.getWorldPosition();
                    Quaternion rotation = track.getWorldRotation();
                    track.setParent(this);
                    track.setWorldPosition(position);
                    track.setWorldRotation(rotation);
                    openEdges.addAll(nodes);
                    openEdges.remove(edge1);
                    openEdges.remove(edge2);

                    ((EdgeNode) edge1).connectedEdge = (EdgeNode) edge2;
                    ((EdgeNode) edge2).connectedEdge = (EdgeNode) edge1;
                    size++;
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks the connection from an edge to the open edges of this {@link TrackLayoutNode}.
     * @param edge The edge to be checked
     * @return The edge, which the checked edge is connected to.
     */
    public Node checkConnection(Node edge) {
        for (Node edge1 : openEdges) {
            if (TrackNode.checkConnection(edge, edge1)) {
                return edge1;
            }
        }
        return null;
    }

    public Vector3 evaluateCurve(double t) {
        if(t < 0 || t > 1) return null;

        double delta = 1.0 / size;
        int index = (int)(t / delta);
        double tLocal = (t - index * delta) / delta;

        EdgeNode edge = getEdge(index);
        if(edge == null) return null;
        TrackNode track = (TrackNode) edge.getParent();
        return track.evaluateBezier(tLocal, edge);
    }

    private EdgeNode getEdge(int index) {
        EdgeNode currentEdge = (EdgeNode) openEdges.get(0);

        for(int i = 0; i < index; i++) {
            List<Node> children = new ArrayList<>(currentEdge.getParent().getChildren());
            children.remove(currentEdge);
            EdgeNode otherEdge = (EdgeNode) children.get(0);
            currentEdge = otherEdge.connectedEdge;
        }

        return currentEdge;
    }
}
