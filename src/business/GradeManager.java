package business;

import exceptions.ConstraintException;
import exceptions.IllegalInputException;
import model.Course;
import model.Grade;
import model.Student;
import persistence.Database;
import persistence.Pair;
import persistence.datapool.HashMapDataPool;

import java.util.*;

import static java.lang.System.out;

public class GradeManager extends BusinessManager<String, Grade>{
    private static GradeManager instance = new GradeManager();
    public static GradeManager getInstance() {
        return instance;
    }
    private GradeManager(){
        database = new Database<>(Grade.class, new HashMapDataPool<>());
    }

    public Collection<Grade> queryByStudent(Student student){
        return database.where("student", student.getKey());
    }

    public Collection<Grade> queryByCourse(Course course){
        return database.where("course", course.getKey());
    }

    public Collection<Grade> queryByScore(Double gradeore){
        return database.where("gradeore", gradeore);
    }

    public Collection<Grade> queryByRank(Grade.Rank rank){
        return database.where("rank", rank);
    }

    public Collection<Grade> queryByScore(Double gradeoreFrom, Double gradeoreTo){
        return database.where("age", gradeoreFrom, gradeoreTo);
    }

    public boolean deleteByKey(Student student, Course course){
        return deleteByKey(Grade.getKey(student, course));
    }

    public Grade queryByKey(Student student, Course course){
        return queryByKey(Grade.getKey(student, course));
    }

    public boolean update(HashMap<String, Object> map, String key){
        Grade target = database.retrieveOne("key", key);

        Course course = this.getNestedCourse(target);

        if(map.containsKey("student") || map.containsKey("course")){
             throw new ConstraintException("Updating 'student' or 'grade' is RESTRICTED");
        }
        if(course.getExamType() == Course.ExamType.EXAM && map.containsKey("score")){
            Double score = Double.parseDouble((String) map.get("score"));
            if(100 >= score && 0 <= score){
                target.setScore(score);
            }else{
                throw new IllegalInputException("Score must in range of [0, 100]");
            }

        }
        if(course.getExamType() == Course.ExamType.RANK && map.containsKey("rank")){
            target.setRank(Grade.Rank.valueOf((String) map.get("rank")));
        }

        return super.update(target);
    }

    public boolean create(Grade grade){
        String student = grade.getStudent();
        String course = grade.getCourse();
        if(
            StudentManager.getInstance().queryByKey(student)!=null &&
            CourseManager.getInstance().queryByKey(course)!=null
        ){
            return super.create(grade);
        }else{
            throw new ConstraintException("Student or Course not found in DB");
        }

    }

    public Student getNestedStudent(Grade grade){
        return this.getNested("student", grade, StudentManager.getInstance());
    }

    public Collection<Pair<Course, Grade>> getCurrentGrades(Student student){
        return CourseManager.getInstance().join(
                new HashMap<>(){
                    {
                        put("key", "course");
                    }
                }
                ,this, queryByStudent(student));
    }

    public Collection<Course> getCurrentCourses(Student student){
        ArrayList<Course> ret = new ArrayList<>();
        for(Pair<Course, Grade> p: getCurrentGrades(student)){
            ret.add(p.getKey());
        }
        return ret;
    }

    public Collection<Course> getCoursesCanJoin(Student student){
        Collection<Course> coursesCanJoin=CourseManager.getInstance().getAll();
        coursesCanJoin.removeAll(
                getCurrentCourses(student)
        );
        return coursesCanJoin;
    }

    public Collection<Course> getCoursesHasMark(Student student){
        Collection<Pair<Course, Grade>> data=CourseManager.getInstance().join(
                new HashMap<>(){
                    {
                        put("key", "course");
                    }
                }
                ,this, queryByStudent(student));
        ArrayList<Course> ret =new ArrayList<>();
        for(Pair<Course, Grade> pair : data){
            if(pair.getValue().getRank() != null || pair.getValue().getScore() != null){
                ret.add(pair.getKey());
            }
        }
        return ret;
    }

    public Collection<Course> getCoursesCanRevoke(Student student){
        Collection<Grade> nullGrades = database.filter(
                new HashMap<>() {
                    {
                        put("score", null);
                        put("rank", null);
                    }
                }
        , queryByStudent(student), -1);


        ArrayList<Course> ret = new ArrayList<>();
        Collection<Pair<Course, Grade>> data=CourseManager.getInstance().join(
                new HashMap<>(){
                    {
                        put("key", "course");
                    }
                }
                ,this, nullGrades);
        for(Pair<Course, Grade> p: data){
            ret.add(p.getKey());
        }
        return ret;
    }

    public Course getNestedCourse(Grade grade){
        return this.getNested("course", grade, CourseManager.getInstance());
    }


    public Map<String, Object> getStatistics(Course course){
        HashMap<String, Object> result = new HashMap<>();
        HashMap<Grade.Rank, Integer> rankCount = new HashMap<>();
        HashMap<Student, Grade> allGrades = new HashMap<>();

        Collection<Pair<Student, Grade>> grades = StudentManager.getInstance().join(
                new HashMap<>(){
                    {
                        put("key", "student");
                    }
                }
        , this, queryByCourse(course));


        if(course.getExamType() == Course.ExamType.RANK){
            for(Grade.Rank rank: Grade.Rank.values()){
                rankCount.put(rank, 0);
            }
        }


        int nullCount = 0;
        Grade maxGrade = null, minGrade = null;
        ArrayList<Pair<Student, Grade>> sortedGrades = new ArrayList<>();

        for(Pair<Student, Grade> pair: grades){
            Grade grade = pair.getValue();
            allGrades.put(pair.getKey(), pair.getValue());
            if(grade.getMarkDescription() == null){
                nullCount ++;
            }else{
                sortedGrades.add(pair);

                if(maxGrade == null) maxGrade = grade;
                if(minGrade == null) minGrade = grade;
                maxGrade = maxGrade.compareTo(grade) >= 0 ? maxGrade : grade;
                minGrade = minGrade.compareTo(grade) <= 0 ? minGrade : grade;

                if(course.getExamType() == Course.ExamType.RANK){
                    Grade.Rank rank = pair.getValue().getRank();
                    rankCount.put(rank, rankCount.get(rank) + 1);
                }
            }
        }
        sortedGrades.sort(Map.Entry.<Student, Grade>comparingByValue().reversed());

        result.put("null", nullCount);
        result.put("total", grades.size());
        result.put("valid", grades.size() - nullCount);
        result.put("sorted", sortedGrades);
        result.put("max", maxGrade);
        result.put("min", minGrade);
        result.put("all", allGrades);
        if(course.getExamType() == Course.ExamType.RANK) {
            result.put("rank", rankCount);
        }

        return result;
    }

}
