package com.opentrack.jonasfuhrmann.opentrackplanner.Track;

import com.google.ar.core.Anchor;
import com.google.ar.core.Plane;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.sceneform.Camera;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;

import java.util.Collection;

import static com.opentrack.jonasfuhrmann.opentrackplanner.VectorHelper.floatToVec;
import static com.opentrack.jonasfuhrmann.opentrackplanner.VectorHelper.quatToFloat;
import static com.opentrack.jonasfuhrmann.opentrackplanner.VectorHelper.vecToFloat;

public class CurrentTrackNode extends Node {

    private Session mSession;
    private TrackLoader mTrackLoader;
    private TrackType trackType;

    public CurrentTrackNode(Session session, TrackLoader trackLoader) {
        super();
        mSession = session;
        mTrackLoader = trackLoader;
    }

    @Override
    public void onUpdate(FrameTime frameTime) {
        super.onUpdate(frameTime);
        Collection<Plane> planeList = mSession.getAllTrackables(Plane.class);

        for (Plane plane : planeList) {
            float[] intersection = getIntersection(plane);
            if (intersection != null) {
                setWorldPosition(floatToVec(intersection));
                break;
            }
        }
    }

    public void changeTrackType(TrackType type) {
        trackType = type;
        setRenderable(mTrackLoader.createRenderable(trackType));
    }

    public void placeTrack() {
        TrackNode trackNode = new TrackNode(trackType);
        trackNode.setParent(getScene());
        trackNode.setWorldPosition(getWorldPosition());
        trackNode.setWorldRotation(getWorldRotation());
        trackNode.setRenderable(getRenderable());

        Collection<Plane> planeList = mSession.getAllTrackables(Plane.class);
        Pose pose = new Pose(
                vecToFloat(trackNode.getWorldPosition()),
                quatToFloat(trackNode.getWorldRotation())
        );

        for (Plane plane : planeList) {
            if(plane.isPoseInExtents(pose)) {
                trackNode.setAnchor(plane.createAnchor(pose));
                break;
            }
        }
    }

    private float[] getIntersection(Plane plane) {
        Camera camera = getScene().getCamera();

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
}
