package model;


import java.math.BigDecimal;
import java.util.Objects;

public class Grade extends Model<String> implements Comparable<Grade>{

    private String student;
    private String course;
    private Double score;
    private Rank rank;

    private static final long serialVersionUID = 3333000000000000000L;

    public Grade(String student, String course, Double score) {
        this.student = student;
        this.course = course;
        this.score = score;
    }

    public Grade(String student, String course, Rank rank) {
        this.student = student;
        this.course = course;
        this.rank = rank;
    }

    public Grade(String student, String course) {
        this.student = student;
        this.course = course;
    }

    @Override
    public int compareTo(Grade grade) {
        int courseCompare = course.compareTo(grade.course);
        int studentCompare = student.compareTo(grade.student);
        int scoreCompare, rankCompare;
        if(courseCompare == 0){
            if(rank == null && score == null)
                return studentCompare;
            else if(rank == null){
                scoreCompare = score.compareTo(grade.score);
                if(scoreCompare == 0){
                    return studentCompare;
                }else return scoreCompare;
            }else{
                rankCompare = rank.compareTo(grade.rank);
                if(rankCompare == 0){
                    return studentCompare;
                }else return rankCompare;
            }

        }else return courseCompare;

    }

    public enum Rank{E, D, C, B, A}//worst -> best

    public String getStudent() {
        return student;
    }

    public void setStudent(String student) {
        this.student = student;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = (double) Math.round(score * 10) / 10;
    }

    public Rank getRank() {
        return this.rank;
    }

    public void setRank(Rank rank) {
        this.rank = rank;
    }

    @Override
    public String toString() {
        return "Grade{" +
                "student='" + student + '\'' +
                ", course='" + course + '\'' +
                ", score=" + score  +
                ", rank='" + rank + '\'' +
                '}';
    }

    public String getMarkDescription(){
        if(score == null && rank == null) return null;
        else if(score == null) return rank.name();
        else return String.valueOf(score);
    }

    @Override
    public String getKey() {
        return student + "_" + course;
    }


    public static String getKey(Student student, Course course) {
        return student.getKey() + "_" + course.getKey();
    }

    @Override
    public void setKey(String key) {
        throw new UnsupportedOperationException("Cannot set virtual field No");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Grade grade = (Grade) o;
        return student.equals(grade.student) && course.equals(grade.course);
    }

    @Override
    public int hashCode() {
        return Objects.hash(student, course);
    }
}
