package com.opentrack.jonasfuhrmann.opentrackplanner;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import com.google.ar.core.Anchor;
import com.google.ar.core.Plane;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Camera;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;

import java.util.Collection;

public class PlannerScene extends AppCompatActivity {
    private static final String TAG = PlannerScene.class.getSimpleName();
    private static final double MIN_OPENGL_VERSION = 3.1;

    private ArFragment arFragment;
    private ModelRenderable straightRenderable;
    private AnchorNode anchorNode;

    @Override
    @SuppressWarnings({"AndroidApiChecker", "FutureReturnValueIgnored"})
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!checkIsSupportedDeviceOrFinish(this)) {
            return;
        }

        setContentView(R.layout.activity_ux);

        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);

        straightRenderable.builder()
                .setSource(this, R.raw.straight)
                .build()
                .thenAccept(renderable -> straightRenderable = renderable)
                .exceptionally(
                        throwable -> {
                            Toast toast =
                                    Toast.makeText(this, "Unable to load andy renderable", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                            return null;
                        });

        arFragment.getArSceneView().getScene().setOnUpdateListener(this::onSceneUpdate);
    }

    private void onSceneUpdate(FrameTime frameTime) {
        arFragment.onUpdate(frameTime);

        if (arFragment.getArSceneView().getArFrame() == null) {
            return;
        }

        if (arFragment.getArSceneView().getArFrame().getCamera().getTrackingState() != TrackingState.TRACKING) {
            return;
        }

        float[] pos = { 0,0,0 };
        float[] rotation = {0,0,0,1};
        if (this.anchorNode == null) {
            Session session = arFragment.getArSceneView().getSession();
            Anchor anchor =  session.createAnchor(new Pose(pos, rotation));
            anchorNode = new AnchorNode(anchor);
            anchorNode.setRenderable(straightRenderable);
            anchorNode.setParent(arFragment.getArSceneView().getScene());
        }

        Session session = arFragment.getArSceneView().getSession();
        Collection<Plane> planeList = session.getAllTrackables(Plane.class);

        for(Plane plane : planeList) {
            float[] intersection = getIntersection(plane);
            if(intersection != null) {
                Pose pose = new Pose(intersection, rotation);
                anchorNode.setAnchor(session.createAnchor(pose));
                break;
            }
        }
    }

    private float[] getIntersection(Plane plane) {
        Camera camera = arFragment.getArSceneView().getScene().getCamera();

        Pose center = plane.getCenterPose();

        Vector3 cameraPosition = camera.getWorldPosition();
        Vector3 cameraDirection = camera.getForward();

        float pointA[] = center.transformPoint(new float[]{1, 0, 0}); // x axis
        float pointB[] = center.transformPoint(new float[]{0, 0, 1}); // z axis

        float centerPoint[] = center.transformPoint(new float[]{0, 0, 0}); // origin

        Vector3 dirA = Vector3.subtract(floatToVec(pointA), floatToVec(centerPoint));
        Vector3 dirB = Vector3.subtract(floatToVec(pointB), floatToVec(centerPoint));

        Vector3 normal = Vector3.cross(dirA, dirB);

        float t = -Vector3.dot(normal, Vector3.subtract(cameraPosition, floatToVec(centerPoint)))
                  / Vector3.dot(normal, cameraDirection);

        float intersection[] = vecToFloat(Vector3.add(cameraPosition, cameraDirection.scaled(t)));

        Pose checkPose = new Pose(intersection, new float[]{0, 0, 0, 1});
        if(!plane.isPoseInExtents(checkPose)) {
            return null;
        }
        return intersection;
    }

    private Vector3 floatToVec(float[] vector) {
        Vector3 vec = new Vector3();
        vec.x = vector[0];
        vec.y = vector[1];
        vec.z = vector[2];
        return vec;
    }

    private float[] vecToFloat(Vector3 vector) {
        return new float[]{vector.x, vector.y, vector.z};
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
