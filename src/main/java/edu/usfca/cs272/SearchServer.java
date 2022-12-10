package edu.usfca.cs272;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import java.io.IOException;

public class SearchServer {


    private final Server jettyServer;

    public SearchServer(ThreadSafeQueryFileHandler queryFileHandler, int port) throws IOException {
        this.jettyServer = new Server(port);

        ServletHandler servletHandler = new ServletHandler();
        servletHandler.addServletWithMapping(
                new ServletHolder(new SearchServlet(queryFileHandler)), "/search");
        this.jettyServer.setHandler(servletHandler);
    }

    public void start() throws Exception {
        this.jettyServer.start();
        this.jettyServer.join();
    }

    public void stop () throws Exception {
        this.jettyServer.stop();
    }


}
