package fr.euphyllia.skyllia.api.utils;

public abstract class BitwiseFlag {

    protected long flags = 0;

    protected boolean isFlagSet(long flag) {
        return ((this.flags & flag) == flag);
    }

    protected void setFlags(long flag, boolean value) {
        if (value) {
            this.flags |= flag;
        } else {
            this.flags &= ~flag;
        }
    }
}
