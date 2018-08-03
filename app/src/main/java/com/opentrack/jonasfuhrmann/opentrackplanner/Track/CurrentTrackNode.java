package com.opentrack.jonasfuhrmann.opentrackplanner.Track;

import com.google.ar.core.Plane;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.sceneform.Camera;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Material;
import com.google.ar.sceneform.rendering.MaterialFactory;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ShapeFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.opentrack.jonasfuhrmann.opentrackplanner.VectorHelper.floatToVec;
import static com.opentrack.jonasfuhrmann.opentrackplanner.VectorHelper.quatToFloat;
import static com.opentrack.jonasfuhrmann.opentrackplanner.VectorHelper.vecToFloat;

public class CurrentTrackNode extends TrackNode {

    private static final float ROTATION_STEP = 10*2.0f*(float)Math.PI/360.0f;
    private float rad;

    private static final double SIMULATION_STEP = 0.01;
    private static final double TANGENT_STEP = 0.01;

    private Session mSession;
    private TrackLoader mTrackLoader;
    private TrackType trackType;
    private List<TrackLayoutNode> trackLayoutNodes;

    private boolean simulationRunning;
    private double currentStep;

    private Node trainNode;

    public CurrentTrackNode(Session session, TrackLoader trackLoader) {
        super();
        mSession = session;
        mTrackLoader = trackLoader;
        trackLayoutNodes = new ArrayList<>();
        simulationRunning = false;
        currentStep = 0;
        resetRotation();
    }

    private void resetRotation() {
        Vector3 currentLook = Quaternion.rotateVector(getWorldRotation(), Vector3.right());
        rad = (float)Math.acos(Vector3.dot(Vector3.right(), currentLook));
    }

    public void rotate() {
        rad += ROTATION_STEP;

        if(rad >= 2.0f*Math.PI) {
            rad -= 2.0f*Math.PI;
        }

        Vector3 newLook = new Vector3((float)Math.cos(rad), 0, (float)Math.sin(rad));
        Quaternion rotation = Quaternion.rotationBetweenVectors(newLook, Vector3.right());
        setWorldRotation(rotation);
    }

    public void simulateTrain() {
        if(trainNode == null) {
            ModelRenderable train = ShapeFactory.makeCube(Vector3.one().scaled(0.05f), Vector3.zero(), getRenderable().getMaterial());
            trainNode = new Node();
            trainNode.setParent(getScene());
            trainNode.setRenderable(train);
        }

        if(!simulationRunning) {
            for(TrackLayoutNode node : trackLayoutNodes) {
                node.createControlPoints();
            }
            trainNode.setEnabled(true);
            currentStep = 0;
            simulationRunning = true;
        }
    }

    @Override
    public void onUpdate(FrameTime frameTime) {
        super.onUpdate(frameTime);

        if(simulationRunning && !trackLayoutNodes.isEmpty()) {
            TrackLayoutNode node = trackLayoutNodes.get(trackLayoutNodes.size()-1);
            Vector3 position = node.evaluateHermite(currentStep);
            Vector3 stepBefore = node.evaluateHermite(currentStep - TANGENT_STEP);
            Vector3 stepAfter = node.evaluateHermite(currentStep + TANGENT_STEP);
            currentStep += SIMULATION_STEP;

            if(currentStep >= 1.0 || position == null) {
                simulationRunning = false;
            }

            if(position != null) {
                Vector3 up = node.localToWorldDirection(Vector3.up());
                trainNode.setWorldPosition(Vector3.add(position, up.scaled(0.05f)));

                if(stepBefore != null && stepAfter != null) {
                    Vector3 direction = Vector3.subtract(stepAfter, stepBefore);
                    Quaternion rotation = Quaternion.lookRotation(direction, node.localToWorldDirection(Vector3.up()));
                    trainNode.setWorldRotation(rotation);
                }
            }
        } else {
            if(trainNode != null) {
                trainNode.setEnabled(false);
            }
        }

        Collection<Plane> planeList = mSession.getAllTrackables(Plane.class);

        Map<Float, Vector3> intersectionMap = new HashMap<>();
        for(Plane plane : planeList) {
            Vector3 intersection = getIntersection(plane);
            if (intersection != null) {
                Vector3 camPosition = getScene().getCamera().getWorldPosition();
                intersectionMap.put(Vector3.subtract(intersection, camPosition).length(), intersection);
            }
        }

        Set<Float> keys = intersectionMap.keySet();
        if(!keys.isEmpty()) {
            float nearest = Float.MAX_VALUE;
            for (float length : keys) {
                if (length < nearest) {
                    nearest = length;
                }
            }

            setWorldPosition(intersectionMap.get(nearest));
        }

        for(Node edge : getChildren()) {
            for(TrackLayoutNode layout : trackLayoutNodes) {
                Node collidingEdge = layout.checkConnection(edge);
                if(collidingEdge != null) {
                    setWorldRotation(Quaternion.identity());
                    Vector3 normal = Vector3.subtract(edge.getWorldPosition(), edge.getParent().getWorldPosition());
                    Vector3 collidingNormal = Vector3.subtract(collidingEdge.getParent().getWorldPosition(), collidingEdge.getWorldPosition());
                    Quaternion rotation = Quaternion.rotationBetweenVectors(normal, collidingNormal);

                    setWorldRotation(rotation);
                    resetRotation();

                    normal = Vector3.subtract(edge.getWorldPosition(), edge.getParent().getWorldPosition());
                    setWorldPosition(Vector3.subtract(collidingEdge.getWorldPosition(), normal));
                    return;
                }
            }
        }
    }

    public void changeTrackType(TrackType type) {
        trackType = type;
        setRenderable(mTrackLoader.createRenderable(trackType));
        setNormalOrigins(getLocalEdges(type));
    }

    public void placeTrack() {
        if(trackType == null) return;

        TrackNode trackNode = new TrackNode();
        trackNode.setWorldPosition(getWorldPosition());
        trackNode.setWorldRotation(getWorldRotation());
        trackNode.setNormalOrigins(getLocalEdges(trackType));
        trackNode.setRenderable(getRenderable());

        for(TrackLayoutNode layoutNode : trackLayoutNodes) {
            if(layoutNode.connect(trackNode)) {
                layoutNode.setParent(getScene());
                return;
            }
        }

        trackNode.setWorldPosition(Vector3.zero());
        trackNode.setWorldRotation(Quaternion.identity());
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

    private Vector3 getIntersection(Plane plane) {
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

        Vector3 intersection = Vector3.add(cameraPosition, cameraDirection.scaled(t));

        Pose checkPose = new Pose(vecToFloat(intersection), new float[]{0, 0, 0, 1});
        if(!plane.isPoseInExtents(checkPose)) {
            return null;
        }
        return intersection;
    }
}
