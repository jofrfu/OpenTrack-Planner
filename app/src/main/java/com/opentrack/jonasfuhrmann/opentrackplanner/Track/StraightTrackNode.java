package com.opentrack.jonasfuhrmann.opentrackplanner.Track;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.opentrack.jonasfuhrmann.opentrackplanner.R;

public class StraightTrackNode extends TrackNode {

    public StraightTrackNode(Context context) {
        ModelRenderable.builder()
                .setSource(context, R.raw.straight)
                .build()
                .thenAccept(this::setRenderable)
                .exceptionally(
                        throwable -> {
                            Toast toast =
                                    Toast.makeText(context, "Unable to load straight renderable", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                            return null;
                        }
                );

        // TODO: replace with correct vectors
        setEdges(
                new TrackEdge(new Vector3(0,0,-6.4f*SCALE), new Vector3(0,0,-7.4f*SCALE)),
                new TrackEdge(new Vector3(0,0, 6.4f*SCALE), new Vector3(0,0, 7.4f*SCALE))
        );
    }
}
