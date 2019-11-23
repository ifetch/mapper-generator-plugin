package com.ifetch.cq.view;

import com.ifetch.cq.model.DatabaseConfig;
import com.ifetch.cq.model.DbType;
import com.ifetch.cq.operate.ConnectionOperate;
import com.ifetch.cq.operate.ConnectionOperateImpl;
import com.ifetch.cq.tools.DbTools;
import com.ifetch.cq.tools.JTextFieldTools;
import com.ifetch.cq.tools.PatternTools;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBPasswordField;
import com.intellij.ui.components.JBTextField;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Supplier;

/**
 * Created by cq on 19-10-23.
 */
public class ConnectionView extends DialogWrapper {

    private static final Logger _LOG = Logger.getInstance(ConnectionView.class);

    ConnectionOperate connectionOperate;

    private DatabaseConfig config;

    MapperGeneratorDialogImpl leftView;

    private Long id;

    private JBPanel rootPanel;

    public ConnectionView(MapperGeneratorDialogImpl leftView, DatabaseConfig config) {
        super(leftView.getContentPane(), true);
        id = null; //id要提前设置为null
        this.config = config;
        connectionOperate = new ConnectionOperateImpl();
        this.leftView = leftView;
        setTitle("数据库配置");
        init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        JBPanel panel = initRootPlan();
        panel.setLayout(constraints());
        return panel;
    }

    @Nullable
    @Override
    protected JComponent createSouthPanel() {
        //按钮
        JButton testJButton = new JButton("测试连接");
        testJButton.setName("testButton");
        JButton cancelJButton = new JButton("取消");
        cancelJButton.setName("cancelButton");
        JButton saveJButton = new JButton("保存");
        saveJButton.setName("saveButton");
        //绑定事件
        ClickButtonListener clickButtonListener = new ClickButtonListener(this);
        testJButton.addMouseListener(clickButtonListener);
        cancelJButton.addMouseListener(clickButtonListener);
        saveJButton.addMouseListener(clickButtonListener);

        JBPanel buttonJPanel = new JBPanel(new FlowLayout());
        buttonJPanel.add(cancelJButton, 0);
        buttonJPanel.add(saveJButton, 1);
        buttonJPanel.add(testJButton, 2);
        return buttonJPanel;
    }

    private JBPanel initRootPlan() {
        jbPasswordField = new JBPasswordField();
        jbPasswordField.setName("passwordField");
        jbPasswordField.setColumns(12);
        setFieldValue(config);

        rootPanel = new JBPanel();
        rootPanel.add(aliasNameJField);
        rootPanel.add(dbTypeJBox);
        rootPanel.add(hostNameJField);
        rootPanel.add(portJField);
        rootPanel.add(userNameJField);
        rootPanel.add(jbPasswordField);
        rootPanel.add(dbNameJField);
        rootPanel.add(encodeJBox);

        rootPanel.add(nameLabel);
        rootPanel.add(dbTypeLabel);
        rootPanel.add(hostLabel);
        rootPanel.add(portLabel);
        rootPanel.add(userNameLabel);
        rootPanel.add(pwdLabel);
        rootPanel.add(dbNameLabel);
        rootPanel.add(encodeLabel);
        return rootPanel;
    }

    private GridBagLayout constraints() {
        GridBagLayout layout = new GridBagLayout();
        layout.addLayoutComponent(nameLabel, cell(1, 1, 1, GridBagConstraints.EAST));
        layout.addLayoutComponent(aliasNameJField, cell(1, 2, 0, GridBagConstraints.WEST));
        layout.addLayoutComponent(dbTypeLabel, cell(2, 1, 1, GridBagConstraints.EAST));
        layout.addLayoutComponent(dbTypeJBox, cell(2, 2, 0, GridBagConstraints.WEST));
        layout.addLayoutComponent(hostLabel, cell(3, 1, 1, GridBagConstraints.EAST));
        layout.addLayoutComponent(hostNameJField, cell(3, 2, 0, GridBagConstraints.WEST));
        layout.addLayoutComponent(portLabel, cell(4, 1, 1, GridBagConstraints.EAST));
        layout.addLayoutComponent(portJField, cell(4, 2, 0, GridBagConstraints.WEST));
        layout.addLayoutComponent(userNameLabel, cell(5, 1, 1, GridBagConstraints.EAST));
        layout.addLayoutComponent(userNameJField, cell(5, 2, 0, GridBagConstraints.WEST));
        layout.addLayoutComponent(pwdLabel, cell(6, 1, 1, GridBagConstraints.EAST));
        layout.addLayoutComponent(jbPasswordField, cell(6, 2, 0, GridBagConstraints.WEST));
        layout.addLayoutComponent(dbNameLabel, cell(7, 1, 1, GridBagConstraints.EAST));
        layout.addLayoutComponent(dbNameJField, cell(7, 2, 0, GridBagConstraints.WEST));
        layout.addLayoutComponent(encodeLabel, cell(8, 1, 1, GridBagConstraints.EAST));
        layout.addLayoutComponent(encodeJBox, cell(8, 2, 0, GridBagConstraints.WEST));
        return layout;
    }

    public GridBagConstraints cell(int r, int c, int gw, int anchor) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridwidth = gw;
        constraints.gridy = r;
        constraints.gridx = c;
        constraints.anchor = anchor;
        constraints.insets = new Insets(2, 2, 2, 2);
        return constraints;
    }

    private boolean verifyParameter(DatabaseConfig model, Container container) {
        if (model.getName() == null || "".equals(model.getName())) {
            Messages.showWarningDialog(container, "请输入保存名称!", "提示");
            return false;
        }
        if (model.getDbType() == null || DbType.getByName(model.getDbType()) == null) {
            Messages.showWarningDialog(container, "请选择数据库类型", "提示");
            return false;
        }
        if (model.getHost() == null) {
            Messages.showWarningDialog(container, "请输入主机名或IP", "提示");
            return false;
        }
        if (model.getPort() == null) {
            Messages.showWarningDialog(container, "请输入端口号!", "提示");
            return false;
        }
        if (!PatternTools.verify(model.getPort(), PatternTools.portRegex)) {
            Messages.showWarningDialog(container, "请输入正确的端口号!", "提示");
            return false;
        }
        if (model.getSchema() == null || "".equals(model.getSchema())) {
            Messages.showWarningDialog(container, "请输入数据库名称!", "提示");
            return false;
        }
        if (model.getUsername() == null || "".equals(model.getUsername())) {
            Messages.showWarningDialog(container, "请选择编码!", "提示");
            return false;
        }
        if (model.getEncoding() == null || "".equals(model.getEncoding()) || "请选择".equals(model.getEncoding())) {
            Messages.showWarningDialog(container, "请选择编码!", "提示");
            return false;
        }
        return true;
    }

    private void setFieldValue(DatabaseConfig config) {
        if (config == null) {
            return;
        }
        id = config.getId();
        aliasNameJField.setText(config.getName());
        hostNameJField.setText(config.getHost());
        portJField.setText(config.getPort());
        dbNameJField.setText(config.getSchema());
        userNameJField.setText(config.getUsername());
        jbPasswordField.setText(config.getPassword());
        dbTypeJBox.setSelectedItem(config.getDbType());
        encodeJBox.setSelectedItem(config.getEncoding());
    }

    class ClickButtonListener extends MouseAdapter {

        private ConnectionView connectionView;

        public ClickButtonListener(ConnectionView connectionView) {
            this.connectionView = connectionView;
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            try {
                doWork(e);
            } catch (Exception ex) {
                _LOG.error("ConnectionView " + e.getSource() + ",err:" + ex.getMessage(), ex);
            }
        }

        private void doWork(MouseEvent e) {
            if (e.getClickCount() != 1 && e.getButton() != MouseEvent.BUTTON1) {
                return;
            }
            Object obj = e.getSource();
            if (!(obj instanceof JButton)) {
                return;
            }
            String name = ((JButton) obj).getName();
            if (name == null) {
                return;
            }
            DatabaseConfig model = supplier.get();
            boolean isTrue = true;
            if (!"cancelButton".equals(name)) {
                isTrue = verifyParameter(model, connectionView.getContentPanel());
            }
            if ("saveButton".equals(name) && isTrue) {
                boolean isSuccess;
                if (model.getId() == null || model.getId() == 0) {
                    isSuccess = connectionOperate.create(model);
                    if (isSuccess) {
                        leftView.addTreeNode(model);
                    }
                } else {
                    isSuccess = connectionOperate.edit(model);
                    if (isSuccess) {
                        leftView.editTreeNode(model);
                    }
                }
                connectionView.dispose();
            } else if ("cancelButton".equals(name)) {
                connectionView.dispose();
            } else if ("testButton".equals(name) && isTrue) {
                boolean connection = DbTools.testConnection(model);
                if (connection) {
                    Messages.showInfoMessage(connectionView.getContentPane(), "连接成功!", "提示");
                } else {
                    Messages.showErrorDialog(connectionView.getContentPane(), "连接失败!", "提示");
                }
            }
        }
    }

    static String[] dbTypes;
    JBPasswordField jbPasswordField;
    static JComboBox dbTypeJBox, encodeJBox;
    static String[] encodes = new String[]{"utf-8", "gb2312", "gbk"};
    static JBTextField aliasNameJField, hostNameJField, portJField, userNameJField, dbNameJField;
    static JBLabel nameLabel, hostLabel, portLabel, userNameLabel, pwdLabel, dbNameLabel, dbTypeLabel, encodeLabel;

    static {
        DbType[] types = DbType.values();
        dbTypes = new String[types.length + 1];
        dbTypes[0] = "请选择";
        for (int i = 1; i < types.length + 1; i++) {
            dbTypes[i] = types[i - 1].name();
        }
        dbTypeJBox = new ComboBox(dbTypes);
        dbTypeJBox.setPreferredSize(new Dimension(100, 30));
        encodeJBox = new ComboBox(encodes);
        encodeJBox.setPreferredSize(new Dimension(100, 30));

        //lable
        nameLabel = new JBLabel("保存名称");
        dbTypeLabel = new JBLabel("数据库类型");
        hostLabel = new JBLabel("主机或IP地址");
        portLabel = new JBLabel("端口号");
        userNameLabel = new JBLabel("用户名");
        pwdLabel = new JBLabel("数据库密码");
        dbNameLabel = new JBLabel("Schema/数据库");
        encodeLabel = new JBLabel("编码");
        // text field
        aliasNameJField = JTextFieldTools.createJBTextField("nameField", 12);
        hostNameJField = JTextFieldTools.createJBTextField("hostField", 12);
        portJField = JTextFieldTools.createJBTextField("portField", 12);
        userNameJField = JTextFieldTools.createJBTextField("userNameField", 12);
        dbNameJField = JTextFieldTools.createJBTextField("schemaField", 12);
    }

    Supplier<DatabaseConfig> supplier = () -> {
        DatabaseConfig config = new DatabaseConfig();
        config.setId(id);
        config.setName(aliasNameJField.getText());
        config.setHost(hostNameJField.getText());
        config.setPort(portJField.getText());
        config.setSchema(dbNameJField.getText());
        config.setUsername(userNameJField.getText());
        config.setPassword(new String(jbPasswordField.getPassword()));
        config.setDbType(dbTypeJBox.getSelectedItem().toString());
        config.setEncoding(encodeJBox.getSelectedItem().toString());
        return config;
    };
}



