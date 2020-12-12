package ui.cli;

import business.CourseManager;
import business.GradeManager;
import business.StudentManager;
import model.Course;
import model.Grade;
import model.Student;
import persistence.Pair;

import java.util.*;

public class GradeManagerCLI extends ManagerCLI{
    @CLIAction(index = 0, description = "Create a grade", adminAccess = true)
    private static void actionCreate(){
        out.println("Now, please input as the instructions following:");
        
        out.print("Student No> ");
        String student = scanner.nextLine().trim();
        out.print("Course No> ");
        String course = scanner.nextLine().trim();
        
        Grade grade = new Grade(student, course);

        boolean res = GradeManager.getInstance().create(grade);
        
        out.println(res ? "Create OK." : "Failed.");

    }
    
    
    @CLIAction(index = 3, description = "Print all grades", adminAccess = true)
    private static void actionPrintAll(){
        print(GradeManager.getInstance().getAll());
    }

    @CLIAction(index = 2, description = "Update mark", adminAccess = true)
    private void actionUpdate(){
        out.println("Now, please input as the instructions following:");

        out.print("Specify a Course No> ");
        String key = scanner.nextLine().trim();
        String input;

        Course course = CourseManager.getInstance().queryByKey(key);
        if(course == null){
            out.println("Course is not found!");
            return;
        }

        Grade target = getSelection(
                GradeManager.getInstance().queryByCourse(course),
                "Specify a grade NO to update score:"
        );

        out.println("We will ask you for update fields one by one in this grade. (Press Enter DIRECTLY to SKIP):");
        HashMap<String, Object> fieldsToUpdate = new HashMap<>();

        switch (course.getExamType()){
            case EXAM:
                out.printf("New Score[%s]> ", target.getScore());
                input = scanner.nextLine().trim();
                if(!input.isEmpty()) fieldsToUpdate.put("score", input);
                break;
            case RANK:
                out.printf("New Rank[%s]> ", target.getRank());
                input = scanner.nextLine().trim();
                if(!input.isEmpty()) fieldsToUpdate.put("rank", input);
                break;
        }


        boolean res = GradeManager.getInstance().update(fieldsToUpdate, target.getKey());
        out.println(res ? "Update finished." : "Failed.");

    }

    @CLIAction(index = 4, description = "Query grades", adminAccess = true)
    private static void actionQuery(){
        out.println("Now, please input as the instructions following:");

        out.println("We will ask you for query field one by one in this course. (Press Enter DIRECTLY to SKIP):");

        String input;
        out.print("Query No> ");
        input = scanner.nextLine().trim();
        if(!input.isEmpty()) {
            print(
                    GradeManager.getInstance().queryByKey(input)
            );
            return;
        }

        out.print("Query Course> ");
        input = scanner.nextLine().trim();
        if(!input.isEmpty()) {
            print(
                    GradeManager.getInstance().queryByCourse(
                            CourseManager.getInstance().queryByKey(input)
                    )
            );
            return;
        }


        out.print("Query Student> ");
        input = scanner.nextLine().trim();
        if(!input.isEmpty()) {
            print(
                    GradeManager.getInstance().queryByStudent(
                            StudentManager.getInstance().queryByKey(input)
                    )
            );
            return;
        }


    }

    @CLIAction(index = 1, description = "Delete grade", adminAccess = true)
    private static void actionDelete(){
        out.print("Specify a Grade No to Delete> ");
        String no = scanner.nextLine().trim();
        String input;

        Grade target = GradeManager.getInstance().queryByKey(no);
        if(target == null){
            out.println("Grade to delete is not found.");
            return;
        }

        out.printf("Target: %s.\n",target);

        boolean res = GradeManager.getInstance().deleteByKey(
                no
        );

        out.println(res ? "Deleted." : "Failed.");

    }

    @CLIAction(index = 5, description = "Join a course")
    private void actionStudentJoin(){
        Student student = getStudent();
        if(student == null){
            out.println("You should login as a student to perform this operation");
            return;
        }

        Course target = getSelection(
                GradeManager.getInstance().getCoursesCanJoin(student),
                "Specify a grade to join:"
        );
        boolean res = target != null && GradeManager.getInstance().create(new Grade(student.getKey(), target.getKey()));
        out.println(res ? "OK." : "Failed.");
    }

    @CLIAction(index = 6, description = "Revoke a course")
    private void actionStudentRevoke(){
        Student student = getStudent();
        if(student == null){
            out.println("You should login as a student to perform this operation");
            return;
        }


        Course target = getSelection(
                GradeManager.getInstance().getCoursesCanRevoke(student),
                "Specify a grade NO to revoke:"
        );
        boolean res = target != null && GradeManager.getInstance().deleteByKey(student, target);
        out.println(res ? "OK." : "Failed.");
    }

    @CLIAction(index = 7, description = "Query scores")
    private void actionStudentQueryScore(){
        Student student = getStudent();
        if(student == null){
            out.println("You should login as a student to perform this operation");
            return;
        }


        out.println("Now, please input as the instructions following:");

        out.print("Course No[*, null, not null, specified]> ");
        String input = scanner.nextLine().trim();
        Collection<Pair<Course, Grade>> data = GradeManager.getInstance().getCurrentGrades(student);
        switch (input){
            case "*":
                for(Pair<Course, Grade> pair: data)
                {
                    out.printf("%s %s %s\n", pair.getKey().getKey(), pair.getKey().getName(), pair.getValue().getMarkDescription());
                }
                break;
            case "null":
                for(Pair<Course, Grade> pair: data)
                {
                    if(pair.getValue().getMarkDescription() == null)
                        out.printf("%s %s %s\n", pair.getKey().getKey(), pair.getKey().getName(), pair.getValue().getMarkDescription());
                }
                break;
            case "not null":
                for(Pair<Course, Grade> pair: data)
                {
                    if(pair.getValue().getMarkDescription() != null)
                        out.printf("%s %s %s\n", pair.getKey().getKey(), pair.getKey().getName(), pair.getValue().getMarkDescription());
                }
                break;
            default:
                Course course = CourseManager.getInstance().queryByKey(input);
                if(course == null){
                    out.println("Course No not found!");
                }else{
                    Grade grade = GradeManager.getInstance().queryByKey(student, course);
                    if(grade == null){
                        out.println("You have not joint this course!");
                    }else if(grade.getMarkDescription() == null) out.println("This course doesn't have a mark yet!");
                    else {
                        out.printf("Result: %s %s\n", course.getName(), grade.getMarkDescription());
                        Map<String, Object> statistics = GradeManager.getInstance().getStatistics(course);
                        out.printf("Total: %d, Null: %d\n", (int) statistics.get("total") ,  (int) statistics.get("null"));

                        Grade maxGrade = ((Grade)statistics.get("max")), minGrade = ((Grade)statistics.get("min"));
                        out.printf("Max: %s, Min: %s\n",
                                maxGrade == null ? null : maxGrade.getMarkDescription(),
                                minGrade == null ? null : minGrade.getMarkDescription());

                        if(course.getExamType() == Course.ExamType.EXAM){
                            int order = 1;
                            int sameOrder = 0;
                            int count = 0;
                            int myOrder = 0;

                            Grade last = null;
                            ArrayList<Pair<Student, Grade>> sorted = (ArrayList<Pair<Student, Grade>>) statistics.get("sorted");
                            for(Pair<Student, Grade> pair: sorted){
                                if(last == null){
                                    last = pair.getValue();
                                }
                                if(pair.getValue().compareTo(last) != 0){
                                    order += sameOrder;
                                    sameOrder = 1;
                                }else{
                                    sameOrder += 1;
                                }

                                if(++count <= 10)
                                    out.printf("Rank %d(%s): %s\n", order, pair.getKey().getName(), pair.getValue().getMarkDescription());

                                if(student == pair.getKey()){
                                    myOrder = order;
                                }
                                last = pair.getValue();
                            }

                            out.printf("Your order: %d\n", myOrder);
                        }else{
                            HashMap<Grade.Rank, Integer> rankCount = (HashMap<Grade.Rank, Integer>) statistics.get("rank");
                            Double last = 0.0;
                            Grade.Rank[] sortedRankAccess = Grade.Rank.values();
                            Arrays.sort(sortedRankAccess, Comparator.reverseOrder());
                            for(Grade.Rank rank: sortedRankAccess){
                                if(rankCount.get(rank) == 0) continue;
                                last += 100.0 * rankCount.get(rank) /  (int) statistics.get("valid");

                                if(rank == grade.getRank())
                                    out.print("[YOU]");
                                out.printf("Rank %.1f%%(%s)\n", last, rank);
                            }
                        }
                    }
                }

        }


    }


}
