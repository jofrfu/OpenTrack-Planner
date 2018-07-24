package com.opentrack.jonasfuhrmann.opentrackplanner.Track;

import com.google.ar.sceneform.Node;

public class TrackNode extends Node {
    public TrackNode(TrackEdge... trackEdges) {
        edges = trackEdges;
    }

    private TrackEdge[] edges;

    public TrackEdge[] getEdges() {
        return edges;
    }

}
