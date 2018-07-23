package com.opentrack.jonasfuhrmann.opentrackplanner.Track;

import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Camera;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.math.Vector3;

public class CurrentTrackNode extends AnchorNode {

    @Override
    public void onUpdate(FrameTime frameTime) {
        Camera camera = getScene().getCamera();

        Vector3 cameraPosition =  camera.getWorldPosition();
        Vector3 cameraDirection = camera.getForward();
        // calculate intersection to plane - plane is E: y=0
        Vector3 intersect = Vector3.subtract(cameraPosition, cameraDirection.scaled(cameraPosition.y/cameraDirection.y));

        setWorldPosition(intersect);
    }
}
