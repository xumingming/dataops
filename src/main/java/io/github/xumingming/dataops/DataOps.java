package io.github.xumingming.dataops;

import picocli.CommandLine;

@CommandLine.Command(name = "dataops", footer = "Copyright(c) 2023", description = "DataOps.", subcommands = {RunCommand.class, CommandLine.HelpCommand.class})
public class DataOps
{
    @CommandLine.Option(names = {"-h", "--help"}, usageHelp = true, description = "display this help message")
    boolean usageHelpRequested;

    private DataOps()
    {
    }

    public static void main(String[] args)
    {
        CommandLine commandLine = new CommandLine(new DataOps());
        commandLine.parseArgs(args);
        if (commandLine.isUsageHelpRequested()) {
            commandLine.usage(System.out);
            return;
        }

        int exitCode = commandLine.execute(args);
        System.exit(exitCode);
    }
}
