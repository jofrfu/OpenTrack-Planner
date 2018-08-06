package com.opentrack.jonasfuhrmann.opentrackplanner.Track;

import com.google.ar.core.Plane;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.sceneform.Camera;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
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

/**
 * This class creates a track, which is placeable in space.
 * A {@link CurrentTrackNode} creates and holds {@link TrackLayoutNode}s and can assign tracks to each one.
 */
public class CurrentTrackNode extends TrackNode {

    private static final float ROTATION_STEP = 10*2.0f*(float)Math.PI/360.0f;
    // Current angle in radiant
    private float rad;

    private static final double SIMULATION_STEP = 0.01;
    private static final double TANGENT_STEP = 0.01;

    private Session mSession;
    private TrackLoader mTrackLoader;
    private List<TrackLayoutNode> trackLayoutNodes;

    private boolean simulationRunning;
    private double currentStep;

    // The simulated train
    private Node trainNode;

    /**
     * Creates a new {@link CurrentTrackNode}.
     * @param session The ARCore {@link Session} for interaction purposes
     * @param trackLoader For {@link com.google.ar.sceneform.rendering.Renderable} creation
     */
    public CurrentTrackNode(Session session, TrackLoader trackLoader) {
        super(null);
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

    /**
     * Rotates the {@link CurrentTrackNode} by 10 degrees clockwise on the current plane.
     */
    public void rotate() {
        rad += ROTATION_STEP;

        if(rad >= 2.0f*Math.PI) {
            rad -= 2.0f*Math.PI;
        }

        Vector3 newLook = new Vector3((float)Math.cos(rad), 0, (float)Math.sin(rad));
        Quaternion rotation = Quaternion.rotationBetweenVectors(newLook, Vector3.right());
        setWorldRotation(rotation);
    }

    /**
     * Creates a {@link Node}, which simulates a train.
     * Simulation will start, if this method is called.
     */
    public void simulateTrain() {
        if(trainNode == null) {
            ModelRenderable train = ShapeFactory.makeCube(Vector3.one().scaled(0.05f), Vector3.zero(), getRenderable().getMaterial());
            trainNode = new Node();
            trainNode.setParent(getScene());
            trainNode.setRenderable(train);
        }

        if(!simulationRunning) {
            trainNode.setEnabled(true);
            currentStep = 0;
            simulationRunning = true;
        }
    }

    /**
     * Overridden for animation purposes.
     * All animations are calculated here.
     * @param frameTime Default parameter
     */
    @Override
    public void onUpdate(FrameTime frameTime) {
        super.onUpdate(frameTime);

        if(simulationRunning && !trackLayoutNodes.isEmpty()) {
            TrackLayoutNode node = trackLayoutNodes.get(trackLayoutNodes.size()-1);
            Vector3 position = node.evaluateCurve(currentStep);
            Vector3 stepBefore = node.evaluateCurve(currentStep - TANGENT_STEP);
            Vector3 stepAfter = node.evaluateCurve(currentStep + TANGENT_STEP);
            currentStep += SIMULATION_STEP;

            if(currentStep >= 1.0) {
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

    /**
     * Changes the internal {@link TrackType} state and renders the corresponding track.
     * @param type The {@link TrackType} to be rendered.
     */
    public void changeTrackType(TrackType type) {
        trackType = type;
        setRenderable(mTrackLoader.createRenderable(trackType));
        setDirectionOrigins(getLocalEdges(type));
    }

    /**
     * Places a copy of the current track {@link com.google.ar.sceneform.rendering.Renderable} in space.
     * Adds the created {@link TrackNode} to the corresponding {@link TrackLayoutNode}.
     */
    public void placeTrack() {
        if(trackType == null) return;

        TrackNode trackNode = new TrackNode(trackType);
        trackNode.setWorldPosition(getWorldPosition());
        trackNode.setWorldRotation(getWorldRotation());
        trackNode.setDirectionOrigins(getLocalEdges(trackType));
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

    /**
     * Creates a new {@link TrackLayoutNode} and adds it to the list.
     * @param track The initial {@link TrackNode}
     */
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

    /**
     * Calculates the intersection of the {@link Camera} direction with a {@link Plane}.
     * @param plane The {@link Plane} to be checked
     * @return The intersection point, if any, null otherwise
     */
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
