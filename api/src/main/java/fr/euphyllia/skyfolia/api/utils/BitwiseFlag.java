package fr.euphyllia.skyfolia.api.utils;

public abstract class BitwiseFlag {

    protected int flags = 0;

    protected boolean isFlagSet(int flag) {
        return ((this.flags & flag) == flag);
    }

    protected void setFlags(int flag, boolean value) {
        if (value) {
            this.flags |= flag;
        } else {
            this.flags &= ~flag;
        }
    }
}
