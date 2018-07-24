package com.opentrack.jonasfuhrmann.opentrackplanner.Track;

import android.content.Context;
import android.os.AsyncTask;
import android.view.Gravity;
import android.widget.Toast;

import com.google.ar.sceneform.rendering.ModelRenderable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class TrackLoader {

    private Context mContext;
    private ModelRenderable modelList[];

    public TrackLoader(Context context) {
        mContext = context;
        modelList = new ModelRenderable[TrackType.values().length];

        TrackType types[] = TrackType.values();
        for (int i = 0; i < 1; i++) {
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

    // factory method
    public ModelRenderable createRenderable(TrackType type) {
        return modelList[type.ordinal()];
    }
}
