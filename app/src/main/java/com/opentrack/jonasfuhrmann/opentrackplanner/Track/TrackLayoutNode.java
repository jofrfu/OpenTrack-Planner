package com.opentrack.jonasfuhrmann.opentrackplanner.Track;

import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;

import java.util.ArrayList;
import java.util.List;

public class TrackLayoutNode extends AnchorNode {
    private List<TrackNode> trackList;
    private List<Node> controlPoints;
    private List<Node> openEdges;

    public TrackLayoutNode() {
        trackList = new ArrayList<>();
        openEdges = new ArrayList<>();
        controlPoints = new ArrayList<>();
    }

    public TrackLayoutNode(TrackNode track) {
        this();
        addChild(track);
        trackList.add(track);
        openEdges.addAll(track.getChildren());
    }

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
            } else if(i > 0) {
                TrackNode currentNode = trackList.get(i);
                TrackNode prevNode = trackList.get(i-1);

                List<Node> currentConnections = currentNode.getChildren();
                List<Node> prevConnections = prevNode.getChildren();

                loop:
                for(Node currentEdge : currentConnections) {
                    for(Node prevEdge : prevConnections) {
                        if(!TrackNode.checkConnection(currentEdge, prevEdge)) {
                            controlPoints.add(currentEdge);
                            break loop;
                        }
                    }
                }
            }
        }
    }

    public Node checkConnection(Node edge) {
        for (Node edge1 : openEdges) {
            if (TrackNode.checkConnection(edge, edge1)) {
                return edge1;
            }
        }
        return null;
    }

    public Vector3 evaluateHermite(double t) {
        if(t < 0 || t > 1.0) {
            throw new IllegalArgumentException("Hermite curve can only be evaluated between 0 and 1!");
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

        return Vector3.add(
                Vector3.add(
                        Vector3.add(
                                control0.scaled((float)H0),
                                tangent0.scaled((float)H1)),
                        tangent1.scaled((float)H2)),
                control1.scaled((float)H3)
        );
    }

    private Vector3 getControlTangent(Node controlNode) {
        Node parent = controlNode.getParent();

        Vector3 tangent = Vector3.subtract(controlNode.getWorldPosition(), parent.getWorldPosition());

        tangent.normalized();

        return tangent;
    }
}
