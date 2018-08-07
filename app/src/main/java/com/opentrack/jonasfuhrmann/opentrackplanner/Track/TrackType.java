package com.opentrack.jonasfuhrmann.opentrackplanner.Track;

import com.opentrack.jonasfuhrmann.opentrackplanner.R;

/**
 * This enum is used to determine the types of track across a variety of classes.
 * The enum also holds their respective resource IDs for {@link com.google.ar.sceneform.rendering.ModelRenderable}s.
 */
public enum TrackType {
    // TODO: set correct resource IDs
    STRAIGHT(R.raw.straight), R40_CURVE(R.raw.r40_curve), R104_CURVE(R.raw.r104_curve);//, R120_CURVE(0);

    public final int RESOURCE_ID;

    TrackType(int resourceID) {
        RESOURCE_ID = resourceID;
    }
}
