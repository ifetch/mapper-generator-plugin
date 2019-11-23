package com.ifetch.cq.view;

import com.ifetch.cq.model.UITableColumnVO;
import com.intellij.openapi.ui.StripeTable;
import com.intellij.ui.BooleanTableCellRenderer;
import com.intellij.ui.table.JBTable;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;


/**
 * Created by cq on 19-11-19.
 */
public class ColumnsTableView extends JBTable {

    static final int CHECK_COLUMN = 0;
    static final int NAME_COLUMN = 1;
    static final int JDBC_TYPE_COLUMN = 2;
    static final int JAVA_TYPE_COLUMN = 3;
    static final int PROPERTY_NAME_COLUMN = 4;
    static final int TYPE_HANDLER_COLUMN = 5;

    public ColumnsTableView(List<UITableColumnVO> list) {
        ColumnTableModel model = new ColumnTableModel(list);
        this.setModel(model);
        StripeTable.apply(this);
        this.setRowHeight(20);
        initColumns();
    }

    private void initColumns() {
        initBooleanColumn(CHECK_COLUMN);
        initStringColumn(NAME_COLUMN, 92);
        initStringColumn(JDBC_TYPE_COLUMN, 78);
        initStringColumn(JAVA_TYPE_COLUMN, 78);
        initStringColumn(PROPERTY_NAME_COLUMN, 94);
        initStringColumn(TYPE_HANDLER_COLUMN, 86);
    }

    private void initBooleanColumn(int columnIndex) {
        TableColumn column = getColumnModel().getColumn(columnIndex);
        BooleanTableCellRenderer renderer = new BooleanTableCellRenderer();
        column.setCellRenderer(renderer);

        Dimension headerSize = getTableHeader().getDefaultRenderer().
                getTableCellRendererComponent(this, getModel().getColumnName(columnIndex), false, false, 0, columnIndex).
                getPreferredSize();

        column.setMaxWidth(Math.max(50, Math.max(headerSize.width, renderer.getPreferredSize().width)));
    }

    private void initStringColumn(int columnIndex, int weight) {
        TableColumn column = getColumnModel().getColumn(columnIndex);
        column.setPreferredWidth(weight);
    }

    public List<UITableColumnVO> getList() {
        return ((ColumnTableModel) this.getModel()).getList();
    }

    class ColumnTableModel extends AbstractTableModel {
        private List<UITableColumnVO> list = new ArrayList<>();

        public ColumnTableModel(List<UITableColumnVO> list) {
            this.list = list;
        }

        public void addRow(UITableColumnVO columnVO) {
            if (columnVO == null) {
                throw new IllegalArgumentException("number of table columns: " + this.list.size() + " does not match number of argument columns: " + 6);
            } else {
                int size = this.list.size();
                this.list.add(columnVO);
                this.fireTableRowsInserted(size - 1, size - 1);
            }
        }

        public Class getColumnClass(int columnIndex) {
            if (columnIndex == CHECK_COLUMN) {
                return Boolean.class;
            } else {
                return String.class;
            }
        }

        public String getColumnName(int columnIndex) {
            switch (columnIndex) {
                case CHECK_COLUMN:
                    return "Check";
                case NAME_COLUMN:
                    return "Column Name";
                case JDBC_TYPE_COLUMN:
                    return "Jdbc Type";
                case JAVA_TYPE_COLUMN:
                    return "Java Type";
                case PROPERTY_NAME_COLUMN:
                    return "Property Name";
                case TYPE_HANDLER_COLUMN:
                default:
                    return "Type Handler";
            }
        }

        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex != NAME_COLUMN && columnIndex != JDBC_TYPE_COLUMN;
        }

        @Override
        public int getRowCount() {
            return list == null ? 0 : list.size();
        }


        @Override
        public int getColumnCount() {
            return 6;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if (list == null || list.size() <= rowIndex) {
                return null;
            }
            UITableColumnVO columnVO = list.get(rowIndex);
            if (columnVO == null) {
                return null;
            }
            return getValue(columnIndex, columnVO);
        }

        public void setValueAt(Object value, int rowIndex, int columnIndex) {
            if (value == null) {
                return;
            }
            UITableColumnVO columnVO = list.get(rowIndex);
            setValue(columnIndex, value, columnVO);
        }


        /**
         * 获取单列的具体值
         *
         * @param columnIndex
         * @param columnVO
         * @return
         */
        private Object getValue(int columnIndex, UITableColumnVO columnVO) {
            if (columnVO == null) {
                return null;
            }
            switch (columnIndex) {
                case CHECK_COLUMN:
                    return columnVO.isChecked();
                case NAME_COLUMN:
                    return columnVO.getColumnName();
                case JDBC_TYPE_COLUMN:
                    return columnVO.getJdbcType();
                case JAVA_TYPE_COLUMN:
                    return columnVO.getJavaType();
                case PROPERTY_NAME_COLUMN:
                    return columnVO.getPropertyName();
                case TYPE_HANDLER_COLUMN:
                default:
                    return columnVO.getTypeHandle();
            }
        }

        private void setValue(int columnIndex, Object value, UITableColumnVO columnVO) {
            if (columnVO == null) {
                return;
            }
            switch (columnIndex) {
                case CHECK_COLUMN:
                    if (value instanceof Boolean) {
                        columnVO.setChecked((Boolean) value);
                    }
                    break;
                case NAME_COLUMN:
                    columnVO.setColumnName(value + "");
                    break;
                case JDBC_TYPE_COLUMN:
                    columnVO.setJdbcType(value + "");
                    break;
                case JAVA_TYPE_COLUMN:
                    columnVO.setJavaType(value + "");
                    break;
                case PROPERTY_NAME_COLUMN:
                    columnVO.setPropertyName(value + "");
                    break;
                case TYPE_HANDLER_COLUMN:
                default:
                    columnVO.setTypeHandle(value + "");
            }
        }

        public List<UITableColumnVO> getList() {
            return list;
        }
    }
}
