package io.github.xumingming.dataops;

public interface ConfManager
{
    static ConfManager create()
    {
        return new DefaultConfManager();
    }

    DataOpsConf readDataOpsConf();
}
