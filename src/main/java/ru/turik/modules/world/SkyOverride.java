package ru.turik.modules.world;

/**
 * Contract implemented by Ambience and Atmosphere. The ClientWorld mixin
 * asks the first enabled module for a sky color override.
 */
public interface SkyOverride {

    /** RGB 0..1 or null to keep the vanilla sky. */
    float[] skyColor();
}