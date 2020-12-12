package ui.cli;

import business.StudentManager;
import model.Student;


import java.util.*;

public class StudentManagerCLI extends ManagerCLI{

    @CLIAction(index = 0, description = "Create a student", adminAccess = true)
    private void actionCreate(){
        out.println("Now, please input as the instructions following:");


        out.print("Student No> ");
        String no = scanner.nextLine().trim();
        out.print("Student Name> ");
        String name = scanner.nextLine().trim();
        out.print("Student Age> ");
        int age = Integer.parseInt(scanner.nextLine().trim());
        out.print("Student Gender(0:Man, 1:Female)> ");
        int gender = Integer.parseInt(scanner.nextLine().trim());

        Student newStudent = new Student(no, name, age, gender);
        boolean res = StudentManager.getInstance().create(
                newStudent
        );
        out.println(res ? "Create OK." : "Failed.");

    }
    

    @CLIAction(index = 3, description = "Get all students")
    private void actionPrintAll(){
        print(StudentManager.getInstance().getAll());
    }

    @CLIAction(index = 2, description = "Update a student", adminAccess = true)
    private void actionUpdate(){
        out.println("Now, please input as the instructions following:");


        out.print("Specify a Student No to Update> ");
        String no = scanner.nextLine().trim();
        String input;

        Student target = StudentManager.getInstance().queryByKey(no);
        if(target == null){
            out.println("Student is not found!");
            return;
        }

        out.println("We will ask you for update fields one by one in this student. (Press Enter DIRECTLY to SKIP):");
        HashMap<String, Object> fieldsToUpdate = new HashMap<>();

        out.printf("New Name[%s]> ", target.getName());
        input = scanner.nextLine().trim();
        if(!input.isEmpty()) fieldsToUpdate.put("name", input);

        out.printf("New Age[%d]> ", target.getAge());
        input = scanner.nextLine().trim();
        if(!input.isEmpty()) fieldsToUpdate.put("age", Integer.parseInt(input));

        out.printf("New Gender[%s](0:Man, 1:Female)> ", target.getGenderDescription());
        input = scanner.nextLine().trim();
        if(!input.isEmpty()) fieldsToUpdate.put("gender", Integer.parseInt(input));

        boolean res = StudentManager.getInstance().update(fieldsToUpdate, no);
        out.println(res ? "Update finished." : "Failed.");


    }

    @CLIAction(index = 4, description = "Query a student")
    private void actionQuery(){
        out.println("Now, please input as the instructions following:");

        out.println("We will ask you for query field one by one in this student. (Press Enter DIRECTLY to SKIP):");

        String input;
        out.print("Query No> ");
        input = scanner.nextLine().trim();
        if(!input.isEmpty()) {
            print(
                    StudentManager.getInstance().queryByKey(input)
            );
            return;
        }


        out.print("Query Name> ");
        input = scanner.nextLine().trim();
        if(!input.isEmpty()) {
            print(
                    StudentManager.getInstance().queryByName(input)
            );
            return;
        }

        out.print("Query Age> ");
        input = scanner.nextLine().trim();
        if(!input.isEmpty()) {
            if (input.contains(" ")) {
                String[] rangebuf = input.split("\\s+", 2);
                print(
                        StudentManager.getInstance().queryByAge(
                                Integer.parseInt(rangebuf[0]), Integer.parseInt(rangebuf[1])
                        )
                );
            } else print(StudentManager.getInstance().queryByAge(Integer.parseInt(input)));
            return;
        }

        out.print("Query Gender(0:Man, 1:Female)> ");
        input = scanner.nextLine().trim();
        if(!input.isEmpty()){
            print(StudentManager.getInstance().queryByGender(Integer.parseInt(input)));
            return;
        }

    }

    @CLIAction(index = 1, description = "Delete a student", adminAccess = true)
    private void actionDelete(){

        out.print("Specify a Student No to Update> ");
        String no = scanner.nextLine().trim();
        String input;

        Student target = StudentManager.getInstance().queryByKey(no);
        if(target == null){
            out.println("Student to delete is not found.");
            return;
        }

        out.printf("Target: %s.\n",target);

        boolean res = StudentManager.getInstance().deleteByKey(
                no
        );

        out.println(res ? "Deleted." : "Failed.");


    }


}
