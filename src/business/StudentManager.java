package business;

import exceptions.ConstraintException;
import exceptions.IllegalInputException;
import model.Course;
import persistence.Database;
import model.Student;
import persistence.datapool.HashMapDataPool;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.regex.Pattern;

import static java.lang.System.out;

public class StudentManager extends BusinessManager<String, Student> {
    private static StudentManager instance = new StudentManager();
    public static StudentManager getInstance() {
        return instance;
    }
    private StudentManager(){
        database = new Database<>(Student.class, new HashMapDataPool<>());
    }

    public Collection<Student> queryByName(String name){
        return database.where("name", name);
    }

    public Collection<Student> queryByAge(Integer age){
        return database.where("age", age);
    }

    public Collection<Student> queryByAge(Integer ageFrom, Integer ageTo){
        return database.where("age", ageFrom, ageTo);
    }

    public Collection<Student> queryByGender(Integer gender){
        return database.where("gender", gender);
    }

    public boolean deleteByKey(String no){
        Student target = database.retrieveOne("key", no);
        if(null!=target){
            if(!GradeManager.getInstance().queryByStudent(target).isEmpty())
                throw new ConstraintException("Delete operation is RESTRICTED before remove all of Grade records related to this student");
            return super.deleteByKey(no);
        }else return false;
    }

    public boolean update(HashMap <String, Object> map, String key){
        Student target = this.queryByKey(key);

        int age, gender;

        if(map.containsKey("age")){
            age = (int) map.get("age");
            if(age < 8 || age > 100) throw new IllegalInputException("age should in [8, 100]");
        }
        if(map.containsKey("gender")){
            gender = (int) map.get("gender");
            if(!(gender == 0 || gender == 1)) throw new IllegalInputException("Gender should be (Man)0 or (Female)1");
        }
        return this.update(map, target);
    }

    public boolean create(Student student){
        if(!Pattern.matches("^\\d+$", student.getNo()))
            throw new IllegalInputException("Student's No should only be digits");

        int age = student.getAge();
        int gender = student.getGender();
        if(age >= 8 && age <= 100 && (gender == 0 || gender == 1)){
            return super.create(student);
        }

        else throw new IllegalInputException("Student's age should in range of [8, 100] and gender must be 0 or 1");
    }

}
