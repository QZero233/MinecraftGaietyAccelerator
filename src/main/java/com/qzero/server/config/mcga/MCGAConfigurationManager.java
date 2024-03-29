package com.qzero.server.config.mcga;

import com.qzero.server.config.IConfigurationManager;
import com.qzero.server.utils.ConfigurationUtils;

import java.io.File;
import java.util.Map;

public class MCGAConfigurationManager implements IConfigurationManager {

    private MCGAConfiguration mcgaConfiguration;

    public static final String MCGA_CONFIG_FILE_NAME ="mcga.config";

    @Override
    public void loadConfig() throws Exception {
        File file=new File(MCGA_CONFIG_FILE_NAME);
        if(!file.exists())
            throw new IllegalStateException("MCGA config file does not exist");

        Map<String,String> config= ConfigurationUtils.readConfiguration(file);
        if(config==null)
            throw new IllegalStateException("Manager config file can not be empty");

        String enableLogOutput=config.get("enableLogOutput");
        if(enableLogOutput==null){
            enableLogOutput="false";
        }

        if(!enableLogOutput.equalsIgnoreCase("true") && !enableLogOutput.equalsIgnoreCase("false")){
            throw new IllegalArgumentException("In mcga.config, property enableLogOutput can only be true or false");
        }

        mcgaConfiguration=new MCGAConfiguration(config.get("containerName"),enableLogOutput,config);
    }

    public MCGAConfiguration getMcgaConfiguration() {
        return mcgaConfiguration;
    }

    public void updateMCGAConfiguration(String key,String value) throws Exception{
        ConfigurationUtils.updateConfiguration(new File(MCGA_CONFIG_FILE_NAME),key,value);
    }

}
