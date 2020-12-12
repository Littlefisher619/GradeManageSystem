package ui.gui;

import business.GradeManager;
import business.StudentManager;
import model.Student;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.*;

public class StudentManagerGUI extends ManagerGUI {

    String[][] extractStudentData(Collection<Student> students){
        TreeSet<Student> orderedStudents = new TreeSet<>(students);
        String[][] ret =new String[students.size()][];
        int count =0;
        for(Student student: orderedStudents){
            ret[count++] = new String[]{student.getNo(), student.getName(), student.getGenderDescription(), String.valueOf(student.getAge())};
        }
        return ret;
    }

    @Override
    void initSearchOptions() {
        searchOption.addItem("No");
        searchOption.addItem("Name");
        searchOption.addItem("Age");
        searchOption.addItem("Gender");
        searchOption.setSelectedIndex(1);
    }

    @Override
    void registerListeners() {
        tableModel.addTableModelListener(
                (TableModelEvent event) ->{
                    int row = event.getFirstRow(), col = event.getColumn();
                    if(row == -1 || col == -1) return;
                    if (event.getType() == TableModelEvent.UPDATE) {
                        String filed = columnTitle[event.getColumn()].toLowerCase();
                        HashMap<String, Object> update = new HashMap<String, Object>(){
                            {
                                put(filed.toLowerCase(Locale.ROOT), tableModel.getValueAt(row, col));
                            }
                        };
                        if(update.containsKey("gender") ) {
                            update.put("gender", Student.parseGender((String) update.get("gender")));
                        }
                        if(update.containsKey("age")){
                            update.put("age",  Integer.parseInt((String) update.get("age")));
                        }

                        StudentManager.getInstance().update(
                                update
                                , (String) tableModel.getValueAt(row, 0));
                    }

                }

        );

        searchBtn.addActionListener((ActionEvent actionEvent) -> {
            String text = searchField.getText();
            if(searchOption.getSelectedItem() == null || text == null || text.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please specified something to search");
                return;
            }
            String field = searchOption.getSelectedItem().toString();
            Object value = text;
            if(text.equals("null")) value=null;
            else {
                if (field.equalsIgnoreCase("Age")) value = Integer.parseInt(text);
                if (field.equalsIgnoreCase("Gender")) value = Student.parseGender(text);
            }
            data = extractStudentData(StudentManager.getInstance().getDatabase().where(field, value));
            tableModel.setRowCount(0);
            tableModel.setDataVector(
                    data, columnTitle
            );
            table.validate();
        });

        resetBtn.addActionListener((ActionEvent actionEvent) -> {
            searchField.setText("");
            refreshBtn.doClick();
        });

        refreshBtn.addActionListener(
                (ActionEvent actionEvent) -> {
                    data = extractStudentData(StudentManager.getInstance().getAll());
                    tableModel.setRowCount(0);
                    tableModel.setDataVector(
                            data, columnTitle
                    );
                    table.validate();
                }
        );
        searchField.addKeyListener(new KeyAdapter(){
            public void keyPressed(KeyEvent e)
            {
                if(e.getKeyChar()==KeyEvent.VK_ENTER )
                {
                    searchBtn.doClick();
                }
            }
        });
        createBtn.addActionListener((ActionEvent actionEvent) -> {
            String no = JOptionPane.showInputDialog("Input NO");
            if(no == null || (no = no.trim()).isEmpty()) return;
            if(StudentManager.getInstance().queryByKey(no)!=null){
                JOptionPane.showMessageDialog(this, "This student NO already exists","Failed", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String name = JOptionPane.showInputDialog("Input Name");
            if(name == null || (name = name.trim()).isEmpty()) return;
            String age = JOptionPane.showInputDialog("Input Age");
            if(age == null || (age = age.trim()).isEmpty()) return;
            String gender = (String) JOptionPane.showInputDialog(this,
                    "Select Gender: Man(0), Female(1)","Gender",JOptionPane.QUESTION_MESSAGE,null,
                    new String[]{"Man", "Female"}, "Man");
            if(gender == null) return;
            boolean ret = StudentManager.getInstance().create(
                    new Student(no, name, Integer.parseInt(age), Student.parseGender(gender))
            );

            if(ret) JOptionPane.showMessageDialog(this, "Student Created OK");
            else JOptionPane.showMessageDialog(this, "Failed to create student");
            refreshBtn.doClick();
        });

        deleteBtn.addActionListener((ActionEvent actionEvent) -> {
            int row = table.getSelectedRow();
            if(row == -1){
                JOptionPane.showMessageDialog(this, "Please select a data item to delete");
                return;
            }
            String key = (String) tableModel.getValueAt(row, 0);
            if(key == null || (key = key.trim()).isEmpty()) return;
            boolean ret = StudentManager.getInstance().deleteByKey(key);
            if(ret) JOptionPane.showMessageDialog(this, "Student Deleted.");
            else JOptionPane.showMessageDialog(this, "Failed to delete student, may be it doesn't exist");

            tableModel.removeRow(row);
            table.validate();

        });

    }
    void initDataModel(){
        columnTitle = new String[]{"No" , "Name", "Gender", "Age"};
        tableModel = new DefaultTableModel(){
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                if(columnIndex == 0) return false;
                else return privileged;
            }
        };
        tableModel.setDataVector(null, columnTitle);
    }

    @Override
    void refresh() {
        data = extractStudentData(StudentManager.getInstance().getAll());
        tableModel.setDataVector(data, columnTitle);
        if(!privileged){
            createBtn.setEnabled(false);
            deleteBtn.setEnabled(false);
            table.setCellSelectionEnabled(false);
        }else{
            createBtn.setEnabled(true);
            deleteBtn.setEnabled(true);
        }
    }
}
