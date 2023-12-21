package io.github.xumingming.dataops;

import io.github.xumingming.beauty.Beauty;
import io.github.xumingming.beauty.Color;
import io.github.xumingming.beauty.Column;
import picocli.CommandLine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

@CommandLine.Command(name = "run", description = "run sql", subcommands = CommandLine.HelpCommand.class)
public class RunCommand
        implements Callable<Integer>
{
    @CommandLine.Parameters(index = "0", description = "path of the sql to run")
    private String sqlPath;

    @CommandLine.Parameters(index = "1", defaultValue = "1", description = "parallelism")
    private int parallelism;

    public static String read(String filePath)
    {
        try (BufferedReader newReader = newReader(new File(filePath), StandardCharsets.UTF_8)) {
            List<String> lines = newReader.lines().collect(Collectors.toList());
            String sql = String.join("\n", lines);
            return sql;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static BufferedReader newReader(File file, Charset charset)
            throws FileNotFoundException
    {
        return new BufferedReader(new InputStreamReader(new FileInputStream(file), charset));
    }

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
        private final ConnectionInfo info;
        private final String sql;

        public RunSqlTask(String name, ConnectionInfo info, String sql)
        {
            this.name = name;
            this.info = info;
            this.sql = sql;
        }

        @Override
        public void run()
        {
            long counter = 0;
            while (counter < 1000000) {
                long start = System.currentTimeMillis();
                Optional<String> response = runSql(info, sql);
                String status = response.isPresent() ? response.get() : "OK";
                long end = System.currentTimeMillis();
                System.out.printf("[%s-%s] Status: %s, RT: %sms%n", name, counter++, status, (end - start));
            }
        }

        public Optional<String> runSql(ConnectionInfo conn, String sql)
        {
            try (Connection connection = DriverManager.getConnection(conn.getJdbcUrl(), conn.getUser(), conn.getPassword());
                    Statement stmt = connection.createStatement();
                    ResultSet resultSet = stmt.executeQuery(sql)) {
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
