package com.ifetch.cq.tools;

import com.ifetch.cq.model.DbType;
import com.intellij.ide.plugins.cl.PluginClassLoader;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.impl.libraries.ProjectLibraryTable;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.File;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.Iterator;

/**
 * XML based config file help class
 * <p>
 * Created by Owen on 6/16/16.
 */
public class ConfigHelper {

    private static final Logger _LOG = Logger.getInstance(ConfigHelper.class);

    private static final String JAR = ".jar";

    public static String findConnectorLibPath(Project project, DbType type) {
        VirtualFile urlFile = getJarPath(project, type.getConnectorJarFile());
        if (urlFile != null) {
            try {
                String url = URLDecoder.decode(urlFile.getCanonicalPath(), Charset.forName("UTF-8").displayName());
                int jarIndex = url.lastIndexOf(JAR);
                if (url.length() > (jarIndex + JAR.length())) {
                    return url.substring(0, jarIndex + JAR.length());
                }
                return url;
            } catch (Exception e) {
                throw new RuntimeException("找不到驱动文件，请联系开发者", e);
            }
        } else {
            throw new RuntimeException("未找到 " + type + "的驱动包，请在项目中引入" + type.getConnectorJarFile() + JAR);
        }
    }

    /**
     * 获取项目的jar文件
     *
     * @param project
     * @param jarName
     * @return
     */
    public static VirtualFile getJarPath(Project project, String jarName) {
        LibraryTable table = ProjectLibraryTable.getInstance(project);
        Iterator<Library> it = table.getLibraryIterator();
        VirtualFile urlFile = null;
        while (it.hasNext()) {
            Library library = it.next();
            VirtualFile file = library.getFiles(OrderRootType.CLASSES)[0];
            if (file != null && file.getCanonicalPath().contains(jarName)) {
                urlFile = file;
                break;
            }
        }
        return urlFile;
    }

    public static Class loadClass(Project project, DbType dbType, PluginClassLoader loader) {
        if (loader == null || dbType == null) {
            return null;
        }
        String url = findConnectorLibPath(project, dbType);
        if (!StringTools.isEmpty(url)) {
            try {
                File file = new File(url);
                loader.addURL(file.toURL());
                return loader.loadClass(dbType.getDriverClass());
            } catch (Exception e) {
                _LOG.error("class loader fail ." + url + "not found", e);
            }
        }
        return null;
    }
}
