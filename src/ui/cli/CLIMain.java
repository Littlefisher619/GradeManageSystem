package ui.cli;

public class CLIMain extends ManagerCLI{
    private static final ManagerCLI studentCLI = new StudentManagerCLI(), courseCLI = new CourseManagerCLI(), gradeCLI = new GradeManagerCLI();

    @CLIAction(index = 0, description = "Student CommandLine Interface")
    private void studentCLI(){
        studentCLI.mainLoop();
    }

    @CLIAction(index = 1, description = "Course CommandLine Interface")
    private void courseCLI(){
        courseCLI.mainLoop();
    }

    @CLIAction(index = 2, description = "Grade CommandLine Interface")
    private void gradeCLI(){
        gradeCLI.mainLoop();
    }

    @CLIAction(index = 3, description = "My Profile")
    private void myProfile(){
        out.println(getStudent());
    }

    @CLIAction(index = 4, description = "Logout")
    private void switchRole(){
        logout();
    }


}
