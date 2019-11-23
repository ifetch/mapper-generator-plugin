package com.ifetch.cq.view;

import com.ifetch.cq.model.UITableColumnVO;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by cq on 19-11-18.
 */
public class SelectTableColumnView extends DialogWrapper {

    private List<UITableColumnVO> columnVOs;

    private List<UITableColumnVO> editorColumn;

    protected SelectTableColumnView(@Nullable Project project, List<UITableColumnVO> columnVOs) {
        super(project);
        this.columnVOs = columnVOs;
        this.setTitle("定制列");
        if (columnVOs != null && columnVOs.size() > 0) {
            editorColumn = new ArrayList<>(columnVOs.size());
            for (UITableColumnVO item : columnVOs) {
                UITableColumnVO column = new UITableColumnVO();
                column.setChecked(item.isChecked());
                column.setColumnName(item.getColumnName());
                column.setJdbcType(item.getJdbcType());
                column.setJavaType(item.getJavaType());
                column.setPropertyName(item.getPropertyName());
                column.setTypeHandle(item.getTypeHandle());
                editorColumn.add(column);
            }
        }
        init();
    }

    @Nullable
    protected JComponent createTitlePane() {
        JBLabel jbLabel = new JBLabel("1. 如果要忽略请取消列的选择");
        jbLabel.setFont(new Font(null, Font.BOLD, 12));
        JBLabel jbLabelType = new JBLabel("2. 如果要定制列的Java数据类型, 字段名或者自动类型转换双击对应的地方编辑即可");
        jbLabel.setFont(new Font(null, Font.BOLD, 12));

        BorderLayout layout = new BorderLayout();
        JBPanel topPanel = new JBPanel(layout);
        topPanel.add(jbLabel, BorderLayout.PAGE_START);
        topPanel.add(jbLabelType, BorderLayout.PAGE_END);
        return topPanel;
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        ColumnsTableView table = new ColumnsTableView(editorColumn);
        table.setStriped(true);
        return new JBScrollPane(table);
    }

    @Override
    protected void doOKAction() {
        if (editorColumn != null && editorColumn.size() > 0) {
            for (int i = 0; i < editorColumn.size(); i++) {
                UITableColumnVO oldVO = columnVOs.get(i);
                UITableColumnVO newVO = editorColumn.get(i);
                callback(newVO, oldVO);
            }
        }
        doCancelAction();
    }

    public void callback(UITableColumnVO newVO, UITableColumnVO oldVO) {
        oldVO.setChecked(newVO.isChecked());
        oldVO.setColumnName(newVO.getColumnName());
        oldVO.setJdbcType(newVO.getJdbcType());
        oldVO.setJavaType(newVO.getJavaType());
        oldVO.setPropertyName(newVO.getPropertyName());
        oldVO.setTypeHandle(newVO.getTypeHandle());
    }

    public List<UITableColumnVO> getColumnVOs() {
        return columnVOs;
    }

    @Override
    protected void dispose() {
        super.dispose();
        this.editorColumn = null;
    }
}
