package com.opentrack.jonasfuhrmann.opentrackplanner.Track;

import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.math.Vector3;

public class TrackNode extends AnchorNode {

    // For mm to m conversion
    private static final float SCALE = 0.001f;

    public static final TrackEdge EDGES[][] = {
            {       // Straight track edges
                    new TrackEdge(new Vector3(0,0,-6.4f*SCALE), new Vector3(0,0,-6.4f*SCALE)),
                    new TrackEdge(new Vector3(0,0, 6.4f*SCALE), new Vector3(0,0, 6.4f*SCALE))
            },
            {       // R104 track edges
                    // TODO: create edges
            },
            {       // R120 track edges
                    // TODO: create edges
            }
    };

    private TrackType trackType;

    public TrackNode(TrackType type) {
        edges = EDGES[type.ordinal()];
        trackType = type;
    }

    private TrackEdge[] edges;

    public TrackEdge[] getEdges() {
        return edges;
    }
}
