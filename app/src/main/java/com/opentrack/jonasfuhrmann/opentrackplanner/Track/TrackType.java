package com.opentrack.jonasfuhrmann.opentrackplanner.Track;

import com.opentrack.jonasfuhrmann.opentrackplanner.R;

public enum TrackType {
    // TODO: set correct resource IDs
    STRAIGHT(R.raw.straight), R104_CURVE(R.raw.r104_curve);//, R120_CURVE(0);

    public final int RESOURCE_ID;

    TrackType(int resourceID) {
        RESOURCE_ID = resourceID;
    }
}
