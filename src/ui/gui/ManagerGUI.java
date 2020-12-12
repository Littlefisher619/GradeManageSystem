package ui.gui;

import business.StudentManager;
import model.Student;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.PrintStream;
import java.util.Scanner;

public abstract class ManagerGUI extends JPanel{
    protected static PrintStream out = System.out;
    protected static Scanner scanner = new Scanner(System.in);
    protected GridBagLayout gridBagLayout = new GridBagLayout();
    protected String[] columnTitle;
    protected String[][] data;

    protected JButton refreshBtn = new JButton("Refresh");
    protected JButton resetBtn = new JButton("Reset");
    protected JButton deleteBtn = new JButton("Delete");
    protected JButton createBtn = new JButton("Create");
    protected JButton searchBtn = new JButton("Search");
    protected JComboBox<String> searchOption =new JComboBox<>();
    protected JTextField searchField = new JTextField(10);
    protected JLabel searchTip = new JLabel("Search By:");

    protected DefaultTableModel tableModel;
    protected JTable table;
    protected JScrollPane scrollPane;

    protected GridBagConstraints gridBagConstraints=new GridBagConstraints();

    protected static Student student = null;
    protected static boolean privileged = false;

    protected void initInterface(){
        this.setLayout(gridBagLayout);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setResizingAllowed(false);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        gridBagConstraints.fill=GridBagConstraints.BOTH;
        gridBagConstraints.insets = new Insets(2, 4, 2, 4);
    }

    protected void setComponentPosition(){
        gridBagConstraints.gridx=0;
        gridBagConstraints.gridy=0;
        gridBagConstraints.gridwidth=1;
        gridBagConstraints.gridheight=1;
        gridBagLayout.setConstraints(searchTip, gridBagConstraints);

        gridBagConstraints.gridx=2;
        gridBagConstraints.gridy=0;
        gridBagConstraints.gridwidth=1;
        gridBagConstraints.gridheight=1;
        gridBagLayout.setConstraints(searchOption, gridBagConstraints);


        gridBagConstraints.gridx=3;
        gridBagConstraints.gridy=0;
        gridBagConstraints.gridwidth=1;
        gridBagConstraints.gridheight=1;
        gridBagLayout.setConstraints(searchField, gridBagConstraints);

        gridBagConstraints.gridx=4;
        gridBagConstraints.gridy=0;
        gridBagConstraints.gridwidth=2;
        gridBagConstraints.gridheight=1;
        gridBagLayout.setConstraints(searchBtn, gridBagConstraints);

        gridBagConstraints.gridx=6;
        gridBagConstraints.gridy=0;
        gridBagConstraints.gridwidth=2;
        gridBagConstraints.gridheight=1;
        gridBagLayout.setConstraints(resetBtn, gridBagConstraints);

        gridBagConstraints.gridx=0;
        gridBagConstraints.gridy=2;
        gridBagConstraints.gridwidth=8;
        gridBagConstraints.gridheight=4;
        gridBagLayout.setConstraints(scrollPane, gridBagConstraints);

        gridBagConstraints.gridx=0;
        gridBagConstraints.gridy=7;
        gridBagConstraints.gridwidth=2;
        gridBagConstraints.gridheight=1;
        gridBagLayout.setConstraints(refreshBtn, gridBagConstraints);

        gridBagConstraints.gridx=2;
        gridBagConstraints.gridy=7;
        gridBagConstraints.gridwidth=4;
        gridBagConstraints.gridheight=1;
        gridBagLayout.setConstraints(createBtn, gridBagConstraints);

        gridBagConstraints.gridx=6;
        gridBagConstraints.gridy=7;
        gridBagConstraints.gridwidth=2;
        gridBagConstraints.gridheight=1;
        gridBagLayout.setConstraints(deleteBtn, gridBagConstraints);
    }
    abstract void initDataModel();
    abstract void initSearchOptions();
    abstract void registerListeners();
    abstract void refresh();

    protected void addAllComponents(){
        add(searchTip);
        add(searchOption);
        add(searchField);
        add(searchBtn);

        add(scrollPane);

        add(refreshBtn);
        add(createBtn);
        add(deleteBtn);
        add(resetBtn);
    }

    ManagerGUI(){

        initDataModel();

        table = new JTable(tableModel);
        DefaultTableCellRenderer tcr = new DefaultTableCellRenderer();
        tcr.setHorizontalAlignment(SwingConstants.CENTER);
        table.setDefaultRenderer(Object.class, tcr);

        scrollPane = new JScrollPane(table);


        initInterface();
        initSearchOptions();

        setComponentPosition();
        addAllComponents();
        registerListeners();
//        resetBtn.doClick();
    }
}
