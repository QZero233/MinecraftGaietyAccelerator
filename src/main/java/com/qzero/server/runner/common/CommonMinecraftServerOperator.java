package com.qzero.server.runner.common;

import com.qzero.server.config.GlobalConfigurationManager;
import com.qzero.server.config.minecraft.MinecraftEnvironmentChecker;
import com.qzero.server.config.minecraft.MinecraftServerConfiguration;
import com.qzero.server.exception.MinecraftServerStatusException;
import com.qzero.server.runner.MinecraftRunner;
import com.qzero.server.runner.MinecraftServerOperator;
import com.qzero.server.runner.MinecraftServerOutputProcessCenter;
import com.qzero.server.runner.ServerOutputListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class CommonMinecraftServerOperator implements MinecraftServerOperator {

    private Logger log= LoggerFactory.getLogger(getClass());

    protected MinecraftRunner runner;
    protected String serverName;

    protected MinecraftServerOutputProcessCenter processCenter=MinecraftServerOutputProcessCenter.getInstance();

    public CommonMinecraftServerOperator(String serverName) {
        this.serverName=serverName;
        runner=new MinecraftRunner(serverName);
    }

    @Override
    public boolean checkServerEnvironment() {
        MinecraftServerConfiguration configuration= GlobalConfigurationManager.getInstance().
                getServerConfigurationManager().getMinecraftServerConfig(serverName);
        MinecraftEnvironmentChecker checker=new MinecraftEnvironmentChecker(configuration);
        try {
            checker.checkMinecraftServerEnvironment();
            return true;
        } catch (IOException e) {
            log.error(String.format("Check server environment for server named %s failed", serverName),e);
            return false;
        }
    }

    @Override
    public void startServer() throws IOException {
        MinecraftServerConfiguration configuration= GlobalConfigurationManager.getInstance().
                getServerConfigurationManager().getMinecraftServerConfig(serverName);
        new MinecraftEnvironmentChecker(configuration).checkMinecraftServerEnvironment();
        if(runner.getServerStatus()== MinecraftRunner.ServerStatus.RUNNING)
            throw new MinecraftServerStatusException(serverName,"stopped","running","start server again");

        runner.startServer(configuration);
    }

    @Override
    public void stopServer() {
        if(runner.getServerStatus()!= MinecraftRunner.ServerStatus.RUNNING)
            throw new MinecraftServerStatusException(serverName,"running","stopped","stop server");

        runner.stopServer();
    }

    @Override
    public void forceStopServer() {
        runner.forceStopServer();
    }

    @Override
    public MinecraftRunner.ServerStatus getServerStatus() {
        return runner.getServerStatus();
    }

    @Override
    public void sendCommand(String commandLine) {
        if(runner.getServerStatus()!= MinecraftRunner.ServerStatus.RUNNING)
            throw new MinecraftServerStatusException(serverName,"running","stopped","send command");

        runner.sendCommand(commandLine);
    }

    @Override
    public void registerOutputListener(ServerOutputListener listener) {
        processCenter.registerOutputListener(listener);
    }

    @Override
    public void unregisterOutputListener(String listenerId) {
        processCenter.unregisterOutputListener(listenerId);
    }

}
