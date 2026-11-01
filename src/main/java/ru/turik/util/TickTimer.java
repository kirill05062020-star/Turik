package ru.turik.util;

/** Simple millis-based cooldown timer used across modules. */
public final class TickTimer {
    private long last = -1;

    public boolean elapsed(long ms) {
        long now = System.currentTimeMillis();
        if (last < 0 || now - last >= ms) {
            last = now;
            return true;
        }
        return false;
    }

    public boolean elapsedOnce(long ms) {
        if (last < 0) {
            last = System.currentTimeMillis();
            return false;
        }
        return elapsed(ms);
    }

    public void reset() {
        last = System.currentTimeMillis();
    }
}
