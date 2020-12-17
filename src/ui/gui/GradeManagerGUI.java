package ui.gui;

import javax.swing.*;

import business.CourseManager;
import business.GradeManager;
import business.StudentManager;
import model.Course;
import model.Grade;
import model.Student;
import persistence.Pair;

import javax.swing.border.Border;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class GradeManagerGUI extends ManagerGUI {
    static final String informationFormat =
            "Course: %s\n" +
            "> Name=%s\n" +
            "> Grade=%d\n" +
            "> ExamType=%s\n\n——————————————————\n\n" +
            "Student: %s\n" +
            "> Name=%s\n" +
            "> Age=%d\n" +
            "> Gender=%s\n\n——————————————————\n\n" ;

    protected JTextArea infoArea;

    String[][] extractGradeData(Collection<Grade> grades){
        TreeSet<Grade> orderedGrades = new TreeSet<>(Comparator.reverseOrder());
        orderedGrades.addAll(grades);
        String[][] ret =new String[grades.size()][];
        int count =0;
        for(Grade grade: orderedGrades){
            ret[count++] = new String[]{grade.getKey(), grade.getCourse(), grade.getStudent(), grade.getMarkDescription()};
        }
        return ret;
    }

    @Override
    void initDataModel() {
        columnTitle = new String[] {"Key" , "Course", "Student", "Mark"};
        tableModel = new DefaultTableModel(){
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                if(columnIndex == 0 || columnIndex == 1 || columnIndex == 2) return false;
                return privileged;
            }
        };
        tableModel.setDataVector(null, columnTitle);
    }

    @Override
    void initSearchOptions() {
        searchOption.addItem("Course");
        searchOption.addItem("Student");
        searchOption.setSelectedIndex(0);
    }

    @Override
    void registerListeners() {
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int selectedRow = table.getSelectedRow();
                if(selectedRow == -1) {
                    infoArea.setText("");
                    return;
                }
                String courseNo = (String) tableModel.getValueAt(selectedRow, 1);
                String studentNo = (String) tableModel.getValueAt(selectedRow, 2);
                String gradeKey = (String) tableModel.getValueAt(selectedRow, 0);
                Course course = CourseManager.getInstance().queryByKey(courseNo);
                Student student = StudentManager.getInstance().queryByKey(studentNo);
                Grade grade = GradeManager.getInstance().queryByKey(gradeKey);
                String baseInfo = String.format(informationFormat,
                        course.getNo(), course.getName(), course.getGrade(), course.getExamType(),
                        student.getNo(), student.getName(), student.getAge(), student.getGenderDescription()
                );
                StringBuilder extraInfo = new StringBuilder();
                Map<String, Object> statistics = GradeManager.getInstance().getStatistics(course);
                extraInfo.append(String.format("Total: %d, Null: %d\n", (int) statistics.get("total") ,  (int) statistics.get("null")));
                Grade maxGrade = (Grade) statistics.get("max"), minGrade = (Grade) statistics.get("min");
                extraInfo.append(String.format("Max: %s, Min: %s\n",
                        maxGrade != null ? maxGrade.getMarkDescription() : null,
                        minGrade != null ? minGrade.getMarkDescription() : null
                        )
                );
                if(course.getExamType() == Course.ExamType.EXAM) {
                    int order = 1;
                    int sameOrder = 0;
                    int count = 0;
                    int myOrder = 0;

                    Grade last = null;
                    ArrayList<Pair<Student, Grade>> sorted = (ArrayList<Pair<Student, Grade>>) statistics.get("sorted");


                    for (Pair<Student, Grade> pair : sorted) {
                        if (last == null) {
                            last = pair.getValue();
                        }
                        if (pair.getValue().compareTo(last) != 0) {
                            order += sameOrder;
                            sameOrder = 1;
                        } else {
                            sameOrder += 1;
                        }

                        if (++count <= 10)
                            extraInfo.append(
                                    String.format("> Top %d(%s): %s\n", order, pair.getKey().getName(), pair.getValue().getMarkDescription())
                            );

                        if (student == pair.getKey()) {
                            myOrder = order;
                        }
                        last = pair.getValue();
                    }

                    extraInfo.append("Mark order: " + myOrder);
                }else{
                    HashMap<Grade.Rank, Integer> rankCount = (HashMap<Grade.Rank, Integer>) statistics.get("rank");
                    Double last = 0.0, myOrder = 0.0;
                    Grade.Rank[] sortedRankAccess = Grade.Rank.values();
                    Arrays.sort(sortedRankAccess, Comparator.reverseOrder());
                    for(Grade.Rank rank: sortedRankAccess){
                        if(rankCount.get(rank) == 0) continue;
                        last += 100.0 * rankCount.get(rank) /  (int) statistics.get("valid");

                        if(rank == grade.getRank())
                            myOrder = last;
                        extraInfo.append(String.format("> %s %.1f%%\n", rank, last));
                    }

                    extraInfo.append(String.format("Mark order: %.1f%%", myOrder));
                }

                infoArea.setText(
                        baseInfo + extraInfo
                );

            }
        });

        searchField.addKeyListener(new KeyAdapter(){
            public void keyPressed(KeyEvent e)
            {
                if(e.getKeyChar()==KeyEvent.VK_ENTER )
                {
                    searchBtn.doClick();
                }
            }
        });

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
                        if(update.containsKey("mark")){
                            String newMark = (String) update.remove("mark");
                            Course course = CourseManager.getInstance().queryByKey((String) tableModel.getValueAt(row, 1));
                            switch (course.getExamType()){
                                case EXAM:
                                    update.put("score",  newMark);
                                    break;
                                case RANK:
                                    update.put("rank", newMark);
                                    break;
                            }
                        }

                        GradeManager.getInstance().update(update, (String) tableModel.getValueAt(row, 0));

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
            Collection<Grade> results = GradeManager.getInstance().getDatabase().where(field, value);
            if(!privileged){
                results = GradeManager.getInstance().getDatabase().filter("student", student.getKey(),  results, Integer.MAX_VALUE);
            }
            data = extractGradeData(
                    results
            );
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
                    tableModel.setRowCount(0);
                    refresh();
                    table.validate();
                }
        );

        createBtn.addActionListener((ActionEvent actionEvent) -> {
            Collection<Course> coursesToSelect = privileged ?
                    CourseManager.getInstance().getAll() : GradeManager.getInstance().getCoursesCanJoin(student);

            Course course = (Course) JOptionPane.showInputDialog(this,
                    "Select Course","Course",JOptionPane.QUESTION_MESSAGE,null,
                    coursesToSelect.toArray(new Course[0]), null);;

            if(course == null) return;

            Student student = ManagerGUI.student;

            if(privileged) {
                LinkedList<Student> students = new LinkedList<>();
                for (Grade grade : GradeManager.getInstance().queryByCourse(course)) {
                    students.add(
                            GradeManager.getInstance().getNestedStudent(grade)
                    );
                }
                Collection<Student> studentsToSelect = StudentManager.getInstance().getAll();
                studentsToSelect.removeAll(students);
                student = (Student) JOptionPane.showInputDialog(this,
                        "Select Student", "Student", JOptionPane.QUESTION_MESSAGE, null,
                        studentsToSelect.toArray(), null);
            }

            if (student == null) return;


            boolean ret = GradeManager.getInstance().create(
                    new Grade(student.getKey(), course.getKey())
            );

            if(ret) JOptionPane.showMessageDialog(this, "Selection Record Created");
            else JOptionPane.showMessageDialog(this, "Failed to create, maybe it exists");
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

            if(!privileged && tableModel.getValueAt(row, 3) != null){
                JOptionPane.showMessageDialog(this, "You can't revoke what already has mark");
                return;
            }


            boolean ret = GradeManager.getInstance().deleteByKey(key);
            if(ret) JOptionPane.showMessageDialog(this, "Selection Record Removed.");
            else JOptionPane.showMessageDialog(this, "Failed to delete grade, may be it doesn't exist");

            tableModel.removeRow(row);
            table.validate();

        });

    }
    protected void addAllComponents() {
        super.addAllComponents();
        add(infoArea);
    }

    protected void setComponentPosition(){
        super.setComponentPosition();
        gridBagConstraints.gridx=9;
        gridBagConstraints.gridy=0;
        gridBagConstraints.gridwidth=4;
        gridBagConstraints.gridheight=8;
        gridBagLayout.setConstraints(infoArea, gridBagConstraints);
    }

    protected void initInterface(){
        super.initInterface();
        infoArea = new JTextArea("");
        infoArea.setEditable(false);
        infoArea.setBackground(this.getBackground());
        infoArea.setColumns(20);
        Border border = BorderFactory.createLineBorder(Color.BLACK);
        infoArea.setBorder(BorderFactory.createCompoundBorder(border,
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
    }

    protected String getDefaultSaveName(){
        return "course_selection_data.txt";
    }

    @Override
    void refresh() {
        data = null;
        if(privileged){
            data = extractGradeData(GradeManager.getInstance().getAll());
            createBtn.setText("Create");
            deleteBtn.setText("Delete");
            searchOption.addItem("Student");
        }
        else if(student != null){
            data = extractGradeData(GradeManager.getInstance().queryByStudent(student));
            createBtn.setText("Course-Selection");
            deleteBtn.setText("Revoke");
//            searchOption.remove(1);
            searchOption.removeItem("Student");
        }

        tableModel.setDataVector(data, columnTitle);

    }
}
