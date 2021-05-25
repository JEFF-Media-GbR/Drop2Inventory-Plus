package de.jeff_media.drop2inventory.data;

/**
 * Represents the height and lifetime a future BoundingBox will need
 */
public class BoundingBoxPrediction {

    private final int heightNeeded;
    private final int lifetimeNeeded;

    public BoundingBoxPrediction(int heightNeeded, int lifetimeNeeded) {
        this.heightNeeded = heightNeeded;
        this.lifetimeNeeded = lifetimeNeeded;
    }

    public int getHeightNeeded() {
        return heightNeeded;
    }

    public int getLifetimeNeeded() {
        return lifetimeNeeded;
    }
}
