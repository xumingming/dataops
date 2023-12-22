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
import java.util.Optional;
import java.util.concurrent.Callable;

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

    @Override
    public Integer call()
            throws Exception
    {
        ConfManager confManager = ConfManager.create();
        DataOpsConf conf = confManager.readDataOpsConf();

        String str = Beauty.detail(conf, Arrays.asList(Column.column("Host", (DataOpsConf theConf) -> conf.getHost()), Column.column("Db", (DataOpsConf theConf) -> conf.getDb()), Column.column("User", (DataOpsConf theConf) -> conf.getUser()), Column.column("Password", (DataOpsConf theConf) -> conf.getPassword())), Color.NONE);
        System.out.println(str);

        List<RunSqlTask> tasks = new ArrayList<>(parallelism);
        ConnectionInfo conn = new ConnectionInfo(conf.getHost(), 3306, conf.getDb(), conf.getUser(), conf.getPassword());
        for (int i = 0; i < parallelism; i++) {
            RunSqlTask task = new RunSqlTask("Thread" + i, conn, read(sqlPath));
            tasks.add(task);
            task.start();
        }

        for (int i = 0; i < parallelism; i++) {
            tasks.get(i).join();
        }

        return 0;
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
