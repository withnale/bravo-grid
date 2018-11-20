package com.bravostudiodev.grid;

import com.google.gson.Gson;
import io.sterodium.rmi.protocol.MethodInvocationDto;
import io.sterodium.rmi.protocol.MethodInvocationResultDto;
import io.sterodium.rmi.protocol.server.RmiFacade;
import org.openqa.grid.internal.GridRegistry;
import org.openqa.grid.web.servlet.RegistryBasedServlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author IgorV
 *         Date: 13.2.2017
 */
public class BravoExtensionServlet extends RegistryBasedServlet {

    private static final Logger LOGGER = Logger.getLogger(BravoExtensionServlet.class.getName());

    private static final Gson GSON = new Gson();

    private final RmiFacade rmiFacade;

    public BravoExtensionServlet() {
        this(null);
    }

    public BravoExtensionServlet(GridRegistry registry) {
        super(registry);

        rmiFacade = new RmiFacade();
        try {
            rmiFacade.add("screen", new SikuliScreen());
        } catch (ExceptionInInitializerError e) {
            LOGGER.log(Level.SEVERE, "Sikuli operations are not available on this environment.", e);
            throw e;
        }
        try {
            rmiFacade.add("files", new FileTransfer());
        } catch (ExceptionInInitializerError e) {
            LOGGER.log(Level.SEVERE, "File transfer is not available on this environment.", e);
            throw e;
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String objectId = getObjectId(req);
        if (objectId == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Can't find object ID in URL string");
            return;
        }
        MethodInvocationDto method = GSON.fromJson(req.getReader(), MethodInvocationDto.class);
        MethodInvocationResultDto result = rmiFacade.invoke(objectId, method);
        resp.getWriter().write(GSON.toJson(result));
    }

    private String getObjectId(HttpServletRequest req) {
        String requestURI = req.getRequestURI();
        Pattern pattern = Pattern.compile(".+/([^/]+)");
        Matcher matcher = pattern.matcher(requestURI);
        if (!matcher.matches()) {
            return null;
        }
        return matcher.group(1);
    }
}
