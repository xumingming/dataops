package io.github.xumingming.dataops;

import io.github.xumingming.beauty.Beauty;
import io.github.xumingming.beauty.Color;
import io.github.xumingming.beauty.Column;
import picocli.CommandLine;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.Callable;

import static io.github.xumingming.beauty.Beauty.draw;
import static io.github.xumingming.beauty.Beauty.drawError;
import static io.github.xumingming.dataops.Utils.read;

@CommandLine.Command(name = "run", description = "run sql", subcommands = CommandLine.HelpCommand.class)
public class RunCommand
        implements Callable<Integer>
{
    @CommandLine.Parameters(index = "0", description = "path of the sql to run")
    private String sqlPath;

    @CommandLine.Parameters(index = "1", defaultValue = "1", description = "parallelism")
    private int parallelism;

    @CommandLine.Option(
            names = {"-p", "--placeholders"},
            description = "placeholders")
    private String placeholders;

    @CommandLine.Option(
            names = {"-v", "--verbose"},
            description = "whether show verbose(detailed) information",
            defaultValue = "false")
    private boolean verbose;

    @Override
    public Integer call()
            throws Exception
    {
        ConfManager confManager = ConfManager.create();
        DataOpsConf conf = confManager.readDataOpsConf();

        String str = Beauty.detail(conf, Arrays.asList(Column.column("Host", (DataOpsConf theConf) -> conf.getHost()), Column.column("Db", (DataOpsConf theConf) -> conf.getDb()), Column.column("User", (DataOpsConf theConf) -> conf.getUser()), Column.column("Password", (DataOpsConf theConf) -> conf.getPassword())), Color.NONE);
        System.out.println(str);

        List<PlaceholderInfo> placeholderInfos = new ArrayList<>();
        if (placeholders != null && !placeholders.isEmpty()) {
            placeholderInfos = parsePlaceholders(placeholders);
        }

        List<RunSqlTask> tasks = new ArrayList<>(parallelism);
        ConnectionInfo conn = new ConnectionInfo(conf.getHost(), 3306, conf.getDb(), conf.getUser(), conf.getPassword());
        for (int i = 0; i < parallelism; i++) {
            String sql = read(sqlPath);
            sql = rewrite(sql, placeholderInfos);
            if (verbose) {
                draw(String.format("Thread %s will run SQL: %s", i, sql));
            }

            RunSqlTask task = new RunSqlTask("Thread" + i, conn, sql);
            tasks.add(task);
            task.start();
        }

        for (int i = 0; i < parallelism; i++) {
            tasks.get(i).join();
        }

        return 0;
    }

    public static class PlaceholderInfo
    {
        private String key;
        private int start;
        private int end;

        public PlaceholderInfo(String key, int start, int end)
        {
            this.key = key;
            this.start = start;
            this.end = end;
        }

        public String getKey()
        {
            return key;
        }

        public void setKey(String key)
        {
            this.key = key;
        }

        public int getStart()
        {
            return start;
        }

        public void setStart(int start)
        {
            this.start = start;
        }

        public int getEnd()
        {
            return end;
        }

        public void setEnd(int end)
        {
            this.end = end;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            PlaceholderInfo that = (PlaceholderInfo) o;
            return start == that.start && end == that.end && key.equals(that.key);
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(key, start, end);
        }

        @Override
        public String toString()
        {
            return "PlaceholderInfo{" +
                    "key='" + key + '\'' +
                    ", start=" + start +
                    ", end=" + end +
                    '}';
        }
    }

    public static List<PlaceholderInfo> parsePlaceholders(String placeholders)
    {
        String[] topParts = placeholders.split(";");
        List<PlaceholderInfo> ret = new ArrayList<>();
        for (String topPart : topParts) {
            String[] secondParts = topPart.split(":");
            if (secondParts.length != 2) {
                throw new RuntimeException("Invalid placeholder: " + placeholders);
            }

            String key = secondParts[0].trim();
            String value = secondParts[1].trim();
            String[] valueParts = value.split(",");
            if (valueParts.length != 2) {
                throw new RuntimeException("Invalid placeholder: " + placeholders);
            }

            int start = Integer.parseInt(valueParts[0].trim());
            int end = Integer.parseInt(valueParts[1].trim());

            ret.add(new PlaceholderInfo(key, start, end));
        }

        return ret;
    }

    public static String rewrite(String sql, List<PlaceholderInfo> placeholderInfos)
    {
        String ret = sql;
        for (PlaceholderInfo info : placeholderInfos) {
            int value = info.start + new Random().nextInt(info.end - info.start);
            ret = ret.replaceAll("__" + info.key + "__", String.valueOf(value));
        }

        return ret;
    }

    public static class RunSqlTask
            extends Thread
    {
        private final String name;
        private final ConnectionInfo connInfo;
        private final String sql;

        public RunSqlTask(String name, ConnectionInfo connInfo, String sql)
        {
            this.name = name;
            this.connInfo = connInfo;
            this.sql = sql;
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
                while (counter < 1000000) {
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

        public Optional<String> runSql(Statement stmt, String sql)
        {
            try (ResultSet resultSet = stmt.executeQuery(sql)) {
                while (resultSet.next()) {
                    // Empty.
                }
                return Optional.empty();
            }
            catch (Exception e) {
                e.printStackTrace();
                return Optional.of(e.getMessage());
            }
        }
    }
}
