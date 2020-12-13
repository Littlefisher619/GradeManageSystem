package ui.gui;

import business.CourseManager;
import business.StudentManager;
import model.Course;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.HashMap;
import java.util.TreeSet;

public class CourseManagerGUI extends ManagerGUI {
    String[][] extractCourseData(Collection<Course> courses){
        TreeSet<Course> orderedCourses = new TreeSet<>(courses);
        String[][] ret =new String[courses.size()][];
        int count =0;
        for(Course course: orderedCourses){
            ret[count++] = new String[]{course.getNo(), course.getName(), course.getExamType().name(), String.valueOf(course.getGrade())};
        }
        return ret;
    }

    @Override
    void initDataModel() {
        columnTitle = new String[]{"No" , "Name", "ExamType", "Grade"};
        tableModel = new DefaultTableModel(){
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                if(columnIndex == 0 || columnIndex == 2) return false;
                else return privileged;
            }
        };
        tableModel.setDataVector(null, columnTitle);
    }

    @Override
    void initSearchOptions() {
        searchOption.addItem("No");
        searchOption.addItem("Name");
        searchOption.addItem("Grade");
        searchOption.addItem("ExamType");
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
                                put(filed.toLowerCase(), tableModel.getValueAt(row, col));
                            }
                        };
                        if(update.containsKey("grade")){
                            update.put("grade",  Integer.parseInt((String) update.get("grade")));
                        }

                        CourseManager.getInstance().update(
                                update
                                , (String) tableModel.getValueAt(row, 0));
                    }

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
                if (field.equalsIgnoreCase("grade")) value = Integer.parseInt(text);
                if (field.equalsIgnoreCase("examType")) value = Course.ExamType.valueOf(text.toUpperCase());
            }
            data = extractCourseData(CourseManager.getInstance().getDatabase().where(field, value));
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
                    data = extractCourseData(CourseManager.getInstance().getAll());
                    tableModel.setRowCount(0);
                    tableModel.setDataVector(
                            data, columnTitle
                    );
                    table.validate();
                }
        );

        createBtn.addActionListener((ActionEvent actionEvent) -> {
            String no = JOptionPane.showInputDialog("Input NO");
            if(no == null || (no = no.trim()).isEmpty()) return;
            if(CourseManager.getInstance().queryByKey(no)!=null){
                JOptionPane.showMessageDialog(this, "This student NO already exists","Failed", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String name = JOptionPane.showInputDialog("Input Name");
            if(name == null || (name = name.trim()).isEmpty()) return;
            String grade = JOptionPane.showInputDialog("Input Grade");
            if(grade == null || (grade = grade.trim()).isEmpty()) return;
            String examType = (String) JOptionPane.showInputDialog(this,
                    "Select ExamType: Exam, Rank","ExamType",JOptionPane.QUESTION_MESSAGE,null,
                    new String[]{"EXAM", "RANK"}, "EXAM");
            if(examType == null) return;
            boolean ret = CourseManager.getInstance().create(
                    new Course(no, name, Integer.parseInt(grade), Course.ExamType.valueOf(examType))
            );

            if(ret) JOptionPane.showMessageDialog(this, "Course Created OK");
            else JOptionPane.showMessageDialog(this, "Failed to create course");
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
            boolean ret = CourseManager.getInstance().deleteByKey(key);
            if(ret) JOptionPane.showMessageDialog(this, "Course Deleted.");
            else JOptionPane.showMessageDialog(this, "Failed to delete course, may be it doesn't exist");

            tableModel.removeRow(row);
            table.validate();

        });

    }

    @Override
    void refresh() {
        data = extractCourseData(CourseManager.getInstance().getAll());
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
