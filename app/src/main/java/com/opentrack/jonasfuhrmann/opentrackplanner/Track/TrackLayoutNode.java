package com.opentrack.jonasfuhrmann.opentrackplanner.Track;

import com.google.ar.sceneform.AnchorNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TrackLayoutNode extends AnchorNode {
    private List<TrackNode> trackList;
    private List<TrackEdge> openEdges;

    public TrackLayoutNode() {
        trackList = new ArrayList<>();
        openEdges = new ArrayList<>();
    }

    public TrackLayoutNode(TrackNode track) {
        this();
        addChild(track);
        trackList.add(track);
        openEdges.addAll(Arrays.asList(track.getEdges()));
    }

    public boolean connect(TrackNode track) {
        TrackEdge edges[] = track.getEdges();
        for(TrackEdge edge1 : edges) {
            for(TrackEdge edge2 : openEdges) {
                if(TrackEdge.checkConnection(edge1, edge2)) {
                    addChild(track);
                    trackList.add(track);
                    openEdges.addAll(Arrays.asList(edges));
                    openEdges.remove(edge1);
                    openEdges.remove(edge2);
                    return true;
                }
            }
        }
        return false;
    }

    public boolean checkConnection(TrackEdge edges[]) {
        for(TrackEdge edge1 : edges) {
            for (TrackEdge edge2 : openEdges) {
                if (TrackEdge.checkConnection(edge1, edge2)) {
                    return true;
                }
            }
        }
        return false;
    }
}
