package io.github.xumingming.dataops;

public enum SqlGenMode
{
    // Every thread share the same sql.
    PER_THREAD,
    // Each run in the same thread use different sql.
    PER_RUN
}
