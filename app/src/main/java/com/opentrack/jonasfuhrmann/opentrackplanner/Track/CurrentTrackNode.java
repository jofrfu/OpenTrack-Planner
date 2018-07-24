package com.opentrack.jonasfuhrmann.opentrackplanner.Track;

import com.google.ar.core.Plane;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.sceneform.Camera;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Vector3;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.opentrack.jonasfuhrmann.opentrackplanner.VectorHelper.floatToVec;
import static com.opentrack.jonasfuhrmann.opentrackplanner.VectorHelper.quatToFloat;
import static com.opentrack.jonasfuhrmann.opentrackplanner.VectorHelper.vecToFloat;

public class CurrentTrackNode extends Node {

    private Session mSession;
    private TrackLoader mTrackLoader;
    private TrackType trackType;
    private TrackEdge trackEdges[];
    private List<TrackLayoutNode> trackLayoutNodes;

    public CurrentTrackNode(Session session, TrackLoader trackLoader) {
        super();
        mSession = session;
        mTrackLoader = trackLoader;
        trackLayoutNodes = new ArrayList<>();
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

        if(trackEdges != null) {
            for (TrackEdge edge : trackEdges) {
                edge.transform(getWorldPosition(), getWorldRotation());

                for (TrackLayoutNode layoutNode : trackLayoutNodes) {
                    TrackEdge collidingEdge = layoutNode.checkConnection(edge);
                    if (collidingEdge != null) {
                        setWorldPosition(Vector3.subtract(collidingEdge.normalOrig, edge.localNormalOrig));
                        return;
                    }
                }
            }
        }
    }

    public void changeTrackType(TrackType type) {
        trackType = type;
        setRenderable(mTrackLoader.createRenderable(trackType));
        trackEdges = TrackEdge.copyEdges(type);
        for(TrackEdge trackEdge : trackEdges) {
            trackEdge.transform(getWorldPosition(), getWorldRotation());
        }
    }

    public void placeTrack() {
        TrackEdge edges[] = new TrackEdge[trackEdges.length];
        for(int i = 0; i < trackEdges.length; ++i) {
            edges[i] = new TrackEdge(trackEdges[i]);
        }

        TrackNode trackNode = new TrackNode(edges);
        trackNode.setRenderable(getRenderable());

        if(trackLayoutNodes.isEmpty()) {
            createLayout(trackNode);
            return;
        }

        for(TrackLayoutNode layoutNode : trackLayoutNodes) {
            if(layoutNode.connect(trackNode)) {
                layoutNode.setParent(getScene());
                trackNode.setParent(layoutNode);
                trackNode.setWorldPosition(getWorldPosition());
                trackNode.setWorldRotation(getWorldRotation());
                return;
            }
        }

        createLayout(trackNode);
    }

    private void createLayout(TrackNode track) {
        TrackLayoutNode layoutNode = new TrackLayoutNode(track);

        Collection<Plane> planeList = mSession.getAllTrackables(Plane.class);
        Pose pose = new Pose(
                vecToFloat(getWorldPosition()),
                quatToFloat(getWorldRotation())
        );

        for (Plane plane : planeList) {
            if(plane.isPoseInExtents(pose)) {
                layoutNode.setParent(getScene());
                track.setParent(layoutNode);
                layoutNode.setWorldPosition(getWorldPosition());
                layoutNode.setWorldRotation(getWorldRotation());
                layoutNode.setAnchor(plane.createAnchor(pose));
                trackLayoutNodes.add(layoutNode);
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
