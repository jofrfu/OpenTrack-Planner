package com.opentrack.jonasfuhrmann.opentrackplanner.Track;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

import com.google.ar.sceneform.rendering.ModelRenderable;

/**
 * A factory to create track {@link com.google.ar.sceneform.rendering.Renderable}s.
 */
public class TrackLoader {

    private Context mContext;
    private ModelRenderable modelList[];

    /**
     * Creates a new {@link TrackLoader}.
     * @param context Necessary for {@link com.google.ar.sceneform.rendering.Renderable} creation
     */
    public TrackLoader(Context context) {
        mContext = context;
        modelList = new ModelRenderable[TrackType.values().length];

        TrackType types[] = TrackType.values();
        for (int i = 0; i < modelList.length; i++) {
            int finalI = i;
            ModelRenderable.builder()
                    .setSource(mContext, types[i].RESOURCE_ID)
                    .build()
                    .thenAccept(modelRenderable -> modelList[types[finalI].ordinal()] = modelRenderable)
                    .exceptionally(
                            throwable -> {
                                Toast toast =
                                        Toast.makeText(mContext, "Unable to load " + types[finalI] + " renderable", Toast.LENGTH_LONG);
                                toast.setGravity(Gravity.CENTER, 0, 0);
                                toast.show();
                                return null;
                            }
                    );
        }
    }

    /**
     * Returns a {@link ModelRenderable}.
     * {@link com.google.ar.sceneform.rendering.Renderable}s are pre-calculated in {@link #TrackLoader(Context)}.
     * @param type The desired track type
     * @return The desired renderable
     */
    public ModelRenderable createRenderable(TrackType type) {
        return modelList[type.ordinal()];
    }
}
