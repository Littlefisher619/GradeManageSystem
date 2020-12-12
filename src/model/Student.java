package model;

import java.util.Objects;

public class Student extends Model<String> implements Comparable<Student>{
    /* All the fields must not use base types, such as int, double... */
    private String no, name;
    private Integer age;
    private Integer gender;

    private static final long serialVersionUID = 4444000000000000000L;

    public Student(String no, String name, Integer age, Integer gender) {
        this.no = no;
        this.name = name;
        this.age = age;
        this.gender = gender;
    }

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

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Integer getGender() {
        return gender;
    }

    public void setGender(Integer gender) {
        this.gender = gender;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Student student = (Student) o;
        return no.equals(student.no);
    }

    @Override
    public int hashCode() {
        return Objects.hash(no);
    }

    public String getGenderDescription(){
        return (gender == 0 ? "Man" : "Female");
    }

    @Override
    public String toString() {
        return "Student{" +
                "no='" + no + '\'' +
                ", name='" + name + '\'' +
                ", age=" + age +
                ", gender=" + getGenderDescription()  +
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

    public static int parseGender(String gender){
        if(gender == null || gender.isEmpty()) return -1;
        if(gender.equalsIgnoreCase("man")) return 0;
        if(gender.equalsIgnoreCase("female")) return 1;
        try{
            int i = Integer.parseInt(gender);
            if(i == 0 || i == 1) return i;
        }catch (NumberFormatException e){
            return  -1;
        }
        return  -1;
    }

    @Override
    public int compareTo(Student student) {
        return no.compareTo(student.getNo());
    }
}
