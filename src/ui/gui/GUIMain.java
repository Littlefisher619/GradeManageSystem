package ui.gui;

import business.StudentManager;
import exceptions.ConstraintException;
import exceptions.DBError;
import exceptions.SyncException;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.InputMismatchException;

public class GUIMain extends JFrame{
        private final JTabbedPane tabs = new JTabbedPane();
        private final String[] tabNames = { "StudentManager", "CourseManager", "GradeManager" };


        public GUIMain() {
            Thread.setDefaultUncaughtExceptionHandler(
                    (Thread thread, Throwable throwable) -> {
                        try {
                            throw throwable;
                        }catch (InputMismatchException | IllegalArgumentException e){
                            JOptionPane.showMessageDialog(this, e.getMessage(), "Please check your input", JOptionPane.WARNING_MESSAGE);
                        }catch (UnsupportedOperationException e){
                            JOptionPane.showMessageDialog(this, e.getMessage(), "Unsupported Operation Exception", JOptionPane.WARNING_MESSAGE);
                        }catch (ConstraintException e){
                            JOptionPane.showMessageDialog(this, e.getMessage(), "Constraint taken effects", JOptionPane.WARNING_MESSAGE);
                        }catch (DBError | SyncException e){
                            e.printStackTrace(System.out);
                            StringWriter sw = new StringWriter();
                            PrintWriter pw = new PrintWriter(sw);
                            e.printStackTrace(pw);
                            JOptionPane.showMessageDialog(this, sw.toString(), "Persistence Layer Exception:"+ e.getMessage(), JOptionPane.ERROR_MESSAGE);

                        }catch (Throwable e){
                            e.printStackTrace(System.out);
                            StringWriter sw = new StringWriter();
                            PrintWriter pw = new PrintWriter(sw);
                            e.printStackTrace(pw);

                            JOptionPane.showMessageDialog(this,   sw.toString(),e.getClass().getSimpleName() + " : " + e.getMessage(),  JOptionPane.ERROR_MESSAGE);

                        }
                    }
            );

            this.setTitle("Java Student & Course & Grade Manage System");
            this.setSize(800, 600);
            Dimension displaySize = Toolkit.getDefaultToolkit().getScreenSize();
            Dimension frameSize = this.getSize();
            if (frameSize.width > displaySize.width)
                frameSize.width = displaySize.width;

            this.setLocation((displaySize.width - frameSize.width) / 2,
                    (displaySize.height - frameSize.height) / 2);


            int i = 0;

            JPanel welcome = new JPanel();
            welcome.setLayout(new GridBagLayout());
            welcome.add(new JLabel("<html><HTML><body style=color:blue align=center>Welcome to our Graphic User Interface! <br> Tips: Login before use GUI.</body></html>\n"){
            });
            tabs.addTab("Welcome", null, welcome, "Welcome Page");

            JPanel student = new StudentManagerGUI();
            student.setEnabled(false);

            tabs.addTab(tabNames[i++], null, student, "Manage students");

            JPanel course = new CourseManagerGUI();
            tabs.addTab(tabNames[i++], null, course, "Manage courses");
            course.setVisible(false);

            JPanel grade = new GradeManagerGUI();
            tabs.addTab(tabNames[i++], null, grade, "Manage grades");
            grade.setVisible(false);


            tabs.addChangeListener(
                    (ChangeEvent event) -> {
                        if(tabs.getSelectedIndex() == 0) return;
                        ManagerGUI gui = (ManagerGUI) tabs.getSelectedComponent();
                        gui.refresh();
                    }
            );

            setLayout(new GridLayout(1, 1));
            add(tabs);

            tabs.setEnabledAt(1, false);
            tabs.setEnabledAt(2, false);
            tabs.setEnabledAt(3, false);


            this.setVisible(true);
            this.setResizable(false);
            this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            JMenuBar menuBar = new JMenuBar();
            JMenu loginMenu = new JMenu("Login");


            JMenuItem loginAsStudent = new JMenuItem("As Student");
            JMenuItem loginAsAdmin = new JMenuItem("As Admin");
            JMenuItem logout = new JMenuItem("Logout");

            logout.setEnabled(false);

            loginAsStudent.addActionListener(
                    (ActionEvent event) -> {
                        String studentNo = JOptionPane.showInputDialog(this, "Input your student NO");
                        if(studentNo == null || (studentNo = studentNo.trim()).isEmpty()) return;
                        if ((ManagerGUI.student = StudentManager.getInstance().queryByKey(studentNo)) == null) {
                            JOptionPane.showMessageDialog(this, "Student not found!", "Error", JOptionPane.WARNING_MESSAGE);
                        }else{
                            JOptionPane.showMessageDialog(this, "Welcome: " + ManagerGUI.student.getName(), "Hello", JOptionPane.INFORMATION_MESSAGE);
                            loginAsStudent.setEnabled(false);
                            loginAsAdmin.setEnabled(false);
                            logout.setEnabled(true);
                            tabs.setEnabledAt(1, true);
                            tabs.setEnabledAt(2, true);
                            tabs.setEnabledAt(3, true);

                        }
                    }
            );


            loginAsAdmin.addActionListener(
                    (ActionEvent event) -> {
                        String password = JOptionPane.showInputDialog(this, "Input password", "fzu");
                        if(password == null || (password = password.trim()).isEmpty()) return;
                        if (!password.equals("fzu")) {
                            JOptionPane.showMessageDialog(this, "Password Incorrect", "Error", JOptionPane.WARNING_MESSAGE);
                        }else{
                            JOptionPane.showMessageDialog(this, "Welcome: Admin", "Hello", JOptionPane.INFORMATION_MESSAGE);
                            ManagerGUI.privileged = true;
                            loginAsStudent.setEnabled(false);
                            loginAsAdmin.setEnabled(false);
                            logout.setEnabled(true);
                            tabs.setEnabledAt(1, true);
                            tabs.setEnabledAt(2, true);
                            tabs.setEnabledAt(3, true);
                        }
                    }
            );


            logout.addActionListener(
                    (ActionEvent event) -> {
                        ManagerGUI.student = null;
                        ManagerGUI.privileged = false;
                        logout.setEnabled(false);
                        loginAsStudent.setEnabled(true);
                        loginAsAdmin.setEnabled(true);
                        tabs.setEnabledAt(1, false);
                        tabs.setEnabledAt(2, false);
                        tabs.setEnabledAt(3, false);
                        tabs.setSelectedIndex(0);
                    }
            );


            loginMenu.add(loginAsStudent);
            loginMenu.add(loginAsAdmin);
            loginMenu.add(logout);
            menuBar.add(loginMenu);

            this.setJMenuBar(menuBar);

        }


}

