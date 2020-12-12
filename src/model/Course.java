package model;

import java.io.Serializable;
import java.util.Objects;

public class Course extends Model<String> implements Comparable<Course>{
    /* All the fields must not use base types, such as int, double... */
    private String no, name;
    private Integer grade;
    private ExamType examType;

    private static final long serialVersionUID = 2222000000000000000L;

    public Course(String no, String name, Integer grade) {
        this.no = no;
        this.name = name;
        this.grade = grade;
        this.examType = ExamType.EXAM;
    }

    public Course(String no, String name, Integer grade, ExamType examType) {
        this.no = no;
        this.name = name;
        this.grade = grade;
        this.examType = examType;
    }

    public enum ExamType{EXAM, RANK}

    public String getNo() {
        return no;
    }

    public void setNo(String no) {
        this.no = no;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getGrade() {
        return grade;
    }

    public void setGrade(Integer grade) {
        this.grade = grade;
    }

    public ExamType getExamType() {
        return examType;
    }

    public void setExamType(ExamType examType) {
        this.examType = examType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Course student = (Course) o;
        return no.equals(student.no);
    }

    @Override
    public int hashCode() {
        return Objects.hash(no);
    }

    @Override
    public String toString() {
        return "Course{" +
                "no='" + no + '\'' +
                ", name='" + name + '\'' +
                ", grade=" + grade +
                ", examType=" + examType +
                '}';
    }

    @Override
    public String getKey() {
        return this.getNo();
    }

    @Override
    public void setKey(String key) {
        this.setNo(key);
    }

    @Override
    public int compareTo(Course course) {
        return no.compareTo(course.getNo());
    }
}
