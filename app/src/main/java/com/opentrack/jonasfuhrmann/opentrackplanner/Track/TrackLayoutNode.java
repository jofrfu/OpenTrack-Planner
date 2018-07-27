package com.opentrack.jonasfuhrmann.opentrackplanner.Track;

import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;

import java.util.ArrayList;
import java.util.List;

public class TrackLayoutNode extends AnchorNode {
    private List<TrackNode> trackList;
    private List<Node> openEdges;

    public TrackLayoutNode() {
        trackList = new ArrayList<>();
        openEdges = new ArrayList<>();
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
                    trackList.add(track);
                    openEdges.addAll(nodes);
                    openEdges.remove(edge1);
                    openEdges.remove(edge2);
                    return true;
                }
            }
        }
        return false;
    }

    public Node checkConnection(Node edge) {
        for (Node edge1 : openEdges) {
            if (TrackNode.checkConnection(edge, edge1)) {
                return edge1;
            }
        }
        return null;
    }
}
