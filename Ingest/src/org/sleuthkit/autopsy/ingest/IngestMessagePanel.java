/*
 * Autopsy Forensic Browser
 *
 * Copyright 2011 Basis Technology Corp.
 * Contact: carrier <at> sleuthkit <dot> org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sleuthkit.autopsy.ingest;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import org.sleuthkit.autopsy.ingest.IngestMessage.*;
import org.sleuthkit.datamodel.BlackboardArtifact;

/**
 * Notification window showing messages from services to user
 * 
 */
class IngestMessagePanel extends javax.swing.JPanel {

    private MessageTableModel tableModel;
    private IngestMessageMainPanel mainPanel;
    private static Font visitedFont = new Font("Arial", Font.PLAIN, 11);
    private static Font notVisitedFont = new Font("Arial", Font.BOLD, 11);
    private static Color ERROR_COLOR = new Color(255, 90, 90);
    private int lastRowSelected = -1;

    /** Creates new form IngestMessagePanel */
    public IngestMessagePanel(IngestMessageMainPanel mainPanel) {
        this.mainPanel = mainPanel;
        tableModel = new MessageTableModel();
        initComponents();
        customizeComponents();
    }

    int getLastRowSelected() {
        return this.lastRowSelected;
    }

    IngestMessageGroup getSelectedMessage() {
        if (lastRowSelected < 0) {
            return null;
        }

        return tableModel.getMessageGroup(lastRowSelected);
    }

    IngestMessageGroup getMessageGroup(int rowNumber) {
        return tableModel.getMessageGroup(rowNumber);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        messageTable = new javax.swing.JTable();

        setOpaque(false);

        jScrollPane1.setOpaque(false);

        messageTable.setBackground(new java.awt.Color(221, 221, 235));
        messageTable.setFont(new java.awt.Font("Arial", 0, 10)); // NOI18N
        messageTable.setModel(tableModel);
        messageTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_LAST_COLUMN);
        messageTable.setAutoscrolls(false);
        messageTable.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        messageTable.setGridColor(new java.awt.Color(204, 204, 204));
        messageTable.setOpaque(false);
        messageTable.setSelectionForeground(new java.awt.Color(0, 0, 0));
        messageTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        messageTable.setShowHorizontalLines(false);
        messageTable.setShowVerticalLines(false);
        messageTable.getTableHeader().setReorderingAllowed(false);
        jScrollPane1.setViewportView(messageTable);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 314, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 144, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable messageTable;
    // End of variables declaration//GEN-END:variables

    private void customizeComponents() {
        mainPanel.setOpaque(true);
        jScrollPane1.setOpaque(true);
        messageTable.setOpaque(false);

        jScrollPane1.setWheelScrollingEnabled(true);

        messageTable.setAutoscrolls(false);
        //messageTable.setTableHeader(null);
        messageTable.setShowHorizontalLines(false);
        messageTable.setShowVerticalLines(false);

        messageTable.getParent().setBackground(messageTable.getBackground());

        //customize column witdhs
        //messageTable.setSize(260, 260);
        //messageTable.setSize(messageTable.getParent().getPreferredSize());
        final int width = messageTable.getSize().width;
        TableColumn column = null;
        for (int i = 0; i < 2; i++) {
            column = messageTable.getColumnModel().getColumn(i);
            if (i == 0) {
                column.setCellRenderer(new MessageTableRenderer());
                column.setPreferredWidth(((int) (width * 0.61)));
            } else if (i == 1) {
                column.setPreferredWidth(((int) (width * 0.15)));
                column.setCellRenderer(new MessageTableRenderer());
            } else {
                column.setPreferredWidth(((int) (width * 0.23)));
                column.setCellRenderer(new MessageTableRenderer());

            }
        }
        messageTable.setCellSelectionEnabled(false);
        messageTable.setColumnSelectionAllowed(false);
        messageTable.setRowSelectionAllowed(true);
        messageTable.getSelectionModel().addListSelectionListener(new MessageVisitedSelection());
    }

    public void addMessage(IngestMessage m) {
        tableModel.addMessage(m);
        //autoscroll
        //messageTable.scrollRectToVisible(messageTable.getCellRect(messageTable.getRowCount() - 1, messageTable.getColumnCount(), true));
    }

    public void clearMessages() {
        tableModel.clearMessages();
    }

    private void setVisited(int rowNumber) {
        tableModel.setVisited(rowNumber);
        lastRowSelected = rowNumber;
    }

    private class MessageTableModel extends AbstractTableModel {
        //data

        private Logger logger = Logger.getLogger(MessageTableModel.class.getName());
        private List<TableEntry> messageData = new ArrayList<TableEntry>();
        //for keeping track of messages to group, per service, by uniqness
        private Map<IngestServiceAbstract, Map<String, List<IngestMessageGroup>>> groupings = new HashMap<IngestServiceAbstract, Map<String, List<IngestMessageGroup>>>();
        private static final int MESSAGE_GROUP_THRESH = 3; //group messages after 3 messages per service with same uniqness

        MessageTableModel() {
            //initialize groupings map with services
            for (IngestServiceAbstract service : IngestManager.enumerateFsContentServices()) {
                groupings.put(service, new HashMap<String, List<IngestMessageGroup>>());
            }
            for (IngestServiceAbstract service : IngestManager.enumerateImageServices()) {
                groupings.put(service, new HashMap<String, List<IngestMessageGroup>>());
            }
        }

        @Override
        public int getColumnCount() {
            return 3;
        }

        @Override
        public int getRowCount() {
            return messageData.size();
        }

        @Override
        public String getColumnName(int column) {
            String colName = null;

            switch (column) {
                case 0:
                    colName = "Subject";
                    break;
                case 1:
                    colName = "Pri";
                    break;
                case 2:
                    colName = "Module";
                    break;
                default:
                    ;

            }
            return colName;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Object ret = null;
            TableEntry entry = messageData.get(rowIndex);

            switch (columnIndex) {
                case 0:
                    ret = (Object) entry.messageGroup.getSubject();
                    break;
                case 1:
                    ret = (Object) entry.messageGroup.getCount();
                    break;
                case 2:
                    Object service = entry.messageGroup.getSource();
                    if (service == null) {
                        ret = "";
                    } else {
                        ret = (Object) entry.messageGroup.getSource().getName();
                    }
                    break;
                default:
                    logger.log(Level.SEVERE, "Invalid table column index: " + columnIndex);
                    break;
            }
            return ret;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;
        }

        @Override
        public Class getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }

        private int getTableEntryIndex(IngestMessageGroup group) {
            int ret = -1;
            int i = 0;
            for (TableEntry e : messageData) {
                if (e.messageGroup.equals(group)) {
                    ret = i;
                    break;
                }
                ++i;
            }
            return ret;
        }

        public void addMessage(IngestMessage m) {
            //check how many messages per service with the same uniqness
            //and add to existing group or create a new group
            IngestServiceAbstract service = m.getSource();
            IngestMessageGroup messageGroup = null;
            if (service != null && m.getMessageType() == IngestMessage.MessageType.DATA) {
                //not a manager message, a data message, then group
                final Map<String, List<IngestMessageGroup>> groups = groupings.get(service);
                //groups for this uniqueness
                final String uniqueness = m.getUniqueKey();
                List<IngestMessageGroup> uniqGroups = groups.get(uniqueness);
                if (uniqGroups == null) {
                    //first one with this uniqueness
                    uniqGroups = new ArrayList<IngestMessageGroup>();
                    messageGroup = new IngestMessageGroup(m);
                    uniqGroups.add(messageGroup);
                    groups.put(uniqueness, uniqGroups);
                } else {
                    int uniqueGroupsCount = uniqGroups.size();
                    if (uniqueGroupsCount > MESSAGE_GROUP_THRESH) {
                        //merge them
                        messageGroup = uniqGroups.get(0);
                        for (int i = 1; i < uniqueGroupsCount; ++i) {
                            messageGroup.add(uniqGroups.get(i));
                        }
                        //remove merged groups
                        uniqGroups.clear();
                        uniqGroups.add(messageGroup);
                        //remove all rows, new merged row will be added to the bottom
                        int toRemove = 0;
                        while ((toRemove = getTableEntryIndex(messageGroup)) != -1) {
                            messageData.remove(toRemove);
                            //remove the row, will be added to the bottom
                            this.fireTableRowsDeleted(toRemove, toRemove); //TODO check
                        }


                    } else if (uniqueGroupsCount == 1) {
                        IngestMessageGroup first = uniqGroups.get(0);
                        //one group with multiple messages
                        if (first.getCount() > 1) {
                            //had already been merged
                            first.add(m);
                            messageGroup = first;
                            //move to bottom of table
                            //remove from existing position
                            int toRemove = 0;
                            while ((toRemove = getTableEntryIndex(messageGroup)) != -1) {
                                messageData.remove(toRemove);
                                //remove the row, will be added to the bottom
                                this.fireTableRowsDeleted(toRemove, toRemove); //TODO check
                            }
                        } else {
                            //one group with one message
                            //create another group
                            messageGroup = new IngestMessageGroup(m);
                            uniqGroups.add(messageGroup);

                        }
                    } else {
                        //multiple groups with 1 msg each
                        //create another group, until need to merge
                        messageGroup = new IngestMessageGroup(m);
                        uniqGroups.add(messageGroup);
                        //add to bottom
                    }
                }

            } else {
                //manager or non-data message
                messageGroup = new IngestMessageGroup(m);
            }

            //add new or updated row to the bottom
            messageData.add(new TableEntry(messageGroup));
            int size = messageData.size();
            this.fireTableRowsInserted(size - 1, size);
        }

        public void clearMessages() {
            messageData.clear();
            fireTableDataChanged();
        }

        public void setVisited(int rowNumber) {
            messageData.get(rowNumber).visited = true;
            //repaint the cell 
            fireTableCellUpdated(rowNumber, 0);
        }

        public void setVisitedAll() {
            int row = 0;
            for (TableEntry e : messageData) {
                if (e.visited == false) {
                    e.visited = true;
                    fireTableCellUpdated(row, 0);
                }
                ++row;
            }
        }

        public boolean isVisited(int rowNumber) {
            return messageData.get(rowNumber).visited;
        }

        public MessageType getMessageType(int rowNumber) {
            return messageData.get(rowNumber).messageGroup.getMessageType();
        }

        public IngestMessageGroup getMessageGroup(int rowNumber) {
            return messageData.get(rowNumber).messageGroup;
        }

        class TableEntry implements Comparable {

            IngestMessageGroup messageGroup;
            boolean visited;

            TableEntry(IngestMessageGroup messageGroup) {
                this.messageGroup = messageGroup;
                visited = false;
            }

            @Override
            public int compareTo(Object o) {
                return this.messageGroup.getDatePosted().compareTo(((TableEntry) o).messageGroup.getDatePosted());
            }
        }
    }

    //represents grouping of similar messages
    //with the same uniqness
    static class IngestMessageGroup {

        static Color VERY_HIGH_PRI_COLOR = new Color(5, 131, 46); //for a single message in a group
        static Color HIGH_PRI_COLOR = new Color(36, 166, 65);
        static Color MED_PRI_COLOR = new Color(55, 213, 90);
        static Color LOW_PRI_COLOR = new Color(160, 235, 177);
        List<IngestMessage> messages;
        int count;

        IngestMessageGroup(IngestMessage message) {
            messages = new ArrayList<IngestMessage>();
            messages.add(message);
            count = 1;
        }

        List<IngestMessage> getMessages() {
            return messages;
        }

        void add(IngestMessage message) {

            IngestMessage first = messages.get(0);
            //make sure uniqness agrees
            if (!message.getSource().equals(first.getSource())
                    || !message.getUniqueKey().equals(first.getUniqueKey())) {
                throw new IllegalArgumentException("Tried to add a message to a wrong message group.");
            }

            messages.add(message);
            ++count;
        }

        //add all messages from another group
        void add(IngestMessageGroup group) {

            IngestMessage first = messages.get(0);
            IngestMessage firstG = group.messages.get(0);
            //make sure uniqness agrees
            if (!firstG.getSource().equals(first.getSource())
                    || !firstG.getUniqueKey().equals(first.getUniqueKey())) {
                throw new IllegalArgumentException("Tried to add a message to a wrong message group.");
            }

            for (IngestMessage m : group.getMessages()) {
                messages.add(m);
                ++count;
            }
        }

        int getCount() {
            return count;
        }

        String getDetails() {
            StringBuilder b = new StringBuilder("");
            for (IngestMessage m : messages) {
                String details = m.getDetails();
                if (details == null || details.equals("")) {
                    continue;
                }
                b.append(details);
                b.append("<hr />");
            }

            return b.toString();
        }

        /**
         * return color corresp to priority
         * @return 
         */
        Color getColor() {
            if (count == 1) {
                return VERY_HIGH_PRI_COLOR;
            } else if (count < 5) {
                return HIGH_PRI_COLOR;
            } else if (count < 15) {
                return MED_PRI_COLOR;
            } else {
                return LOW_PRI_COLOR;
            }

        }

        /**
         * return date of the last message of the group
         * used for chrono sort
         * @return 
         */
        Date getDatePosted() {
            return messages.get(count - 1).getDatePosted();
        }

        /**
         * get subject of the first message
         * @return 
         */
        String getSubject() {
            return messages.get(0).getSubject();
        }

        /*
         * return unique key, should be the same for all msgs
         */
        String getUniqueKey() {
            return messages.get(0).getUniqueKey();
        }

        /*
         * return source service, should be the same for all msgs
         */
        IngestServiceAbstract getSource() {
            return messages.get(0).getSource();
        }

        /*
         * return data of the first message
         */
        BlackboardArtifact getData() {
            return messages.get(0).getData();
        }

        /*
         * return message type, should be the same for all msgs
         */
        IngestMessage.MessageType getMessageType() {
            return messages.get(0).getMessageType();
        }
    }

    /**
     * bold font if not visited, colors for errors
     * tooltips that show entire query string, disable selection borders
     */
    private class MessageTableRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value,
                boolean isSelected, boolean hasFocus,
                int row, int column) {

            final Component cell = super.getTableCellRendererComponent(
                    table, value, false, false, row, column);

            if (column == 0 || column == 2) {
                String val = (String) table.getModel().getValueAt(row, column);
                setToolTipText(val);
                //setText(val);
            } else if (column == 1) {
                //Integer val = (Integer) table.getModel().getValueAt(row, column);
                //setToolTipText(Integer.toString(val));
            }

            if (column == 0) {
                if (tableModel.isVisited(row)) {
                    cell.setFont(visitedFont);
                } else {
                    cell.setFont(notVisitedFont);
                }
                if (!isSelected) {
                    final IngestMessageGroup messageGroup = tableModel.getMessageGroup(row);
                    MessageType mt = messageGroup.getMessageType();
                    if (mt == MessageType.ERROR) {
                        cell.setBackground(ERROR_COLOR);
                    } else if (mt == MessageType.WARNING) {
                        cell.setBackground(Color.orange);
                    } else {
                        //cell.setBackground(table.getBackground());
                        cell.setBackground(messageGroup.getColor());
                    }
                } else {
                    super.setForeground(table.getSelectionForeground());
                    super.setBackground(table.getSelectionBackground());
                }
            }

            if (column == 1) {
                if (isSelected) {
                    super.setForeground(table.getSelectionForeground());
                    super.setBackground(table.getSelectionBackground());
                } else {
                    cell.setBackground(table.getBackground());
                }
            }

            return this;
        }

        @Override
        protected void setValue(Object value) {
            super.setValue(value);
        }
    }

    /**
     * handle table selections / cell visitations
     */
    private class MessageVisitedSelection implements ListSelectionListener {

        private Logger logger = Logger.getLogger(MessageVisitedSelection.class.getName());

        @Override
        public void valueChanged(ListSelectionEvent e) {
            DefaultListSelectionModel selModel = (DefaultListSelectionModel) e.getSource();
            if (!selModel.getValueIsAdjusting()) {
                final int minIndex = selModel.getMinSelectionIndex();
                final int maxIndex = selModel.getMaxSelectionIndex();
                int selected = -1;
                for (int i = minIndex; i <= maxIndex; i++) {
                    if (selModel.isSelectedIndex(i)) {
                        selected = i;
                        break;
                    }
                }
                if (selected != -1) {
                    setVisited(selected);
                    //check if has details
                    IngestMessageGroup m = getMessageGroup(selected);
                    String details = m.getDetails();
                    if (details != null && !details.equals("")) {
                        mainPanel.showDetails(selected);
                    }
                }


            }
        }
    }
}
