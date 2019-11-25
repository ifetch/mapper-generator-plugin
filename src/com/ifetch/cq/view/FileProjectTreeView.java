package com.ifetch.cq.view;

import com.intellij.icons.AllIcons;
import com.intellij.ide.IdeEventQueue;
import com.intellij.ide.SaveAndSyncHandler;
import com.intellij.ide.SaveAndSyncHandlerImpl;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.ide.util.treeView.NodeRenderer;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationActivationListener;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.fileChooser.*;
import com.intellij.openapi.fileChooser.ex.*;
import com.intellij.openapi.fileChooser.impl.FileChooserFactoryImpl;
import com.intellij.openapi.fileChooser.impl.FileChooserUtil;
import com.intellij.openapi.project.DumbModePermission;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Iconable;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.IdeFrame;
import com.intellij.ui.ClickListener;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.UIBundle;
import com.intellij.ui.components.JBList;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.ArrayUtil;
import com.intellij.util.Consumer;
import com.intellij.util.IconUtil;
import com.intellij.util.containers.HashMap;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.update.MergingUpdateQueue;
import com.intellij.util.ui.update.UiNotifyConnector;
import com.intellij.util.ui.update.Update;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by cq on 19-11-7.
 */

@SuppressWarnings("all")
public class FileProjectTreeView extends DialogWrapper implements FileChooserDialog, PathChooserDialog, FileLookup {

    private Project myProject;

    private String chooseFilePath;

    public static final String DRAG_N_DROP_HINT =
            "<html><center><small><font color=gray>Drag and drop a file into the space above to quickly locate it in the tree</font></small></center></html>";
    private final Map<String, LocalFileSystem.WatchRequest> myRequests = new HashMap<String, LocalFileSystem.WatchRequest>();

    @NonNls
    public static final String FILE_CHOOSER_SHOW_PATH_PROPERTY = "FileChooser.ShowPath";
    public static final String RECENT_FILES_KEY = "file.chooser.recent.files";
    private final FileChooserDescriptor myChooserDescriptor;
    protected FileSystemTreeImpl myFileSystemTree;

    private VirtualFile[] myChosenFiles = VirtualFile.EMPTY_ARRAY;
    private TextFieldAction myTextFieldAction;
    protected FileTextFieldImpl myPathTextField;
    private JComponent myPathTextFieldWrapper;

    private MergingUpdateQueue myUiUpdater;
    private boolean myTreeIsUpdating;
    public static DataKey<PathField> PATH_FIELD = DataKey.create("PathField");


    public FileProjectTreeView(Project project, FileChooserDescriptor descriptor) {
        super(project, true);
        this.myProject = project;
        myChooserDescriptor = descriptor;
        setTitle(getChooserTitle(descriptor));
        init();
    }

    public FileProjectTreeView(@NotNull final FileChooserDescriptor descriptor, @NotNull Component parent, @Nullable Project project) {
        super(parent, true);
        myChooserDescriptor = descriptor;
        this.myProject = project;
        setTitle(getChooserTitle(descriptor));
    }


    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        JPanel panel = new MyPanel();

        myUiUpdater = new MergingUpdateQueue("FileChooserUpdater", 200, false, panel);
        Disposer.register(myDisposable, myUiUpdater);
        new UiNotifyConnector(panel, myUiUpdater);

        panel.setBorder(JBUI.Borders.empty());
        createTree();
        final JPanel toolbarPanel = new JPanel(new BorderLayout());

        myPathTextFieldWrapper = new JPanel(new BorderLayout());
        myPathTextFieldWrapper.setBorder(JBUI.Borders.emptyBottom(2));
        myPathTextField = new FileTextFieldImpl.Vfs(
                FileChooserFactoryImpl.getMacroMap(), getDisposable(),
                new LocalFsFinder.FileChooserFilter(myChooserDescriptor, myFileSystemTree)) {
            protected void onTextChanged(final String newValue) {
                myUiUpdater.cancelAllUpdates();
                updateTreeFromPath(newValue);
            }
        };
        Disposer.register(myDisposable, myPathTextField);
        myPathTextFieldWrapper.add(myPathTextField.getField(), BorderLayout.CENTER);
        if (getRecentFiles().length > 0) {
            myPathTextFieldWrapper.add(createHistoryButton(), BorderLayout.EAST);
        }
        JScrollPane scrollPane = ScrollPaneFactory.createScrollPane(myFileSystemTree.getTree());
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.setPreferredSize(JBUI.size(240));
        panel.add(new JLabel(DRAG_N_DROP_HINT, SwingConstants.CENTER), BorderLayout.SOUTH);
        ApplicationManager.getApplication().getMessageBus().connect(getDisposable())
                .subscribe(ApplicationActivationListener.TOPIC, new ApplicationActivationListener.Adapter() {
                    @Override
                    public void applicationActivated(IdeFrame ideFrame) {
                        DumbService.allowStartingDumbModeInside(DumbModePermission.MAY_START_MODAL, new Runnable() {
                            @Override
                            public void run() {
                                ((SaveAndSyncHandlerImpl) SaveAndSyncHandler.getInstance()).maybeRefresh(ModalityState.current());
                            }
                        });
                    }
                });
        return panel;
    }

    @NotNull
    @Override
    public VirtualFile[] choose(@Nullable VirtualFile toSelect, @Nullable Project project) {
        if (toSelect == null) {
            return choose(project);
        }
        return choose(project, toSelect);
    }

    @NotNull
    @Override
    public VirtualFile[] choose(@Nullable Project project, @NotNull VirtualFile... toSelect) {
        init();
        if ((myProject == null) && (project != null)) {
            myProject = project;
        }
        if (toSelect.length == 1) {
            restoreSelection(toSelect[0]);
        } else if (toSelect.length == 0) {
            restoreSelection(null); // select last opened file
        } else {
            selectInTree(toSelect, true);
        }
        show();
        return myChosenFiles;
    }

    @Override
    public void choose(@Nullable VirtualFile toSelect, @NotNull Consumer<List<VirtualFile>> callback) {
        init();
        restoreSelection(toSelect);
        show();
        if (myChosenFiles.length > 0) {
            callback.consume(Arrays.asList(myChosenFiles));
        } else if (callback instanceof FileChooser.FileChooserConsumer) {
            ((FileChooser.FileChooserConsumer) callback).cancelled();
        }
    }

    @NotNull
    private String[] getRecentFiles() {
        final String[] recent = PropertiesComponent.getInstance().getValues(RECENT_FILES_KEY);
        if (recent != null) {
            if (recent.length > 0 && myPathTextField.getField().getText().replace('\\', '/').equals(recent[0])) {
                final String[] pathes = new String[recent.length - 1];
                System.arraycopy(recent, 1, pathes, 0, recent.length - 1);
                return pathes;
            }
            return recent;
        }
        return ArrayUtil.EMPTY_STRING_ARRAY;
    }

    protected void restoreSelection(@Nullable VirtualFile toSelect) {
        final VirtualFile lastOpenedFile = FileChooserUtil.getLastOpenedFile(myProject);
        final VirtualFile file = FileChooserUtil.getFileToSelect(myChooserDescriptor, myProject, toSelect, lastOpenedFile);

        if (file != null && file.isValid()) {
            myFileSystemTree.select(file, new Runnable() {
                public void run() {
                    if (!file.equals(myFileSystemTree.getSelectedFile())) {
                        VirtualFile parent = file.getParent();
                        if (parent != null) {
                            myFileSystemTree.select(parent, null);
                        }
                    } else if (file.isDirectory()) {
                        myFileSystemTree.expand(file, null);
                    }
                }
            });
        }
    }

    private void selectInTree(final VirtualFile vFile, String fromText) {
        if (vFile != null && vFile.isValid()) {
            if (fromText == null || fromText.equalsIgnoreCase(myPathTextField.getTextFieldText())) {
                selectInTree(new VirtualFile[]{vFile}, false);
            }
        } else {
            reportFileNotFound();
        }
    }

    private void selectInTree(final VirtualFile[] array, final boolean requestFocus) {
        myTreeIsUpdating = true;
        final List<VirtualFile> fileList = Arrays.asList(array);
        if (!Arrays.asList(myFileSystemTree.getSelectedFiles()).containsAll(fileList)) {
            myFileSystemTree.select(array, new Runnable() {
                public void run() {
                    if (!myFileSystemTree.areHiddensShown() && !Arrays.asList(myFileSystemTree.getSelectedFiles()).containsAll(fileList)) {
                        myFileSystemTree.showHiddens(true);
                        selectInTree(array, requestFocus);
                        return;
                    }

                    myTreeIsUpdating = false;
                    setErrorText(null);
                    if (requestFocus) {
                        //noinspection SSBasedInspection
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                myFileSystemTree.getTree().requestFocus();
                            }
                        });
                    }
                }
            });
        } else {
            myTreeIsUpdating = false;
            setErrorText(null);
        }
    }

    protected JTree createTree() {
        Tree internalTree = createInternalTree();
        myFileSystemTree = new FileSystemTreeImpl(myProject, myChooserDescriptor, internalTree, null, null, null);
        internalTree.setRootVisible(myChooserDescriptor.isTreeRootVisible());
        internalTree.setShowsRootHandles(true);
        Disposer.register(myDisposable, myFileSystemTree);

        myFileSystemTree.addOkAction(new Runnable() {
            public void run() {
                doOKAction();
            }
        });
        JTree tree = myFileSystemTree.getTree();
        tree.setRootVisible(true);
        tree.setCellRenderer(new NodeRenderer());
        tree.getSelectionModel().addTreeSelectionListener(new FileTreeSelectionListener());
        tree.addTreeExpansionListener(new FileTreeExpansionListener());
        setOKActionEnabled(false);

        myFileSystemTree.addListener(new FileSystemTree.Listener() {
            public void selectionChanged(final List<VirtualFile> selection) {
                updatePathFromTree(selection, false);
            }
        }, myDisposable);

        new FileDrop(tree, new FileDrop.Target() {
            public FileChooserDescriptor getDescriptor() {
                return myChooserDescriptor;
            }

            public boolean isHiddenShown() {
                return myFileSystemTree.areHiddensShown();
            }

            public void dropFiles(final List<VirtualFile> files) {
                if (!myChooserDescriptor.isChooseMultiple() && files.size() > 0) {
                    selectInTree(new VirtualFile[]{files.get(0)}, true);
                } else {
                    selectInTree(VfsUtilCore.toVirtualFileArray(files), true);
                }
            }
        });
        return tree;
    }

    protected JComponent createHistoryButton() {
        JLabel label = new JLabel(AllIcons.Actions.Get);
        label.setToolTipText("Recent files");
        new ClickListener() {
            @Override
            public boolean onClick(@NotNull MouseEvent event, int clickCount) {
                showRecentFilesPopup();
                return true;
            }
        }.installOn(label);

        new AnAction() {
            @Override
            public void actionPerformed(AnActionEvent e) {
                showRecentFilesPopup();
            }

            @Override
            public void update(AnActionEvent e) {
                e.getPresentation().setEnabled(!IdeEventQueue.getInstance().isPopupActive());
            }
        }.registerCustomShortcutSet(KeyEvent.VK_DOWN, 0, myPathTextField.getField());
        return label;
    }

    private void registerFileChooserShortcut(@NonNls final String baseActionId, @NonNls final String fileChooserActionId) {
        final JTree tree = myFileSystemTree.getTree();
        final AnAction syncAction = ActionManager.getInstance().getAction(fileChooserActionId);

        AnAction original = ActionManager.getInstance().getAction(baseActionId);
        syncAction.registerCustomShortcutSet(original.getShortcutSet(), tree, myDisposable);
    }

    private static String getChooserTitle(final FileChooserDescriptor descriptor) {
        final String title = descriptor.getTitle();
        return title != null ? title : UIBundle.message("file.chooser.default.title");
    }

    private void showRecentFilesPopup() {
        final JBList files = new JBList(getRecentFiles()) {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(myPathTextField.getField().getWidth(), super.getPreferredSize().height);
            }
        };
        files.setCellRenderer(new ColoredListCellRenderer() {
            @Override
            protected void customizeCellRenderer(JList list, Object value, int index, boolean selected, boolean hasFocus) {
                final String path = value.toString();
                append(path);
                final VirtualFile file = LocalFileSystem.getInstance().findFileByIoFile(new File(path));
                if (file != null) {
                    setIcon(IconUtil.getIcon(file, Iconable.ICON_FLAG_READ_STATUS, null));
                }
            }
        });
        JBPopupFactory.getInstance()
                .createListPopupBuilder(files)
                .setItemChoosenCallback(new Runnable() {
                    @Override
                    public void run() {
                        myPathTextField.getField().setText(files.getSelectedValue().toString());
                    }
                }).createPopup().showUnderneathOf(myPathTextField.getField());
    }

    private void updateTreeFromPath(final String text) {
        if (!isToShowTextField()) return;
        if (myPathTextField.isPathUpdating()) return;
        if (text == null) return;

        myUiUpdater.queue(new Update("treeFromPath.1") {
            public void run() {
                ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
                    public void run() {
                        final LocalFsFinder.VfsFile toFind = (LocalFsFinder.VfsFile) myPathTextField.getFile();
                        if (toFind == null || !toFind.exists()) return;

                        myUiUpdater.queue(new Update("treeFromPath.2") {
                            public void run() {
                                selectInTree(toFind.getFile(), text);
                            }
                        });
                    }
                });
            }
        });
    }

    private static boolean isToShowTextField() {
        return PropertiesComponent.getInstance().getBoolean(FILE_CHOOSER_SHOW_PATH_PROPERTY, true);
    }

    private void reportFileNotFound() {
        myTreeIsUpdating = false;
        setErrorText(null);
    }

    private final class FileTreeExpansionListener implements TreeExpansionListener {
        public void treeExpanded(TreeExpansionEvent event) {
            final Object[] path = event.getPath().getPath();
            if (path.length == 2) {
                // top node has been expanded => watch disk recursively
                final DefaultMutableTreeNode node = (DefaultMutableTreeNode) path[1];
                Object userObject = node.getUserObject();
                if (userObject instanceof FileNodeDescriptor) {
                    final VirtualFile file = ((FileNodeDescriptor) userObject).getElement().getFile();
                    if (file != null && file.isDirectory()) {
                        final String rootPath = file.getPath();
                        if (myRequests.get(rootPath) == null) {
                            final LocalFileSystem.WatchRequest watchRequest = LocalFileSystem.getInstance().addRootToWatch(rootPath, true);
                            myRequests.put(rootPath, watchRequest);
                        }
                    }
                }
            }
        }

        public void treeCollapsed(TreeExpansionEvent event) {
        }
    }

    private final class FileTreeSelectionListener implements TreeSelectionListener {
        public void valueChanged(TreeSelectionEvent e) {
            TreePath[] paths = e.getPaths();

            boolean enabled = true;
            for (TreePath treePath : paths) {
                if (!e.isAddedPath(treePath)) {
                    continue;
                }
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) treePath.getLastPathComponent();
                Object userObject = node.getUserObject();
                if (!(userObject instanceof FileNodeDescriptor)) {
                    enabled = false;
                    break;
                }
                FileElement descriptor = ((FileNodeDescriptor) userObject).getElement();
                VirtualFile file = descriptor.getFile();
                enabled = file != null && myChooserDescriptor.isFileSelectable(file);
            }
            setOKActionEnabled(enabled);
        }
    }

    protected final class MyPanel extends JPanel implements DataProvider {
        public MyPanel() {
            super(new BorderLayout(0, 0));
        }

        public Object getData(String dataId) {
            if (CommonDataKeys.VIRTUAL_FILE_ARRAY.is(dataId)) {
                return myFileSystemTree.getSelectedFiles();
            } else if (PATH_FIELD.is(dataId)) {
                return new PathField() {
                    public void toggleVisible() {
                        toggleShowTextField();
                    }
                };
            } else if (FileSystemTree.DATA_KEY.is(dataId)) {
                return myFileSystemTree;
            }
            return myChooserDescriptor.getUserData(dataId);
        }
    }

    public void toggleShowTextField() {
        setToShowTextField(!isToShowTextField());
    }

    private static void setToShowTextField(boolean toShowTextField) {
        PropertiesComponent.getInstance().setValue(FILE_CHOOSER_SHOW_PATH_PROPERTY, Boolean.toString(toShowTextField));
    }

    @NotNull
    protected Tree createInternalTree() {
        return new Tree();
    }

    private void updatePathFromTree(final List<VirtualFile> selection, boolean now) {
        if (!isToShowTextField() || myTreeIsUpdating) return;

        String text = "";
        if (selection.size() > 0) {
            text = VfsUtil.getReadableUrl(selection.get(0));
        } else {
            final List<VirtualFile> roots = myChooserDescriptor.getRoots();
            if (!myFileSystemTree.getTree().isRootVisible() && roots.size() == 1) {
                text = VfsUtil.getReadableUrl(roots.get(0));
            }
        }

        myPathTextField.setText(text, now, new Runnable() {
            public void run() {
                myPathTextField.getField().selectAll();
                setErrorText(null);
            }
        });
    }

    @Override
    protected void doOKAction() {
        Object[] paths = myFileSystemTree.getTree().getSelectionPath().getPath();
        if (paths == null || paths.length <= 1) {
            chooseFilePath = "";
        } else {
            List<Object> objects = new ArrayList<>();
            for (int i = 1; i < paths.length; i++) {
                objects.add(paths[i]);
            }
            chooseFilePath = joinPath(objects);
        }
        super.doOKAction();
    }

    public String getChooseFilePath() {
        return chooseFilePath;
    }

    public String joinPath(List<Object> objs) {
        if (objs == null || objs.size() == 0) {
            return myProject.getBaseDir().getPath();
        }
        List<String> args = objs.stream().map(item -> item + "").collect(Collectors.toList());
        return String.join(File.separator, args);
    }


}
