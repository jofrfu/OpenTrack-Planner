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
import com.google.ar.sceneform.rendering.Texture;
import com.google.ar.sceneform.ux.ArFragment;
import com.opentrack.jonasfuhrmann.opentrackplanner.Track.CurrentTrackNode;
import com.opentrack.jonasfuhrmann.opentrackplanner.Track.TrackLoader;
import com.opentrack.jonasfuhrmann.opentrackplanner.Track.TrackType;

import static com.google.ar.sceneform.rendering.PlaneRenderer.MATERIAL_TEXTURE;
import static com.google.ar.sceneform.rendering.PlaneRenderer.MATERIAL_UV_SCALE;

/**
 * The main activity.
 * This activity contains the UI elements and the SceneForm fragment.
 */
public class PlannerScene extends AppCompatActivity {
    private static final String TAG = PlannerScene.class.getSimpleName();
    private static final double MIN_OPENGL_VERSION = 3.1;

    private ArFragment arFragment;
    private CurrentTrackNode currentTrackNode;

    private TrackType currentType;

    /**
     * Creates all buttons and the {@link ArFragment}.
     * @param savedInstanceState
     */
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

        currentType = TrackType.STRAIGHT;

        FloatingActionButton chooseButton = findViewById(R.id.chooseButton);
        chooseButton.setOnClickListener(v -> {
            if(currentTrackNode != null) {
                currentTrackNode.changeTrackType(currentType);
                currentType = TrackType.values()[(currentType.ordinal()+1) % TrackType.values().length];
            }
        });

        FloatingActionButton rotateButton = findViewById(R.id.rotateButton);
        rotateButton.setOnClickListener(v -> {
            if(currentTrackNode != null) {
                currentTrackNode.rotate();
            }
        });

        FloatingActionButton playButton = findViewById(R.id.playButton);
        playButton.setOnClickListener(v -> {
            if(currentTrackNode != null) {
                currentTrackNode.simulateTrain();
            }
        });

        //setPlaneTexture("studs.png");
    }

    /**
     * Used to initialize the {@link CurrentTrackNode} and add it to the scenegraph.
     * @param frameTime Default parameter
     */
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
     * Sets a texture, which is used to visualize planes.
     * @param texturePath The path of the texture file
     */
    private void setPlaneTexture(String texturePath) {
        Texture.Sampler sampler = Texture.Sampler.builder()
                .setMinFilter(Texture.Sampler.MinFilter.LINEAR)
                .setMagFilter(Texture.Sampler.MagFilter.LINEAR)
                .setWrapMode(Texture.Sampler.WrapMode.REPEAT)
                .build();

        Texture.builder().setSource(() -> getAssets().open(texturePath))
                .setSampler(sampler)
                .build().thenAccept((texture) -> arFragment.getArSceneView().getPlaneRenderer().getMaterial()
                        .thenAccept((material) -> {
                            material.setTexture(MATERIAL_TEXTURE, texture);
                            material.setFloat(MATERIAL_UV_SCALE,10f);
                        })).exceptionally(ex ->{
                            Log.e(TAG, "Failed to read texture asset file", ex);
                            return null;
                        });
    }

    /**
     * Returns false and displays an error message if SceneForm can not run, true if SceneForm can run
     * on this device.
     *
     * <p>SceneForm requires Android N on the device as well as OpenGL 3.1 capabilities.
     *
     * <p>Finishes the activity if SceneForm can not run
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
