package io.github.xumingming.dataops;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;

import static io.github.xumingming.dataops.Utils.getObjectMapper;

public class DefaultConfManager
        implements ConfManager
{
    private String configFile;

    public DefaultConfManager()
    {
        String userHome = System.getProperty("user.home");
        this.configFile = userHome + "/.dataops.yaml";

        if (!new File(configFile).exists()) {
            throw new RuntimeException("Config file: " + configFile + " not exists!");
        }
    }

    public DataOpsConf readDataOpsConf()
    {
        try {
            ObjectMapper mapper = getObjectMapper();
            DataOpsConf dataOpsConf = mapper.readValue(new File(configFile), DataOpsConf.class);

            if (dataOpsConf.getSqlGenMode() == null) {
                // Default to PER_RUN.
                dataOpsConf.setSqlGenMode(SqlGenMode.PER_RUN);
            }

            if (dataOpsConf.getPort() <= 0) {
                dataOpsConf.setPort(3306);
            }

            return dataOpsConf;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
