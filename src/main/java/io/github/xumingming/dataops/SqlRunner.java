package io.github.xumingming.dataops;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import static io.github.xumingming.beauty.Beauty.draw;
import static io.github.xumingming.beauty.Beauty.drawError;

public class SqlRunner
        extends Thread
{
    private final String name;
    private final ConnectionInfo connInfo;
    private final String sqlTemplate;
    private final List<Placeholder> placeholders;
    private final boolean verbose;
    private final SqlGenMode sqlGenMode;

    private String sql;
    public SqlRunner(String name, ConnectionInfo connInfo, String sqlTemplate, List<Placeholder> placeholders, SqlGenMode sqlGenMode, boolean verbose)
    {
        this.name = name;
        this.connInfo = connInfo;
        this.sqlTemplate = sqlTemplate;
        this.placeholders = placeholders;
        this.verbose = verbose;
        this.sqlGenMode = sqlGenMode;

        if (sqlGenMode == SqlGenMode.PER_THREAD) {
            sql = rewrite(sqlTemplate, placeholders);
            if (verbose) {
                draw(String.format("%s will run SQL: %s", name, sql));
            }
        }
    }

    @Override
    public void run()
    {
        Connection connection = null;
        Statement stmt = null;
        try {
            connection = DriverManager.getConnection(connInfo.getJdbcUrl(), connInfo.getUser(), connInfo.getPassword());
            stmt = connection.createStatement();
            long counter = 0;
            while (counter < 100000000) {
                if (sqlGenMode == SqlGenMode.PER_RUN) {
                    sql = rewrite(sqlTemplate, placeholders);
                    if (verbose) {
                        draw(String.format("[%s-%s] will run SQL: %s", name, counter, sql));
                    }
                }

                long start = System.currentTimeMillis();
                Optional<String> response = runSql(stmt, sql);
                String status = response.isPresent() ? response.get() : "OK";
                long end = System.currentTimeMillis();
                System.out.printf("[%s-%s] Status: %s, RT: %sms%n", name, counter++, status, (end - start));
            }
        }
        catch (Exception e) {
            drawError(e.getMessage());
        }
        finally {
            if (stmt != null) {
                try {
                    stmt.close();
                }
                catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                }
                catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public static String rewrite(String sql, List<Placeholder> placeholders)
    {
        String ret = sql;
        for (Placeholder info : placeholders) {
            int value = info.getStart() + new Random().nextInt(info.getEnd() - info.getStart());
            ret = ret.replaceAll("__" + info.getKey() + "__", String.valueOf(value));
        }

        return ret;
    }

    public Optional<String> runSql(Statement stmt, String sql)
    {
        try (ResultSet resultSet = stmt.executeQuery(sql)) {
            while (resultSet.next()) {
                // Empty.
            }
            return Optional.empty();
        }
        catch (Exception e) {
            return Optional.of(e.getMessage());
        }
    }
}
