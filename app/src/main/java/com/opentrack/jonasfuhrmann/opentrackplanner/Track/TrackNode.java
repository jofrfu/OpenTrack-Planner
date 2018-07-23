package com.opentrack.jonasfuhrmann.opentrackplanner.Track;

import com.google.ar.sceneform.Node;

class TrackNode extends Node {

    // For mm to m conversion
    protected static final float SCALE = 0.001f;

    private TrackEdge[] edges;

    protected void setEdges(TrackEdge... trackEdges) {
        edges = trackEdges;
    }

    public TrackEdge[] getEdges() {
        return edges;
    }
}
