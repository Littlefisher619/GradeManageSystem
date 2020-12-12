import business.CourseManager;
import business.GradeManager;
import business.StudentManager;
import model.Course;
import model.Grade;
import model.Student;
import ui.cli.CLIMain;
import ui.cli.ManagerCLI;
import ui.gui.GUIMain;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.TreeSet;

public class Main {
    public static void createTestData(){
        StudentManager.getInstance().create(new Student("0000", "tomcat", 12, 0));
        StudentManager.getInstance().create(new Student("0001", "mouse", 16, 1));
        StudentManager.getInstance().create(new Student("0002", "jerry", 18, 1));
        StudentManager.getInstance().create(new Student("0003", "laravel", 20, 0));
        CourseManager.getInstance().create(new Course("0000", "高等数学", 2, Course.ExamType.EXAM));
        CourseManager.getInstance().create(new Course("0001", "形式与政策", 2, Course.ExamType.RANK));
        GradeManager.getInstance().create(new Grade("0000","0000", 100.0));
        GradeManager.getInstance().create(new Grade("0001","0000", 90.0));
        GradeManager.getInstance().create(new Grade("0002","0000", 100.0));
        GradeManager.getInstance().create(new Grade("0003","0000", 60.0));
        GradeManager.getInstance().create(new Grade("0000","0001", Grade.Rank.B));
        GradeManager.getInstance().create(new Grade("0001","0001", Grade.Rank.E));
        GradeManager.getInstance().create(new Grade("0002","0001", Grade.Rank.E));
        GradeManager.getInstance().create(new Grade("0003","0001", Grade.Rank.A));


    }
    public static void main(String[] args) {
        TreeSet<String> arg = new TreeSet<>(Arrays.asList(args));
        if(arg.contains("--testdata")){
            createTestData();
        }
        if(arg.contains("--cli")){
            ManagerCLI cli = new CLIMain();
            cli.mainLoop();
        }else{
            SwingUtilities.invokeLater(GUIMain::new);
        }
    }
}
