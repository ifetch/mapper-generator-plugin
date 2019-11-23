package com.ifetch.cq.tools;


import com.intellij.openapi.diagnostic.Logger;

/**
 * Created by cq on 19-10-22.
 */
public class PathTools {

    private static Logger _LOG = Logger.getInstance(PathTools.class);

    private static PathTools pathTools = new PathTools();

    public String getResourcePath(String path) {
        String url = "";
        try {
            url = pathTools.getClass().getClassLoader().getResource(path).getFile();
        } catch (Exception e) {
            _LOG.error("getResourcePath path:{}" + path, e);
        }
        return url;
    }

    public static PathTools newInstance() {
        if (pathTools == null) {
            pathTools = new PathTools();
        }
        return pathTools;
    }

    public PathTools() {

    }
}
