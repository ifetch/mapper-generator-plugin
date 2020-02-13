package com.ifetch.cq.view;

import com.ifetch.cq.model.DatabaseConfig;
import com.ifetch.cq.model.GeneratorConfig;
import com.ifetch.cq.model.Result;
import com.ifetch.cq.tools.JTextFieldTools;
import com.ifetch.cq.tools.StringTools;
import com.intellij.ide.ui.search.SearchUtil;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.impl.scopes.ModuleWithDependenciesScope;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.*;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.structuralsearch.plugin.ui.Configuration;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.TreeSpeedSearch;
import com.intellij.ui.components.*;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.ui.UIUtil;
import com.sun.istack.internal.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by cq on 19-11-22.
 */
@SuppressWarnings("all")
public abstract class MapperGeneratorDialog extends DialogWrapper {

    private static final Logger _LOG = Logger.getInstance(MapperGeneratorDialog.class);

    protected Project myProject;

    Tree dbTree;

    JBPopupMenu dbMenu;

    JBScrollPane leftPane, rightPane;

//    protected ComboBox<String> boxEncode;

    protected static final JBLabel sEntity, sMapper, sXml, sOther;

    protected static final JSeparator jEntity, jMapper, jXml, jOther;

    protected ComboBox<Module> mEntityPath, mInterfacePath, mXmlPath;

    protected JButton file1, file2, file3, bMadeColumn, bGeneratorCode, dbConnectionBtn;

    protected JBCheckBox cUseExample, cPage, cComment, cCoverXml, cLombokPlugin, cGeneratorMethod, cUseSchema, cForUpdate, cDaoAtonnotation, cDaoPublicMethod, cJsr310, cGeneratorAtonnotation, cTryColumn;

    protected JBTextField iTableName, iId, iEntityName, iEntityPackage, iInterfaceName, iInterfacePackage, iXmlPackage;

    protected static final JBLabel lTableName, lId, lEntityName, lEntityPath, lInterfaceName, lInterFacePath, lXmlPath, lEntityPackage, lInterfacePackage, lXmlPackage;

    static {
        jEntity = new JSeparator();
        jMapper = new JSeparator();
        jXml = new JSeparator();
        jOther = new JSeparator();

        lEntityPath = new JBLabel("选择模块");
        lInterFacePath = new JBLabel("选择模块");
        lXmlPath = new JBLabel("选择模块");

        lEntityPackage = new JBLabel("类包路径");
        lInterfacePackage = new JBLabel("接口包路径");
        lXmlPackage = new JBLabel("xml包路径");

        lTableName = new JBLabel("表名");
        lId = new JBLabel("主键(选填)");
        lEntityName = new JBLabel("实体类名");
        lInterfaceName = new JBLabel("接口名称");

        sEntity = new JBLabel("Entity");
        sMapper = new JBLabel("Interface");
        sXml = new JBLabel("Xml");
        sOther = new JBLabel("Option");
    }

    public MapperGeneratorDialog(Project project) {
        super(project, false);
        this.myProject = project;
        //初始化对象
        renderForm(getModules());
        setTitle("生成mapper文件");
        setSize(840, 800);
    }

    @Nullable
    @Override
    protected JComponent createNorthPanel() {
        dbConnectionBtn = new JButton("数据库连接");
        dbConnectionBtn.setFont(new Font("宋体", Font.BOLD, 14));

        // 创建 一个工具栏实例
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setOrientation(JToolBar.HORIZONTAL);
        toolBar.setBorderPainted(true);
        toolBar.setMargin(new Insets(4, 10, 4, 0));
        // 创建 工具栏按钮
        dbConnectionBtn.addMouseListener(new AddConnectionListener());
        // 添加 按钮 到 工具栏
        toolBar.setComponentZOrder(dbConnectionBtn, 0);
        return toolBar;
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        JBSplitter splitter = new JBSplitter("main_view", 0.25F);
        splitter.setFirstComponent(getLeftPane(configs()));
        splitter.setSecondComponent(getRightPane());
        return splitter;
    }

    @Nullable
    @Override
    protected JComponent createSouthPanel() {
        return new JBPanel<>();
    }

    public JComponent getLeftPane(@NotNull List<DatabaseConfig> configs) {
        loadTree(configs);
        return leftPane;
    }

    public JComponent getRightPane() {
        return this.rightPane;
    }

    /**
     * 获取表单值
     *
     * @return
     */
    public GeneratorConfig getGeneratorConfigFromUI() {
        GeneratorConfig config = new GeneratorConfig();
        config.setId(iId.getText());
        config.setTableName(iTableName.getText());
        config.setProjectPath(myProject.getBasePath());
        config.setEntityName(iEntityName.getText());
        config.setEntityPath(getSelectModuleUrl(mEntityPath, true));
        config.setEntityPackage(iEntityPackage.getText());
        config.setMapperName(iInterfaceName.getText());
        config.setMapperPath(getSelectModuleUrl(mInterfacePath, true));
        config.setMapperPackage(iInterfacePackage.getText());
        config.setXmlPath(getSelectModuleUrl(mXmlPath, false));
        config.setXmlPackage(iXmlPackage.getText());

        config.setEncoding("utf-8");
        config.setUseExample(cUseExample.isSelected());
        config.setNeedPage(cPage.isSelected());
        config.setNeedComment(cComment.isSelected());
        config.setCoverXml(cCoverXml.isSelected());
        config.setUseLombokPlugin(cLombokPlugin.isSelected());
        config.setNeedToStringHashcodeEquals(cGeneratorMethod.isSelected());
        config.setUseJSR310(cJsr310.isSelected());
        config.setUseForUpdate(cForUpdate.isSelected());
        config.setUseDaoPublicMethod(cDaoPublicMethod.isSelected());
        config.setDaoRepository(cDaoAtonnotation.isSelected());
        config.setJpaAnnotation(cGeneratorAtonnotation.isSelected());
        config.setUseActualColumnNames(cTryColumn.isSelected());
        config.setUseSchema(cUseSchema.isSelected());
        return config;
    }


    public void renderForm(Module[] modules) {
        JBPanel jPanel = new JBPanel();
        //创建右边控件对象
        mEntityPath = new ComboBox<>(modules);
        mInterfacePath = new ComboBox<>(modules);
        mXmlPath = new ComboBox<>(modules);
        iTableName = JTextFieldTools.createJBTextField("tableName", false);
        iId = JTextFieldTools.createJBTextField("id");
        iEntityName = JTextFieldTools.createJBTextField("entityName");
        iEntityPackage = JTextFieldTools.createJBTextField("entityPath");
        iInterfaceName = JTextFieldTools.createJBTextField("interfaceName");
        iInterfacePackage = JTextFieldTools.createJBTextField("interfacePath");
        iXmlPackage = JTextFieldTools.createJBTextField("xmlPath");

        cUseExample = new JBCheckBox("使用Example");// newObject
        cPage = new JBCheckBox("分页(仅支持MySql和PostgreSql)", true);
        cPage.setEnabled(cUseExample.isSelected());
        cUseExample.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() instanceof JBCheckBox) {
                    boolean isSelect = ((JBCheckBox) e.getSource()).isSelected();
                    cPage.setEnabled(isSelect);
                }
            }
        });
        cComment = new JBCheckBox("生成实体类注释(表注释)", true);
        cCoverXml = new JBCheckBox("覆盖原XML", true);

        cLombokPlugin = new JBCheckBox("LombokPlugin");
        cGeneratorMethod = new JBCheckBox("生成toString,equals,hasCode方法", true);
        cUseSchema = new JBCheckBox("使用schema前缀");

        cForUpdate = new JBCheckBox("select增加ForUpdate");
        cJsr310 = new JBCheckBox("JSR310:Date And Time API", true);

        cDaoAtonnotation = new JBCheckBox("Mapper使用@Repository注解");
        cDaoPublicMethod = new JBCheckBox("Mapper方法抽象到父类", true);

        cGeneratorAtonnotation = new JBCheckBox("生成JPA注解");
        cTryColumn = new JBCheckBox("使用实际列名");

        file1 = new JButton("选择");
        file2 = new JButton("选择");
        file3 = new JButton("选择");
        bMadeColumn = new JButton("定制列");
        bGeneratorCode = new JButton("代码生成");

        //控件绑定事件
        bMadeColumn.addActionListener(new MadeColumnListener());
        iEntityName.getDocument().addDocumentListener(new ChangeInterfaceNameListener());
        ChooseFilePathListener pathListener = new ChooseFilePathListener();
        file1.addActionListener(pathListener);
        file2.addActionListener(pathListener);
        file3.addActionListener(pathListener);
        bGeneratorCode.addActionListener(new GeneratorCodeListener());

        //将右边控件添加到 Jpane容器中
        jPanel.add(lId);
        jPanel.add(lTableName);
        jPanel.add(lEntityName);
        jPanel.add(lEntityPath);
        jPanel.add(lEntityPackage);
        jPanel.add(lInterfaceName);
        jPanel.add(lInterFacePath);
        jPanel.add(lInterfacePackage);
        jPanel.add(lXmlPath);
        jPanel.add(lXmlPackage);

        jPanel.add(iId);
        jPanel.add(iTableName);
        jPanel.add(iEntityName);
        jPanel.add(mEntityPath);
        jPanel.add(iEntityPackage);
        jPanel.add(iInterfaceName);
        jPanel.add(mInterfacePath);
        jPanel.add(iInterfacePackage);
        jPanel.add(mXmlPath);
        jPanel.add(iXmlPackage);

        jPanel.add(jEntity);
        jPanel.add(jMapper);
        jPanel.add(jXml);
        jPanel.add(jOther);
        jPanel.add(sEntity);
        jPanel.add(sOther);
        jPanel.add(sMapper);
        jPanel.add(sXml);
        jPanel.add(bMadeColumn);

        jPanel.add(cUseExample);
        jPanel.add(cPage);

        jPanel.add(cComment);
        jPanel.add(cCoverXml);

        jPanel.add(cLombokPlugin);
        jPanel.add(cUseSchema);
        jPanel.add(cGeneratorMethod);

        jPanel.add(cJsr310);
        jPanel.add(cForUpdate);

        jPanel.add(cDaoPublicMethod);
        jPanel.add(cDaoAtonnotation);

        jPanel.add(cGeneratorAtonnotation);
        jPanel.add(cTryColumn);

        jPanel.add(bGeneratorCode);

        jPanel.add(file1);
        jPanel.add(file2);
        jPanel.add(file3);

        //设置页面布局
        GridBagLayout layout = new GridBagLayout();
        createRightFormHeader(jPanel, layout);
        layout.setConstraints(lId, cell(1, 2, 6, GridBagConstraints.EAST));
        layout.setConstraints(iId, cell(1, 8, 16, GridBagConstraints.WEST));

        layout.setConstraints(lTableName, cell(2, 2, 6, GridBagConstraints.EAST));
        layout.setConstraints(iTableName, cell(2, 8, 16, GridBagConstraints.WEST));
        layout.setConstraints(bMadeColumn, cell(2, 24, 10, GridBagConstraints.WEST));

        layout.setConstraints(sEntity, cell(3, 0, 3, GridBagConstraints.EAST));
        layout.setConstraints(jEntity, cell(3, 3, 0));

        layout.setConstraints(lEntityName, cell(4, 2, 6, GridBagConstraints.EAST));
        layout.setConstraints(iEntityName, cell(4, 8, 16, GridBagConstraints.WEST));

        layout.setConstraints(lEntityPath, cell(5, 2, 6, GridBagConstraints.EAST));
        layout.setConstraints(mEntityPath, cell(5, 8, 17, GridBagConstraints.WEST));

        layout.setConstraints(lEntityPackage, cell(6, 2, 6, GridBagConstraints.EAST));
        layout.setConstraints(iEntityPackage, cell(6, 8, 16, GridBagConstraints.WEST));
        layout.setConstraints(file1, cell(6, 24, 7, GridBagConstraints.WEST));

        layout.setConstraints(sMapper, cell(7, 0, 5, GridBagConstraints.EAST));
        layout.setConstraints(jMapper, cell(7, 5, 0));

        layout.setConstraints(lInterfaceName, cell(8, 2, 6, GridBagConstraints.EAST));
        layout.setConstraints(iInterfaceName, cell(8, 8, 16, GridBagConstraints.WEST));

        layout.setConstraints(lInterFacePath, cell(9, 2, 6, GridBagConstraints.EAST));
        layout.setConstraints(mInterfacePath, cell(9, 8, 17, GridBagConstraints.WEST));

        layout.setConstraints(lInterfacePackage, cell(10, 2, 6, GridBagConstraints.EAST));
        layout.setConstraints(iInterfacePackage, cell(10, 8, 16, GridBagConstraints.WEST));
        layout.setConstraints(file2, cell(10, 24, 7, GridBagConstraints.WEST));

        layout.setConstraints(sXml, cell(11, 0, 2, GridBagConstraints.EAST));
        layout.setConstraints(jXml, cell(11, 2, 0));

        layout.setConstraints(lXmlPath, cell(12, 2, 6, GridBagConstraints.EAST));
        layout.setConstraints(mXmlPath, cell(12, 8, 17, GridBagConstraints.WEST));

        layout.setConstraints(lXmlPackage, cell(13, 2, 6, GridBagConstraints.EAST));
        layout.setConstraints(iXmlPackage, cell(13, 8, 16, GridBagConstraints.WEST));
        layout.setConstraints(file3, cell(13, 24, 7, GridBagConstraints.WEST));

        layout.setConstraints(sOther, cell(14, 0, 3, GridBagConstraints.EAST));
        layout.setConstraints(jOther, cell(14, 3, 0));

        layout.setConstraints(cUseExample, cell(15, 8, 10, GridBagConstraints.WEST));
        layout.setConstraints(cPage, cell(15, 18, 20, GridBagConstraints.WEST));

        layout.setConstraints(cComment, cell(16, 8, 14, GridBagConstraints.WEST));
        layout.setConstraints(cCoverXml, cell(16, 22, 10, GridBagConstraints.WEST));

        layout.setConstraints(cLombokPlugin, cell(17, 8, 10, GridBagConstraints.WEST));
        layout.setConstraints(cGeneratorMethod, cell(17, 18, 20, GridBagConstraints.WEST));

        layout.setConstraints(cJsr310, cell(18, 8, 20, GridBagConstraints.WEST));
        layout.setConstraints(cForUpdate, cell(18, 24, 16, GridBagConstraints.WEST));

        layout.setConstraints(cDaoPublicMethod, cell(19, 8, 16, GridBagConstraints.WEST));
        layout.setConstraints(cDaoAtonnotation, cell(19, 22, 20, GridBagConstraints.WEST));

        layout.setConstraints(cGeneratorAtonnotation, cell(20, 8, 10, GridBagConstraints.WEST));
        layout.setConstraints(cTryColumn, cell(20, 16, 15, GridBagConstraints.WEST));
        layout.setConstraints(cUseSchema, cell(20, 24, 12, GridBagConstraints.WEST));

        layout.setConstraints(bGeneratorCode, cell(21, 16, 20, GridBagConstraints.WEST));

        jPanel.setLayout(layout);
        rightPane = new JBScrollPane(jPanel);
        rightPane.setSize(450, 520);
    }

    /**
     * 加载树节点
     */
    public void loadTree(@NotNull List<DatabaseConfig> configs) {
        final DefaultMutableTreeNode root = new DefaultMutableTreeNode(null);
        for (DatabaseConfig item : configs) {
            final DefaultMutableTreeNode node = new DefaultMutableTreeNode(item);
            node.setAllowsChildren(true);
            root.add(node);
        }
        DefaultTreeModel patternTreeModel = new DefaultTreeModel(root);
        dbTree = createTree(patternTreeModel);
        leftPane = new JBScrollPane(dbTree);
    }

    /**
     * 创建数据库连接菜单
     *
     * @param expanded 撑开
     * @return
     */
    public JBPopupMenu createDBConnectionMenu(boolean expanded) {

        JBMenuItem closeMenu = new JBMenuItem("关闭连接");
        JBMenuItem openMenu = new JBMenuItem("打开连接");
        JBMenuItem editMenu = new JBMenuItem("编辑连接");
        JBMenuItem delMenu = new JBMenuItem("删除连接");

        ActionListener listener = new ClickDBMenuListener();
        closeMenu.addActionListener(listener);
        openMenu.addActionListener(listener);
        editMenu.addActionListener(listener);
        delMenu.addActionListener(listener);

        JBPopupMenu jbPopupMenu = new JBPopupMenu();
        if (expanded) {
            jbPopupMenu.add(closeMenu);
        } else {
            jbPopupMenu.add(openMenu);
        }
        jbPopupMenu.add(editMenu);
        jbPopupMenu.add(delMenu);
        return jbPopupMenu;
    }

    /**
     * 创建树
     *
     * @param treeModel
     * @return
     */
    private Tree createTree(TreeModel treeModel) {
        final Tree tree = new Tree(treeModel);
        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        tree.setDragEnabled(false);
        tree.setEditable(false);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.addMouseListener(new DBTreeClickListener());
        final TreeSpeedSearch speedSearch = new TreeSpeedSearch(
                tree, (object) -> {
            final Object userObject = ((DefaultMutableTreeNode) object.getLastPathComponent()).getUserObject();
            return (userObject instanceof Configuration) ? ((Configuration) userObject).getName() : userObject.toString();
        });
        tree.setCellRenderer(new ExistingTemplatesTreeCellRenderer(speedSearch));
        return tree;
    }

    /**
     * 添加数据库
     *
     * @param config
     * @return
     */
    protected boolean addTreeNode(DatabaseConfig config) {
        DefaultTreeModel treeModel = (DefaultTreeModel) dbTree.getModel();
        DefaultMutableTreeNode parent = ((DefaultMutableTreeNode) treeModel.getRoot());
        DefaultMutableTreeNode newChild = new DefaultMutableTreeNode(config);
        treeModel.insertNodeInto(newChild, parent, parent.getChildCount());
        treeModel.reload();
        return true;
    }

    /**
     * 编辑节点
     *
     * @return
     */
    protected boolean editTreeNode(DatabaseConfig config) {
        DefaultTreeModel treeModel = (DefaultTreeModel) dbTree.getModel();
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) dbTree.getSelectionPath().getLastPathComponent();
        node.setUserObject(config);
        treeModel.reload();
        return true;
    }

    protected boolean removeNode(DefaultTreeModel parentModel, DefaultMutableTreeNode treeNode) {
        parentModel.removeNodeFromParent(treeNode);
        parentModel.reload();
        return true;
    }

    class AddConnectionListener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            addConnection();
        }
    }

    /**
     * 点击操作数据库 菜单事件
     */
    class ClickDBMenuListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            System.out.println("点击菜单");
            String command = e.getActionCommand();
            DefaultTreeModel rootModel = (DefaultTreeModel) dbTree.getModel();
            TreePath treePath = dbTree.getSelectionPath();
            DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) treePath.getLastPathComponent();
            Object obj = treeNode.getUserObject();
            if (!(obj instanceof DatabaseConfig)) {
                return;
            }
            DatabaseConfig config = (DatabaseConfig) obj;
            Result<Boolean> result;
            switch (command) {
                case "关闭连接":
                    treeNode.removeAllChildren();
                    dbTree.collapsePath(treePath);
                    break;
                case "打开连接":
                    result = openConnection(config, treeNode);
                    if (result.getT()) {
                        dbTree.updateUI();
                        dbTree.expandPath(treePath);
                    } else {
                        Messages.showErrorDialog(getContentPanel(), result.getDesc(), "提示");
                    }
                    break;
                case "编辑连接":
                    editConnection(config);
                    break;
                case "删除连接":
                    int show = Messages.showOkCancelDialog("是否删除连接", "删除连接", "删除", "取消", null);
                    if (show == 0) {
                        result = delConnection(config);
                        if (result.getT()) {
                            removeNode(rootModel, treeNode);
                        } else {
                            Messages.showErrorDialog(getContentPanel(), result.getDesc(), "提示");
                        }
                    }
                    break;
            }
            rootModel.reload();
        }
    }

    class DBTreeClickListener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            super.mouseClicked(e);
            JTree jTree = (JTree) e.getSource();
            if (jTree.getSelectionPath() == null) {
                return;
            }
            if (jTree.getSelectionPath().getLastPathComponent() == null) {
                return;
            }
            TreePath selectionTreePath = jTree.getSelectionPath();
            DefaultMutableTreeNode selectionTreeNode = (DefaultMutableTreeNode) selectionTreePath.getLastPathComponent();
            Object obj = selectionTreeNode.getUserObject();
            boolean isChild = selectionTreeNode.getChildCount() > 0;
            if (e.getButton() == MouseEvent.BUTTON3 && e.getClickCount() == 1 && obj instanceof DatabaseConfig) { //右击事件
                int x = e.getX();
                int y = e.getY();
                dbMenu = createDBConnectionMenu(isChild);
                dbMenu.show(dbTree, x, y);
            } else if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
                Result<Boolean> result = new Result<>(true);
                result.setT(false);
                if (!isChild && (obj instanceof DatabaseConfig)) {
                    DatabaseConfig config = (DatabaseConfig) obj;
                    result = openConnection(config, selectionTreeNode);
                    jTree.expandPath(selectionTreePath);
                    if (!result.getT()) {
                        Messages.showErrorDialog(getContentPanel(), result.getDesc(), "错误");
                    }
                } else if (obj instanceof String) {
                    TreeNode parentNode = selectionTreeNode.getParent();
                    if (parentNode == null || !(parentNode instanceof DefaultMutableTreeNode)) {
                        return;
                    }
                    DefaultMutableTreeNode parentDNode = (DefaultMutableTreeNode) parentNode;
                    Object parentObj = parentDNode.getUserObject();
                    if (parentObj == null || !(parentObj instanceof DatabaseConfig)) {
                        return;
                    }
                    String tableName = (String) obj;
                    DatabaseConfig config = (DatabaseConfig) parentObj;
                    result = getTableInfo(tableName, config);
                    if (!result.getT()) {
                        Messages.showErrorDialog(getContentPanel(), result.getDesc(), "错误");
                    }
                }

            }
        }
    }

    static class ExistingTemplatesTreeCellRenderer extends ColoredTreeCellRenderer {

        private final TreeSpeedSearch mySpeedSearch;

        ExistingTemplatesTreeCellRenderer(TreeSpeedSearch speedSearch) {
            mySpeedSearch = speedSearch;
        }

        @Override
        public void customizeCellRenderer(@org.jetbrains.annotations.NotNull JTree tree,
                                          Object value,
                                          boolean selected,
                                          boolean expanded,
                                          boolean leaf,
                                          int row,
                                          boolean hasFocus) {
            final DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) value;
            final Object userObject = treeNode.getUserObject();
            if (userObject == null) return;
            final Color background = selected ? UIUtil.getTreeSelectionBackground(hasFocus) : UIUtil.getTreeTextBackground();
            final Color foreground = selected && hasFocus ? UIUtil.getTreeSelectionForeground() : UIUtil.getTreeTextForeground();

            final String text;
            final int style;
            if (userObject instanceof DatabaseConfig) {
                text = ((DatabaseConfig) userObject).getName();
                style = SimpleTextAttributes.STYLE_BOLD;
                tree.setRowHeight(24);
                tree.setFont(new Font(null, Font.BOLD, 13));
            } else {
                text = userObject.toString();
                style = SimpleTextAttributes.STYLE_PLAIN;
                tree.setRowHeight(24);
                tree.setFont(new Font(null, Font.PLAIN, 12));
            }

            SearchUtil.appendFragments(mySpeedSearch.getEnteredPrefix(), text, style, foreground, background, this);
        }
    }

    /**
     * 定制列
     */
    class MadeColumnListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            madeTableColumn();
        }
    }

    class ChangeInterfaceNameListener implements DocumentListener {
        @Override
        public void insertUpdate(DocumentEvent e) {
            String text = iEntityName.getText();
            iInterfaceName.setText(text + "Mapper");
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            String text = iEntityName.getText();
            iInterfaceName.setText(text + "Mapper");
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            String text = iEntityName.getText();
            iInterfaceName.setText(text + "Mapper");
        }
    }

    class GeneratorCodeListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            boolean v = validateFormValue();
            if (!v) {
                return;
            }
            Result<Boolean> result = generatorCode();
            if (result.getT()) {
                Messages.showInfoMessage(getContentPanel(), result.getDesc(), "提示");
            } else {
                Messages.showErrorDialog(getContentPanel(), result.getDesc(), "提示");
            }
        }
    }

    class ChooseFilePathListener implements ActionListener {
        /**
         * Invoked when an action occurs.
         *
         * @param e
         */
        @Override
        public void actionPerformed(ActionEvent e) {
//            String srcPath = "";
            Module module = null;
            String title = "请选择";
            VirtualFile file = null;
            if (e.getSource() == file1) {
                title = lEntityPackage.getText();
                module = (Module) mEntityPath.getSelectedItem();
                file = getModuleJavaFile(module);
            } else if (e.getSource() == file2) {
                title = lInterfacePackage.getText();
                module = (Module) mInterfacePath.getSelectedItem();
                file = getModuleJavaFile(module);
            } else if (e.getSource() == file3) {
                title = lXmlPackage.getText();
                module = (Module) mXmlPath.getSelectedItem();
                file = getModuleFile(module);
            }
            FileChooserDescriptor descriptor = new MyFileChooserDescriptor();
            String baseDir = file == null ? "" : file.getPath();
            descriptor.setRoots(file);
            descriptor.setShowFileSystemRoots(false);
            descriptor.setHideIgnored(true);
            descriptor.setTitle(title);
            FileProjectTreeView treeView = new FileProjectTreeView(myProject, descriptor);
            treeView.show();
            String chooseFilePath = treeView.getChooseFilePath();
            if (chooseFilePath == null || "".equals(chooseFilePath)) {
                return;
            }
            if (e.getSource() == file1) {
                chooseFilePath = chooseFilePath.replace(File.separator, ".");
                iEntityPackage.setText(chooseFilePath);
            } else if (e.getSource() == file2) {
                chooseFilePath = chooseFilePath.replace(File.separator, ".");
                iInterfacePackage.setText(chooseFilePath);
            } else if (e.getSource() == file3) {
                iXmlPackage.setText(chooseFilePath);
            }
        }

    }

    class MyFileChooserDescriptor extends FileChooserDescriptor {

        public MyFileChooserDescriptor() {
            super(false, true, false, false, false, false);
        }

        @Override
        public FileChooserDescriptor withRoots(@org.jetbrains.annotations.NotNull List<VirtualFile> roots) {
            Class className = this.getClass();
            try {
                Class parentClass = className.getSuperclass();
                Field field = parentClass.getDeclaredField("myRoots");
                if (field == null) {
                    return this;
                }
                field.setAccessible(true);
                Object obj = field.get(this);
                if (obj == null) {
                    return this;
                }
                if (obj instanceof ArrayList) {
                    List<VirtualFile> files = (List<VirtualFile>) obj;
                    files.clear();
                    files.addAll(roots);
                }
            } catch (Exception e) {
                _LOG.error("MyFileChooserDescriptor " + e.getMessage(), e);
            }
            return this;
        }
    }

    public void createRightFormHeader(JBPanel panel, GridBagLayout layout) {
        for (int i = 0; i < 38; i++) {
            JBPanel panel1 = new JBPanel();
            panel.add(panel1);
            layout.setConstraints(panel1, cell(0, i, 1));
        }
    }

    public String getSelectModuleUrl(ComboBox<Module> comboBox, boolean flag) {
        Module module = (Module) comboBox.getSelectedItem();
        if (module == null) {
            return myProject.getBasePath();
        }
        if (flag) {
            return getModuleJavaFile(module).getPath();
        } else {
            return getModuleFile(module).getPath();
        }
    }

    private boolean validateFormValue() {
        String tableName = iTableName.getText();
        String entityName = iEntityName.getText();
        String entityPackage = iEntityPackage.getText();
        String interfaceName = iInterfaceName.getText();
        String interfacePath = iInterfacePackage.getText();
        String xmlPath = iXmlPackage.getText();

        if (StringTools.isEmpty(tableName)) {
            Messages.showWarningDialog(getContentPanel(), "请先在左侧选择数据库表", "提示");
            return false;
        } else if (StringTools.isEmpty(entityName)) {
            Messages.showWarningDialog(getContentPanel(), "请自定义Java实体类名称", "提示");
            return false;
        } else if (StringTools.isEmpty(entityPackage)) {
            Messages.showWarningDialog(getContentPanel(), "请输入实体类文件存放包路径", "提示");
            return false;
        } else if (StringTools.isEmpty(interfaceName)) {
            Messages.showWarningDialog(getContentPanel(), "请自定义接口名称", "提示");
            return false;
        } else if (StringTools.isEmpty(interfacePath)) {
            Messages.showWarningDialog(getContentPanel(), "请输入接口文件存放路径", "提示");
            return false;
        } else if (StringTools.isEmpty(xmlPath)) {
            Messages.showWarningDialog(getContentPanel(), "请选择xml文件存放路径", "提示");
            return false;
        }
        return true;
    }

    public static GridBagConstraints cell(int r, int c, int gw, int anchor) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridwidth = gw;
        constraints.gridy = r;
        constraints.gridx = c;
        constraints.anchor = anchor;
        constraints.insets = new Insets(1, 2, 1, 2);
        return constraints;
    }

    public static GridBagConstraints cell(int r, int c, int gw) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridwidth = gw;
        constraints.gridy = r;
        constraints.gridx = c;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        return constraints;
    }

    /**
     * 获取 module 的文件根路径
     *
     * @param module
     * @return
     */
    public static VirtualFile getModuleJavaFile(Module module) {
        if (module == null) {
            return null;
        }
        GlobalSearchScope scope = module.getModuleScope(false);
        if (scope != null && scope instanceof ModuleWithDependenciesScope) {
            ModuleWithDependenciesScope moduleScope = (ModuleWithDependenciesScope) scope;
            Collection<VirtualFile> directories = moduleScope.getRoots();
            if (directories != null && !directories.isEmpty()) {
                return directories.iterator().next();
            }
        }
        return null;
    }

    public static VirtualFile getModuleFile(Module module) {
        if (module == null) {
            return null;
        }
        GlobalSearchScope scope = module.getModuleContentScope();
        if (scope != null && scope instanceof ModuleWithDependenciesScope) {
            ModuleWithDependenciesScope moduleScope = (ModuleWithDependenciesScope) scope;
            Collection<VirtualFile> directories = moduleScope.getRoots();
            if (directories != null && !directories.isEmpty()) {
                return directories.iterator().next();
            }
        }
        return null;
    }

    private Module[] getModules() {
        return ModuleManager.getInstance(myProject).getModules();
    }

    abstract Result<Boolean> addConnection();

    abstract Result<Boolean> openConnection(DatabaseConfig config, DefaultMutableTreeNode treeNode);

    abstract Result<Boolean> editConnection(DatabaseConfig config);

    abstract Result<Boolean> delConnection(DatabaseConfig config);

    abstract Result<Boolean> getTableInfo(String tableName, DatabaseConfig config);

    abstract Result<Boolean> madeTableColumn();

    abstract Result<Boolean> generatorCode();

    abstract List<DatabaseConfig> configs();

}
