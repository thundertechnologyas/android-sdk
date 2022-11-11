package com.linhua.locky.callback;

public enum PackageSignalType {
    PulseOpen("pulseopenpackage"),
    ForcedOpen("forcedopenpackage"),
    ForcedClosed("forcedclosedpackage"),
    NormalState("normalstatepackage");
    private final String name;

    private PackageSignalType(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
