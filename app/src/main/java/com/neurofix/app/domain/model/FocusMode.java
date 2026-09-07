package com.neurofix.app.domain.model;

/**
 * Plain domain representation of a Focus Mode. Distinct from
 * FocusModeEntity (Room, data layer) — same separation pattern already
 * used for VaultedApp/VaultedAppEntity elsewhere in this codebase.
 */
public class FocusMode {

    private final long id;
    private final String name;
    private final boolean preset;
    private final boolean active;

    public FocusMode(long id, String name, boolean preset, boolean active) {
        this.id = id;
        this.name = name;
        this.preset = preset;
        this.active = active;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isPreset() {
        return preset;
    }

    public boolean isActive() {
        return active;
    }
}
