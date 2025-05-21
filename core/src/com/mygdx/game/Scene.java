package com.mygdx.game;

import com.badlogic.gdx.scenes.scene2d.Actor;
import java.util.ArrayList;

public class Scene extends Actor {
    private ArrayList<SceneSegment> segmentList;
    private int index;

    public Scene() {
        super();
        segmentList = new ArrayList<SceneSegment>();
        index = -1;
    }

    public void addSegment(SceneSegment segment) {
        segmentList.add(segment);
    }

    public void clearSegments() {
        segmentList.clear();
        index = -1; // Reset index when clearing
    }

    public void start() {
        if (!segmentList.isEmpty()) { // Check if list is not empty
            index = 0;
            segmentList.get(index).start();
        } else {
            index = -1; // Ensure index is invalid if list is empty
        }
    }

    @Override
    public void act(float dt) {
        // Check index validity before accessing segmentList
        if (index >= 0 && index < segmentList.size()) {
            if (isSegmentFinished() && !isLastSegment()) {
                loadNextSegment();
            }
        }
        // Note: Base Actor's act() is not called via super.act(dt) here.
        // This might be intentional based on the original code's design,
        // but Scene itself usually doesn't need per-frame actions like movement.
    }

    public boolean isSegmentFinished() {
        // Check index validity
        if (index >= 0 && index < segmentList.size()) {
            return segmentList.get(index).isFinished();
        }
        return true; // Consider it finished if index is invalid or list is empty
    }

    public boolean isLastSegment() {
        // Check index validity
        return (index < 0 || index >= segmentList.size() - 1);
    }

    public void loadNextSegment() {
        if (isLastSegment()) {
            return;
        }
        // Finish current before moving to next
        if (index >= 0 && index < segmentList.size()) {
            segmentList.get(index).finish();
        }
        index++;
        if (index < segmentList.size()) { // Check again after increment
            segmentList.get(index).start();
        }
    }

    /**
     * NEW METHOD: Finishes the currently active segment immediately.
     */
    public void finishCurrentSegment() {
        if (index >= 0 && index < segmentList.size()) {
            segmentList.get(index).finish();
            System.out.println("Explicitly finishing segment index: " + index);
        }
    }


    public boolean isSceneFinished() {
        return (isLastSegment() && isSegmentFinished());
    }

    // --- Added for debugging/access if needed ---
    /**
     * Gets the current segment index. Returns -1 if not started or invalid.
     */
    public int getCurrentSegmentIndex() {
        return index;
    }

    /**
     * Gets the list of segments. Use with caution.
     */
    public ArrayList<SceneSegment> getSegmentList() {
        return segmentList;
    }
    // ----------------------------------------
}