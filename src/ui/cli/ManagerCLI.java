package ui.cli;

import business.StudentManager;
import exceptions.ConstraintException;
import exceptions.DBError;
import exceptions.IllegalInputException;
import exceptions.SyncException;
import model.Student;

import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class ManagerCLI {

    private final String menu;
    private HashMap<Integer, Method> methodMap;
    private TreeSet<Integer> adminAccess;
    protected static PrintStream out = System.out;
    protected static Scanner scanner = new Scanner(System.in);
    private static Student student = null;
    private static boolean privileged = false;


    protected ManagerCLI(){
        methodMap = new HashMap<>();
        adminAccess = new TreeSet<>();

        Method[] methods= this.getClass().getDeclaredMethods();

        StringBuilder menuBuilder = new StringBuilder();
        int maxLength = 0;
        for(Method method: methods){
            CLIAction action = method.getAnnotation(CLIAction.class);
            if(action == null) continue;
            int index = action.index();
            if(index < 0){
                out.println("Warning: invalid index of "+ method.getName());
                continue;
            }
            if(methodMap.containsKey(index)){
                out.printf("Warning: duplicated index %d of %s", index, method.getName());
                continue;
            }

            methodMap.put(action.index(), method);
            if(action.adminAccess()){
                adminAccess.add(index);
            }
        }
        TreeSet<Integer> set = new TreeSet<>(methodMap.keySet());

        for(int i: set){
            CLIAction action = methodMap.get(i).getAnnotation(CLIAction.class);
            String stringToAppend = String.format("\n%d: %s %s", action.index(), action.description(), action.adminAccess() ? "(admin)" : "");

            if(maxLength < stringToAppend.length() ) maxLength = stringToAppend.length();

            menuBuilder.append(stringToAppend);
        }

        menuBuilder.append("\n-1: Exit");

        if(maxLength<=10) maxLength=10;

        if((maxLength&1) == 1) ++maxLength;


        char[] head = new char[maxLength], tail = new char[maxLength];


        for(int i=0; i<maxLength; i++){
            tail[i] = head[i] = '=';
        }
        int titlePos = (maxLength-4)>>1;
        head[titlePos] = 'M';
        head[titlePos + 1] = 'E';
        head[titlePos + 2] = 'N';
        head[titlePos + 3] = 'U';

        this.menu = String.valueOf(head) +
                    menuBuilder.toString() + '\n' +
                    String.valueOf(tail)   + '\n';
    }


    protected static int getChoice(){
        out.print("Please specified your choice > ");
        int choice = Integer.MIN_VALUE;
        try {
            String buf = scanner.nextLine().trim();
            out.println();
            choice = Integer.parseInt(buf);
        }catch (NumberFormatException e){
            out.println("Your input is illegal!");
        }
        return choice;
    }



    protected boolean parseChoice(int choice) throws Throwable {
        Method method = methodMap.get(choice);
        if(choice == -1){
            return false;
            // continue flag: false
        }
        if(method == null){
            out.println("Invalid choice!");
            out.println(menu);
        }else{
            try {
                if(adminAccess.contains(choice) && ! privileged ){
                    out.println("You are not permitted to access this option!");
                    return true;
                }
                method.setAccessible(true);
                method.invoke(this);
            }catch (IllegalAccessException e){
                out.println("Cannot Access to " + method.getName() + ": " + e.getMessage());
            } catch (InvocationTargetException e) {
                throw e.getCause();
//                out.println("Reflection error occurred while call to " + method.getName());
//                e.printStackTrace();

            }
        }


        return true;
    }

    protected static boolean requireLogin(){
        out.println(
                        "\n============ROLE============\n" +
                        "1. Student\n" +
                        "2. Admin\n" +
                        "3. Exit\n" +
                        "============================\n"
        );

        out.print("Select Role> ");

        String input = scanner.nextLine().trim();
        String buf;
        switch (input) {
            case "1":
                privileged = false;
                out.print("Enter your Student NO> ");
                buf = scanner.nextLine().trim();
                if ((student = StudentManager.getInstance().queryByKey(buf)) == null) {
                    out.println("Student not found!");
                }else{
                    out.printf("Welcome %s!\n", student.getName());
                }
                return true;
            case "2":
                out.print("Admin password> ");
                buf = scanner.nextLine().trim();
                if (!buf.equals("fzu")) {
                    out.println("Password incorrect! Hint: fzu");
                } else {
                    privileged = true;
                }
                return true;
            case "3":
                return false;
            default:
                out.println("Invalid input!");
                return true;
        }

    }

    protected static void enterToContinue(){
        System.out.print("[!] Press Enter To Continue!");
        scanner.nextLine();
    }

    protected boolean routine(){
        if(!privileged && student==null)
            return requireLogin();


        out.println();
        out.println(menu);

        int choice = getChoice();

        try {
            return parseChoice(choice);
        }catch (NumberFormatException | InputMismatchException | IllegalInputException e){
            out.println("[ERROR] Input Incorrectly: "+ e.getMessage());
        }catch (UnsupportedOperationException e){
            out.println("[ERROR] Unsupported Operation: " + e.getMessage());
        }catch (ConstraintException e){
            out.println("[ERROR] Constraint taken effects: " + e.getMessage());
        }catch (DBError | SyncException e){
            out.println("[ERROR] Persistence Layer Exception: " + e.getMessage());
            e.printStackTrace(out);
        }catch (Throwable e){
            out.println("[ERROR] " + e.getClass().getSimpleName());
            e.printStackTrace(out);
        }

        return true;

    }

    protected static <T> T getSelection(Collection<T> data){
        T[] selections= (T[]) data.toArray(new Object[data.size()]);

        if(data.isEmpty()) {
            out.println("Nothing to select!");
            return null;
        }
        int count = 0;

        for(T selection: selections){
            out.printf("%d -> %s\n", count++, selection);
        }

        out.print("Your choice> ");

        int index = Integer.parseInt(scanner.nextLine().trim());
        if(index >= 0 && index < count){
            return selections[index];
        }else throw new IllegalInputException("Your index is out of bound");

    }

    protected static <T> T getSelection(Collection<T> data, String message){
        if (message!=null && !message.isEmpty()) {
            out.println(message);
        }
        return getSelection(data);
    }

    protected static <T> void print(Collection<T> data){
        for(T t: data){
            out.println(t);
        }
        out.printf("Results in total: %d\n", data.size());
    }

    protected static <T> void print(T t){
        out.printf("Result: %s\n", t);
    }


    public boolean mainLoop(){

        boolean continueFlag = true;
        while(continueFlag){
            continueFlag = routine();
        }

        return false;
    }

    protected void logout(){
        privileged = false;
        student = null;
    }

    public static Student getStudent() {
        return student;
    }

    public static void setStudent(Student student) {
        ManagerCLI.student = student;
    }

}
