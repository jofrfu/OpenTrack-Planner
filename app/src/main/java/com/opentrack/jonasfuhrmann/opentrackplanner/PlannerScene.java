package com.opentrack.jonasfuhrmann.opentrackplanner;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.ar.core.TrackingState;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.ux.ArFragment;
import com.opentrack.jonasfuhrmann.opentrackplanner.Track.CurrentTrackNode;
import com.opentrack.jonasfuhrmann.opentrackplanner.Track.TrackLoader;
import com.opentrack.jonasfuhrmann.opentrackplanner.Track.TrackNode;
import com.opentrack.jonasfuhrmann.opentrackplanner.Track.TrackType;


public class PlannerScene extends AppCompatActivity {
    private static final String TAG = PlannerScene.class.getSimpleName();
    private static final double MIN_OPENGL_VERSION = 3.1;

    private ArFragment arFragment;
    private CurrentTrackNode currentTrackNode;

    @Override
    @SuppressWarnings({"AndroidApiChecker", "FutureReturnValueIgnored"})
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!checkIsSupportedDeviceOrFinish(this)) {
            return;
        }

        setContentView(R.layout.activity_ux);

        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);
        arFragment.getArSceneView().getScene().setOnUpdateListener(this::onSceneUpdate);

        FloatingActionButton addButton = findViewById(R.id.addButton);
        addButton.setOnClickListener(v -> {
            if(currentTrackNode != null) {
                currentTrackNode.placeTrack();
            }
        });

        FloatingActionButton chooseButton = findViewById(R.id.chooseButton);
        chooseButton.setOnClickListener(v -> {
            if(currentTrackNode != null) {
                currentTrackNode.changeTrackType(TrackType.STRAIGHT);
            }
        });
    }

    private void onSceneUpdate(FrameTime frameTime) {
        arFragment.onUpdate(frameTime);

        if (arFragment.getArSceneView().getArFrame() == null) {
            return;
        }

        if (arFragment.getArSceneView().getArFrame().getCamera().getTrackingState() != TrackingState.TRACKING) {
            return;
        }

        if (currentTrackNode == null) {
            currentTrackNode = new CurrentTrackNode(arFragment.getArSceneView().getSession(),
                    new TrackLoader(this));
            currentTrackNode.setParent(arFragment.getArSceneView().getScene());
        }
    }

    /**
     * Returns false and displays an error message if Sceneform can not run, true if Sceneform can run
     * on this device.
     *
     * <p>Sceneform requires Android N on the device as well as OpenGL 3.1 capabilities.
     *
     * <p>Finishes the activity if Sceneform can not run
     */
    public static boolean checkIsSupportedDeviceOrFinish(final Activity activity) {
        String openGlVersionString =
                ((ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE))
                        .getDeviceConfigurationInfo()
                        .getGlEsVersion();
        if (Double.parseDouble(openGlVersionString) < MIN_OPENGL_VERSION) {
            Log.e(TAG, "Sceneform requires OpenGL ES 3.1 later");
            Toast.makeText(activity, "Sceneform requires OpenGL ES 3.1 or later", Toast.LENGTH_LONG)
                    .show();
            activity.finish();
            return false;
        }
        return true;
    }
}
