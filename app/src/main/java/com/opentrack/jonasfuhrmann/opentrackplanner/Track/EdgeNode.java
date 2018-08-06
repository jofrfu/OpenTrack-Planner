package com.opentrack.jonasfuhrmann.opentrackplanner.Track;

import com.google.ar.sceneform.Node;

/**
 * This class is used to build a dynamic linked list for {@link TrackNode}s.
 * {@link TrackNode}s can then be used in {@link TrackLayoutNode} for layouts.
 */
public class EdgeNode extends Node {
    public EdgeNode connectedEdge;
}
