package io.github.xumingming.dataops;

public class DataOpsConf
{
    private String host;
    private int port;
    private String user;
    private String password;
    private String db;
    private SqlGenMode sqlGenMode;

    public String getHost()
    {
        return host;
    }

    public void setHost(final String host)
    {
        this.host = host;
    }

    public int getPort()
    {
        return port;
    }

    public void setPort(final int port)
    {
        this.port = port;
    }

    public String getUser()
    {
        return user;
    }

    public void setUser(final String user)
    {
        this.user = user;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(final String password)
    {
        this.password = password;
    }

    public String getDb()
    {
        return db;
    }

    public void setDb(final String db)
    {
        this.db = db;
    }

    public SqlGenMode getSqlGenMode()
    {
        return sqlGenMode;
    }

    public void setSqlGenMode(SqlGenMode sqlGenMode)
    {
        this.sqlGenMode = sqlGenMode;
    }
}
