package io.github.xumingming.dataops;

import static java.lang.String.format;

public class ConnectionInfo
{
    private final String host;
    private final int port;
    private final String user;
    private final String password;
    private final String db;

    public ConnectionInfo(String host, int port, String db, String user, String password)
    {
        this.host = host;
        this.port = port;
        this.user = user;
        this.password = password;
        this.db = db;
    }

    public String getHost()
    {
        return host;
    }

    public int getPort()
    {
        return port;
    }

    public String getJdbcUrl()
    {
        return format("jdbc:mysql://%s:%s/%s", host, port, db);
    }

    public String getUser()
    {
        return user;
    }

    public String getPassword()
    {
        return password;
    }

    public String getDb()
    {
        return db;
    }
}
