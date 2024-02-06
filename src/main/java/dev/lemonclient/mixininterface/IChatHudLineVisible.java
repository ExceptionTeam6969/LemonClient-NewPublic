package dev.lemonclient.mixininterface;

public interface IChatHudLineVisible extends IChatHudLine {
    boolean meteor$isStartOfEntry();

    void meteor$setStartOfEntry(boolean start);
}
