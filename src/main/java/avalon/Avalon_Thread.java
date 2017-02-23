/*
 * Copyright (C) 2015 alchemystar, Inc. All Rights Reserved.
 */
package avalon;

/*
 * Java Mysql Proxy
 * Main binary. Just listen for connections and pass them over
 * to the proxy module
 */

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import avalon.plugin.Base;

public class Avalon_Thread implements Runnable {
    public int port;
    public boolean listening = true;
    public ServerSocket listener = null;
    public ArrayList<Base> plugins = new ArrayList<Base>();
    public Logger logger = Logger.getLogger("JMP_Thread");

    public Avalon_Thread(int port) {
        Thread.currentThread().setName("Listener: " + port);
        this.port = port;
    }

    public void run() {
        try {
            this.listener = new ServerSocket(this.port);
        } catch (IOException e) {
            this.logger.fatal("Could not listen on port " + this.port);
            System.exit(-1);
        }

        this.logger.info("Listening on " + this.port);

        String[] ps = new String[0];
        ExecutorService tp = Executors.newCachedThreadPool();

        if (Avalon.config.getProperty("plugins") != null) {
            ps = Avalon.config.getProperty("plugins").split(",");
        }

        while (this.listening) {
            plugins = new ArrayList<Base>();
            for (String p : ps) {
                try {
                    plugins.add((Base) Base.class.getClassLoader().loadClass(p.trim()).newInstance());
                    this.logger.info("Loaded plugin " + p);
                } catch (ClassNotFoundException e) {
                    this.logger.error("[" + p + "] " + e);
                    continue;
                } catch (InstantiationException e) {
                    this.logger.error("[" + p + "] " + e);
                    continue;
                } catch (IllegalAccessException e) {
                    this.logger.error("[" + p + "] " + e);
                    continue;
                }
            }
            try {
                tp.submit(new Engine(this.port, this.listener.accept(), plugins));
            } catch (IOException e) {
                this.logger.fatal("Accept fatal " + e);
                this.listening = false;
            }
        }

        try {
            tp.shutdown();
            this.listener.close();
        } catch (IOException e) {
        }

    }
}
