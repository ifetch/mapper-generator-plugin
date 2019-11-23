package com.ifetch.cq.config;

import com.ifetch.cq.model.DatabaseConfig;
import com.ifetch.cq.tools.StringTools;
import com.intellij.openapi.components.*;

import java.util.*;

import com.intellij.openapi.diagnostic.Logger;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * Created by cq on 19-10-24.
 */
@State(
        name = "mapperGeneratorPlugin",
        storages = {
                @Storage(
                        file = "$PROJECT_CONFIG_DIR$/database_config.xml"
                )})
public class DatabaseSettings implements PersistentStateComponent<Element> {

    private static final Logger _LOG = Logger.getInstance(DatabaseSettings.class);

    private static String SETTING = "setting";
    private static String CONNECTION = "connection";
    private static String ITEM = "item";

    private static String ID = "id";
    private static String NAME = "connectionName";
    private static String TYPE = "type";
    private static String HOST = "host";
    private static String PORT = "port";
    private static String USERNAME = "username";
    private static String PASSWORD = "password";
    private static String SCHEMA = "schema";
    private static String ENCODE = "encode";

    private Map<String, DatabaseConfig> connections = new HashMap<>();


    @Nullable
    @Override
    public Element getState() {
        Element element = new Element(SETTING);
        _LOG.info("getState：" + connections.toString());
        if (!connections.isEmpty()) {
            Element connectionElement = new Element(CONNECTION);
            connections.forEach((k, item) -> connectionElement.addContent(convert(item)));
            element.addContent(connectionElement);
        }
        return element;
    }

    @Override
    public void loadState(@NotNull Element element) {
        _LOG.info("loadState：" + connections.toString());
        Element connectionElement = element.getChild(CONNECTION);
        List<Element> elements = connectionElement.getChildren(ITEM);
        if (elements != null || !elements.isEmpty()) {
            for (Element item : elements) {
                DatabaseConfig config = convert(item);
                connections.put(String.valueOf(config.getName()), config);
            }
        }
    }

    public Map<String, DatabaseConfig> getConfigList() {
        return connections;
    }

    public Element convert(DatabaseConfig item) {
        Element element = new Element(ITEM);
        element.setAttribute(ID, StringTools.valueToString(item.getId()));
        element.setAttribute(NAME, StringTools.valueToString(item.getName()));
        element.setAttribute(TYPE, StringTools.valueToString(item.getDbType()));
        element.setAttribute(HOST, StringTools.valueToString(item.getHost()));
        element.setAttribute(PORT, StringTools.valueToString(item.getPort()));
        element.setAttribute(USERNAME, StringTools.valueToString(item.getUsername()));
        element.setAttribute(PASSWORD, StringTools.valueToString(item.getPassword()));
        element.setAttribute(SCHEMA, StringTools.valueToString(item.getSchema()));
        element.setAttribute(ENCODE, StringTools.valueToString(item.getEncoding()));
        return element;
    }

    public static DatabaseConfig convert(Element item) {
        String id = isEmpty(item.getAttributeValue(ID)) ? "0" : item.getAttributeValue(ID);
        String name = item.getAttributeValue(NAME);
        String type = item.getAttributeValue(TYPE);
        String host = item.getAttributeValue(HOST);
        String port = item.getAttributeValue(PORT);
        String username = item.getAttributeValue(USERNAME);
        String password = item.getAttributeValue(PASSWORD);
        String schema = item.getAttributeValue(SCHEMA);
        String encode = item.getAttributeValue(ENCODE);
        return newInstance(Long.parseLong(id), type, name, host, port, username, password, schema, encode);
    }

    public static DatabaseConfig newInstance(Long id, String type, String name, String host, String port, String username, String pwd, String schema, String encode) {
        DatabaseConfig config = new DatabaseConfig();
        config.setId(id);
        config.setDbType(type);
        config.setName(name);
        config.setHost(host);
        config.setPort(port);
        config.setUsername(username);
        config.setPassword(pwd);
        config.setSchema(schema);
        config.setEncoding(encode);
        return config;
    }

    public void setConnections(Map<String, DatabaseConfig> connections) {
        this.connections = connections;
    }

    private static boolean isEmpty(String value) {
        return value == null || "".equals(value);
    }
}
