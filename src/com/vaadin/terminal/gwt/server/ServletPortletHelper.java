package com.vaadin.terminal.gwt.server;

import java.io.Serializable;

import com.vaadin.Application;
import com.vaadin.terminal.WrappedRequest;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.ui.Root;

/*
 @VaadinApache2LicenseForJavaFiles@
 */

class ServletPortletHelper implements Serializable {
    public static final String UPLOAD_URL_PREFIX = "APP/UPLOAD/";

    public static class ApplicationClassException extends Exception {

        public ApplicationClassException(String message, Throwable cause) {
            super(message, cause);
        }

        public ApplicationClassException(String message) {
            super(message);
        }
    }

    static Class<? extends Application> getApplicationClass(
            String applicationParameter, String rootParameter,
            ClassLoader classLoader) throws ApplicationClassException {
        if (applicationParameter == null) {

            // Validate the parameter value
            verifyRootClass(rootParameter, classLoader);

            // Application can be used if a valid rootLayout is defined
            return Application.class;
        }

        try {
            return (Class<? extends Application>) classLoader
                    .loadClass(applicationParameter);
        } catch (final ClassNotFoundException e) {
            throw new ApplicationClassException(
                    "Failed to load application class: " + applicationParameter,
                    e);
        }
    }

    private static void verifyRootClass(String className,
            ClassLoader classLoader) throws ApplicationClassException {
        if (className == null) {
            throw new ApplicationClassException(Application.ROOT_PARAMETER
                    + " init parameter not defined");
        }

        // Check that the root layout class can be found
        try {
            Class<?> rootClass = classLoader.loadClass(className);
            if (!Root.class.isAssignableFrom(rootClass)) {
                throw new ApplicationClassException(className
                        + " does not implement Root");
            }
            // Try finding a default constructor, else throw exception
            rootClass.getConstructor();
        } catch (ClassNotFoundException e) {
            throw new ApplicationClassException(className
                    + " could not be loaded", e);
        } catch (SecurityException e) {
            throw new ApplicationClassException("Could not access " + className
                    + " class", e);
        } catch (NoSuchMethodException e) {
            throw new ApplicationClassException(className
                    + " doesn't have a public no-args constructor");
        }
    }

    private static boolean hasPathPrefix(WrappedRequest request, String prefix) {
        String pathInfo = request.getRequestPathInfo();

        if (pathInfo == null) {
            return false;
        }

        if (!prefix.startsWith("/")) {
            prefix = '/' + prefix;
        }

        if (pathInfo.startsWith(prefix)) {
            return true;
        }

        return false;
    }

    public static boolean isFileUploadRequest(WrappedRequest request) {
        return hasPathPrefix(request, UPLOAD_URL_PREFIX);
    }

    public static boolean isConnectorResourceRequest(WrappedRequest request) {
        return hasPathPrefix(request,
                ApplicationConnection.CONNECTOR_RESOURCE_PREFIX + "/");
    }

    public static boolean isUIDLRequest(WrappedRequest request) {
        return hasPathPrefix(request, Constants.AJAX_UIDL_URI);
    }

    public static boolean isApplicationResourceRequest(WrappedRequest request) {
        return hasPathPrefix(request, "APP/");
    }

}
