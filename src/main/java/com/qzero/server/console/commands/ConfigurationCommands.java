package com.qzero.server.console.commands;

import com.qzero.server.config.GlobalConfigurationManager;
import com.qzero.server.config.authorize.AdminConfig;
import com.qzero.server.config.authorize.AuthorizeConfigurationManager;
import com.qzero.server.config.mcga.MCGAConfigurationManager;
import com.qzero.server.config.minecraft.MinecraftServerConfiguration;
import com.qzero.server.config.minecraft.MinecraftServerConfigurator;
import com.qzero.server.console.ServerCommandContext;
import com.qzero.server.utils.SHA256Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

public class ConfigurationCommands {

    private Logger log= LoggerFactory.getLogger(getClass());

    private GlobalConfigurationManager configurationManager;

    public ConfigurationCommands(){
        configurationManager=GlobalConfigurationManager.getInstance();
    }

    @CommandMethod(commandName = "auto_config",needServerSelected = false,parameterCount = 1)
    private String autoConfig(String[] commandParts, String commandLine, ServerCommandContext context){
        try {
            MinecraftServerConfigurator.configServer(commandParts[1]);
            return "Auto config successfully";
        } catch (IOException e) {
            log.error("Failed to auto config server for "+context.getCurrentServer(),e);
            return "Failed to auto config server for "+context.getCurrentServer();
        }
    }

    @CommandMethod(commandName = "reload",needServerSelected = false)
    private String reloadConfiguration(String[] commandParts, String commandLine, ServerCommandContext context){
        try {
            configurationManager.loadConfig();
            return "Reload successfully";
        }catch (Exception e){
            log.error("Failed to reload server configuration",e);
            return "Failed to reload\n"+e.getMessage();
        }
    }

    @CommandMethod(commandName = "show_all_admins",needServerSelected = false)
    private String showAllAdmins(String[] commandParts, String commandLine, ServerCommandContext context){
        if(context.getOperatorId()==null)
            return "Haven't logged in yet";

        AuthorizeConfigurationManager authorizeConfigurationManager=configurationManager.getAuthorizeConfigurationManager();
        if(authorizeConfigurationManager.getAdminConfig(context.getOperatorId())==null)
            return "You are not one of the admins";

        Set<String> adminNameSet=authorizeConfigurationManager.getAdminNameList();
        if(adminNameSet==null || adminNameSet.isEmpty())
            return "No in-game admin";

        StringBuffer stringBuffer=new StringBuffer();
        for(String op:adminNameSet){
            stringBuffer.append(op);
            stringBuffer.append("\n");
        }

        return stringBuffer.toString();
    }

    @CommandMethod(commandName = "remove_admin",needServerSelected = false,parameterCount = 1)
    private String removeAdmin(String[] commandParts, String commandLine, ServerCommandContext context){
        if(context.getOperatorId()==null)
            return "Haven't logged in yet";

        AuthorizeConfigurationManager authorizeConfigurationManager=configurationManager.getAuthorizeConfigurationManager();
        AdminConfig adminConfig=authorizeConfigurationManager.getAdminConfig(context.getOperatorId());
        if(adminConfig==null)
            return "You are not one of the admins";

        if(adminConfig.getAdminLevelInInt()<2)
            return "You have no permission";

        if(context.getOperatorId().equals(commandParts[1]))
            return "You can not remove yourself";

        if(authorizeConfigurationManager.getAdminConfig(commandParts[1])==null)
            return String.format("%s is not an admin, can not remove it", commandParts[1]);

        try {
            authorizeConfigurationManager.removeAdmin(commandParts[1]);
            return "Remove successfully";
        } catch (Exception e) {
            log.error("Failed to remove in-game op "+commandParts[1],e);
            return "Failed to remove in-game op "+commandParts[1];
        }
    }

    @CommandMethod(commandName = "add_admin",needServerSelected = false,parameterCount = 4)
    private String addAdmin(String[] commandParts, String commandLine, ServerCommandContext context){
        if(context.getOperatorId()==null)
            return "Haven't logged in yet";

        AuthorizeConfigurationManager authorizeConfigurationManager=configurationManager.getAuthorizeConfigurationManager();
        AdminConfig adminConfig=authorizeConfigurationManager.getAdminConfig(context.getOperatorId());
        if(adminConfig==null)
            return "You are not one of the admins";

        if(adminConfig.getAdminLevelInInt()<2)
            return "You have no permission";

        if(authorizeConfigurationManager.getAdminConfig(commandParts[1])!=null)
            return String.format("%s is already an op, can not add it again", commandParts[1]);

        if(!commandParts[3].equals(commandParts[4]))
            return "The two passwords do not match, please check";

        try {
            AdminConfig config=new AdminConfig();
            config.setAdminLevel(commandParts[2]);
            config.setPasswordHash(SHA256Utils.getHexEncodedSHA256(commandParts[3]));
            authorizeConfigurationManager.saveAdmin(commandParts[1],config);
            return "Add successfully";
        } catch (IOException e) {
            log.error("Failed to add in-game op "+commandParts[1],e);
            return "Failed to add in-game op "+commandParts[1];
        }
    }

    /**
     * update_admin_password admin_name new_password confirm_new_password
     */
    @CommandMethod(commandName = "update_admin_password",needServerSelected = false,parameterCount = 3)
    private String updateAdminPassword(String[] commandParts, String commandLine, ServerCommandContext context){
        if(context.getOperatorId()==null)
            return "Haven't logged in yet";

        AuthorizeConfigurationManager authorizeConfigurationManager=configurationManager.getAuthorizeConfigurationManager();
        AdminConfig adminConfig=authorizeConfigurationManager.getAdminConfig(context.getOperatorId());
        if(adminConfig==null)
            return "You are not one of the admins";

        if(adminConfig.getAdminLevelInInt()<2)
            return "You have no permission";

        String adminName=commandParts[1];
        AdminConfig config=authorizeConfigurationManager.getAdminConfig(adminName);
        if(config==null)
            return "No admin named "+adminName;

        if(!commandParts[2].equals(commandParts[3]))
            return "The two passwords do not match, please check";

        config.setPasswordHash(SHA256Utils.getHexEncodedSHA256(commandParts[2]));
        try {
            authorizeConfigurationManager.saveAdmin(adminName,config);
            return "Update password successfully";
        }catch (Exception e){
            log.error("Failed to update admin password for "+adminName,e);
            return "Failed to update admin password, reason: "+e.getMessage();
        }

    }

    @CommandMethod(commandName = "show_server_config")
    private String showServerConfig(String[] commandParts, String commandLine, ServerCommandContext context){
        MinecraftServerConfiguration configuration=configurationManager.getServerConfigurationManager().getMinecraftServerConfig(context.getCurrentServer());
        StringBuffer result=new StringBuffer();
        result.append(String.format("serverJarFileName=%s\n", configuration.getServerJarFileName()));
        result.append(String.format("javaPath=%s\n", configuration.getJavaPath()));
        result.append(String.format("javaParameter=%s\n", configuration.getJavaParameter()));
        result.append(String.format("autoConfigCopy=%s\n", configuration.getAutoConfigCopy()));

        Map<String,String> customized=configuration.getCustomizedServerProperties();
        Set<String> keySet=customized.keySet();
        for(String key:keySet){
            result.append(key);
            result.append("=");
            result.append(customized.get(key));
            result.append("\n");
        }

        return result.toString();
    }

    @CommandMethod(commandName = "update_server_config",parameterCount = 2)
    private String updateServerConfig(String[] commandParts, String commandLine, ServerCommandContext context){
        String key=commandParts[1];
        String value=commandParts[2];

        try {
            configurationManager.getServerConfigurationManager().updateMinecraftServerConfig(context.getCurrentServer(),key,value);
            return "Update successfully, please reload to apply it";
        } catch (IOException e) {
            log.error("Failed to update server config for "+context.getCurrentServer(),e);
            return "Failed to update server config for "+context.getCurrentServer();
        }
    }

    @CommandMethod(commandName = "add_server",needServerSelected = false,parameterCount = 1)
    private String addServer(String[] commandParts, String commandLine, ServerCommandContext context){
        String serverName=commandParts[1];
        if(configurationManager.getServerConfigurationManager().getMinecraftServerConfig(serverName)!=null)
            return String.format("Server named %s already exists, can not add it", serverName);

        new File(serverName+"/").mkdirs();
        File configFile=new File(serverName+"/serverConfig.config");
        try {
            configFile.createNewFile();
            return "Server created successfully, please reload to apply it";
        } catch (IOException e) {
            log.error("Failed to create config file for new server "+serverName,e);
            return "Failed to create config file for new server "+serverName;
        }
    }

    @CommandMethod(commandName = "update_mcga_config",needServerSelected = false,parameterCount = 2)
    private String updateMCGAConfig(String[] commandParts, String commandLine, ServerCommandContext context){
        String key=commandParts[1];
        String value=commandParts[2];

        try {
            MCGAConfigurationManager configurationManager=GlobalConfigurationManager.getInstance().getMcgaConfigurationManager();
            configurationManager.updateMCGAConfiguration(key,value);

            return "MCGA configuration has been updated successfully, please restart MCGA to apply it";
        }catch (Exception e){
            log.error(String.format("Failed to update mcga config (%s -> %s)", key,value),e);
            return "Failed to update mcga config";
        }
    }

    @CommandMethod(commandName = "show_mcga_config",needServerSelected = false)
    private String showMCGAConfig(String[] commandParts, String commandLine, ServerCommandContext context){
        MCGAConfigurationManager configurationManager=GlobalConfigurationManager.getInstance().getMcgaConfigurationManager();
        Map<String,String> config=configurationManager.getMcgaConfiguration().getMcgaConfig();

        StringBuffer result=new StringBuffer();
        Set<String> keySet=config.keySet();
        for(String key:keySet){
            result.append(key);
            result.append("=");
            result.append(config.get(key));
            result.append("\n");
        }

        return result.toString();
    }

}
