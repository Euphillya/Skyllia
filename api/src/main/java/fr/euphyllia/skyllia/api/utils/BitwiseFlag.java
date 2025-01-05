package fr.euphyllia.skyllia.api.utils;

/**
 * Provides utility methods for managing bitwise flags.
 */
public abstract class BitwiseFlag {

    /**
     * The current set of flags.
     */
    protected long flags = 0;

    /**
     * Checks if a specific flag is set.
     *
     * @param flag The flag to check.
     * @return True if the flag is set, false otherwise.
     */
    protected boolean isFlagSet(long flag) {
        return ((this.flags & flag) == flag);
    }

    /**
     * Sets or clears a specific flag.
     *
     * @param flag  The flag to set or clear.
     * @param value True to set the flag, false to clear it.
     */
    protected void setFlags(long flag, boolean value) {
        if (value) {
            this.flags |= flag;
        } else {
            this.flags &= ~flag;
        }
    }
}
