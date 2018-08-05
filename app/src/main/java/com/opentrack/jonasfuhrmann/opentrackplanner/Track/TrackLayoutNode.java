package com.opentrack.jonasfuhrmann.opentrackplanner.Track;

import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ShapeFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * This class holds all corresponding {@link TrackNode}s and can create a spline across all of them.
 */
public class TrackLayoutNode extends AnchorNode {
    private List<TrackNode> trackList;
    private List<Node> controlPoints;
    private List<Node> openEdges;

    /**
     * Creates a new {@link TrackLayoutNode}.
     * @param track Consists of at least one {@link TrackNode}
     */
    public TrackLayoutNode(TrackNode track) {
        trackList = new ArrayList<>();
        openEdges = new ArrayList<>();
        controlPoints = new ArrayList<>();
        addChild(track);
        trackList.add(track);
        openEdges.addAll(track.getChildren());
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
                    Vector3 localPoint = worldToLocalPoint(track.getWorldPosition());
                    Quaternion rotation = track.getWorldRotation();
                    track.setParent(this);
                    track.setLocalPosition(localPoint);
                    track.setWorldRotation(rotation);
                    openEdges.addAll(nodes);
                    openEdges.remove(edge1);
                    openEdges.remove(edge2);

                    TrackNode connectedNode = (TrackNode) edge2.getParent();
                    int index = trackList.indexOf(connectedNode);
                    if(index == 0) {
                        trackList.add(0, track);
                    } else if(index == trackList.size()-1) {
                        trackList.add(track);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Creates control points from all tracks of this {@link TrackLayoutNode} for the hermite spline.
     */
    public void createControlPoints() {
        controlPoints.clear();

        for(int i = 0; i < trackList.size(); i++) {
            if(i < trackList.size()-1) {
                TrackNode currentNode = trackList.get(i);
                TrackNode nextNode = trackList.get(i+1);

                List<Node> currentConnections = currentNode.getChildren();
                List<Node> nextConnections = nextNode.getChildren();

                loop:
                for(Node currentEdge : currentConnections) {
                    for(Node nextEdge : nextConnections) {
                        if(TrackNode.checkConnection(currentEdge, nextEdge)) {
                            controlPoints.add(currentEdge);
                            break loop;
                        }
                    }
                }
            } else if(i == trackList.size()-1 && i > 0) {
                TrackNode currentNode = trackList.get(i);
                TrackNode prevNode = trackList.get(i-1);

                List<Node> currentConnections = currentNode.getChildren();
                List<Node> prevConnections = prevNode.getChildren();

                List<Node> openEnds = new ArrayList<>(currentConnections);

                for(Node currentEdge : currentConnections) {
                    for(Node prevEdge : prevConnections) {
                        if(TrackNode.checkConnection(currentEdge, prevEdge)) {
                            openEnds.remove(currentEdge);
                        }
                    }
                }

                controlPoints.addAll(openEnds);
            }
        }
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

    /**
     * Evaluates the hermite spline, which is constructed by {@link #createControlPoints()}.
     * @param t The desired point on the spline, can only be between 0 and 1
     * @return The position on the spline, if any, null otherwise
     */
    public Vector3 evaluateHermite(double t) {
        if(t < 0 || t > 1.0) {
            return null;
        }

        int size = controlPoints.size();

        double delta = 1.0 / size;
        int index = (int)(t / delta);
        double tLocal = (t - index * delta) / delta;

        if(index+1 >= size) {
            return null;
        }

        Node controlNode0 = controlPoints.get(index);
        Node controlNode1 = controlPoints.get(index+1);

        Vector3 control0 = controlNode0.getWorldPosition();
        Vector3 control1 = controlNode1.getWorldPosition();

        Vector3 tangent0 = getControlTangent(controlNode0);
        Vector3 tangent1 = getControlTangent(controlNode1);

        double H0 = (1.0-tLocal)*(1.0-tLocal)*(1.0+2.0*tLocal);
        double H1 = tLocal*(1.0-tLocal)*(1.0-tLocal);
        double H2 = -tLocal*tLocal*(1.0-tLocal);
        double H3 = (3.0-2.0*tLocal)*tLocal*tLocal;

        Vector3 c0xH0 = control0.scaled((float)H0);
        Vector3 t0xH1 = tangent0.scaled((float)H1);
        Vector3 t1xH2 = tangent1.scaled((float)H2);
        Vector3 c1xH3 = control1.scaled((float)H3);

        return Vector3.add(Vector3.add(Vector3.add(c0xH0, t0xH1), t1xH2), c1xH3);
    }

    /**
     * Gets the tangent of a node for the hermite spline.
     * @param controlNode The current node which is one control point
     * @return The normalized tangent
     */
    private Vector3 getControlTangent(Node controlNode) {
        Node parent = controlNode.getParent();

        Vector3 tangent = Vector3.subtract(controlNode.getWorldPosition(), parent.getWorldPosition());

        return tangent.scaled(2);
    }
}
