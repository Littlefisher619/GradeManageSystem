package ui.cli;

import business.CourseManager;
import model.Course;

import java.util.*;

public class CourseManagerCLI extends ManagerCLI{

    @CLIAction(index = 0, description = "Create a course", adminAccess = true)
    private static void actionCreate(){
        out.println("Now, please input as the instructions following:");



        out.print("Course No> ");
        String no = scanner.nextLine().trim();
        out.print("Course Name> ");
        String name = scanner.nextLine().trim();
        out.print("Course Grade> ");
        int grade = Integer.parseInt(scanner.nextLine().trim());
        out.print("Course Exam[0=Exam, 1=Rank]> ");
        int courseType = Integer.parseInt(scanner.nextLine().trim());

        Course newCourse = new Course(no, name, grade, courseType==0 ? Course.ExamType.EXAM: Course.ExamType.RANK);

        boolean res = CourseManager.getInstance().create(newCourse);
        out.println(res ? "Create OK." : "Failed.");

    }

    @CLIAction(index = 3, description = "Print all courses")
    private static void actionPrintAll(){
        print(CourseManager.getInstance().getAll());
    }

    @CLIAction(index = 2, description = "Update a course", adminAccess = true)
    private static void actionUpdate(){
        out.println("Now, please input as the instructions following:");

        out.print("Specify a Course No to Update> ");
        String key = scanner.nextLine().trim();
        String input;

        Course target = CourseManager.getInstance().queryByKey(key);
        if(target == null){
            out.println("Course is not found!");
            return;
        }

        out.println("We will ask you for update fields one by one in this course. (Press Enter DIRECTLY to SKIP):");
        HashMap<String, Object> fieldsToUpdate = new HashMap<>();

        out.printf("New Name[%s]> ", target.getName());
        input = scanner.nextLine().trim();
        if(!input.isEmpty()) fieldsToUpdate.put("name", input);

        out.printf("New Grade[%d]> ", target.getGrade());
        input = scanner.nextLine().trim();
        if(!input.isEmpty()) fieldsToUpdate.put("grade", Integer.parseInt(input));


        boolean res = CourseManager.getInstance().update(fieldsToUpdate, key);
        out.println(res ? "Update finished." : "Failed.");

    }

    @CLIAction(index = 4, description = "Query courses")
    private static void actionQuery(){
        out.println("Now, please input as the instructions following:");

        out.println("We will ask you for query field one by one in this course. (Press Enter DIRECTLY to SKIP):");

        String input;
        out.print("Query No> ");
        input = scanner.nextLine().trim();
        if(!input.isEmpty()) {
            print(
                    CourseManager.getInstance().queryByKey(input)
            );
            return;
        }


        out.print("Query Name> ");
        input = scanner.nextLine().trim();
        if(!input.isEmpty()) {
            print(
                    CourseManager.getInstance().queryByName(input)
            );
            return;
        }

        out.print("Query Grade> ");
        input = scanner.nextLine().trim();
        if(!input.isEmpty()) {
            if (input.contains(" ")) {
                String[] rangebuf = input.split("\\s+");
                print(
                        CourseManager.getInstance().queryByGrade(
                                Integer.parseInt(rangebuf[0]), Integer.parseInt(rangebuf[1])
                        )
                );
            } else print(CourseManager.getInstance().queryByGrade(Integer.parseInt(input)));
            return;
        }


    }

    @CLIAction(index = 1, description = "Delete a course", adminAccess = true)
    private static void actionDelete(){
        out.print("Specify a Course No to Delete> ");
        String key = scanner.nextLine().trim();

        Course target = CourseManager.getInstance().queryByKey(key);
        if(target == null){
            out.println("Course to delete is not found.");
            return;
        }

        out.printf("Target: %s.\n",target);

        boolean res = CourseManager.getInstance().deleteByKey(
                key
        );

        out.println(res ? "Deleted." : "Failed.");

    }

}
