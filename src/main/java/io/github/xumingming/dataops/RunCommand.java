package io.github.xumingming.dataops;

import io.github.xumingming.beauty.Beauty;
import io.github.xumingming.beauty.Color;
import io.github.xumingming.beauty.Column;
import picocli.CommandLine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

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

        List<Placeholder> placeholders = new ArrayList<>();
        if (this.placeholders != null && !this.placeholders.isEmpty()) {
            placeholders = parsePlaceholders(this.placeholders);
        }

        List<SqlRunner> tasks = new ArrayList<>(parallelism);
        ConnectionInfo conn = new ConnectionInfo(conf.getHost(), 3306, conf.getDb(), conf.getUser(), conf.getPassword());
        for (int i = 0; i < parallelism; i++) {
            String sql = read(sqlPath);
            SqlRunner task = new SqlRunner("Thread" + i, conn, sql, placeholders, conf.getSqlGenMode(), verbose);
            tasks.add(task);
            task.start();
        }

        for (int i = 0; i < parallelism; i++) {
            tasks.get(i).join();
        }

        return 0;
    }

    public static List<Placeholder> parsePlaceholders(String placeholders)
    {
        String[] topParts = placeholders.split(";");
        List<Placeholder> ret = new ArrayList<>();
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

            ret.add(new Placeholder(key, start, end));
        }

        return ret;
    }
}
