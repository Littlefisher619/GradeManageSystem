package business;

import exceptions.ConstraintException;
import exceptions.IllegalInputException;
import model.Student;
import persistence.Database;
import model.Course;
import persistence.datapool.HashMapDataPool;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.regex.Pattern;


public class CourseManager extends BusinessManager<String, Course>{
    private static CourseManager instance = new CourseManager();
    public static CourseManager getInstance() {
        return instance;
    }
    private CourseManager(){
        database = new Database<>(Course.class, new HashMapDataPool<>());
    }


    public Collection<Course> queryByName(String name){
        return database.where("name", name);
    }

    public Collection<Course> queryByGrade(Integer grade){
        return database.where("grade", grade);
    }

    public Collection<Course> queryByGrade(Integer from, Integer to){
        return database.where("grade", from, to);
    }

    public boolean deleteByKey(String no){
        Course target = database.retrieveOne("key", no);
        if(null!=target){
            if(GradeManager.getInstance().queryByCourse(target).isEmpty())
                throw new ConstraintException("Delete operation is RESTRICTED before remove all of Grade records related to this student");
            return super.deleteByKey(no);
        }else return false;
    }

    public boolean update(HashMap <String, Object> map, String key){
        Course target = this.queryByKey(key);
        String name="";
        int grade=0;

        if(map.containsKey("name")){
            name = (String)map.get("name");
        }
        if(map.containsKey("grade")){
            grade = (int) map.get("grade");
            if(!(grade >= 0 && grade <= 10))
                throw new IllegalInputException("Course's grade should in range of [0, 10]");
        }

        boolean ret;
        if(ret = database.update(map, Collections.singletonList(target)) > 0){
            this.sync();
        }
        return ret;
    }

    public boolean create(Course course) {
        if(!Pattern.matches("^\\d+$", course.getNo()))
            throw new IllegalInputException("Course's No should only be digits");

        int grade = course.getGrade();
        if (grade >= 0 && grade <= 10){
            return super.create(course);
        }
        else throw new IllegalInputException("Course's grade should in range of [0, 10]");
    }

}
