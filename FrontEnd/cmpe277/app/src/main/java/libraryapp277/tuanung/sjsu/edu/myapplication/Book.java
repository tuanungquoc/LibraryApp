package libraryapp277.tuanung.sjsu.edu.myapplication;

/**
 * Created by VimmiRao on 12/1/2017.
 */

public class Book {


    String id;
    String author;
    String title;
    String callnumber;
    String publisher;
    String year;
    String location;
    String copies;
    String status;
    String keywords;
    String image;
    boolean isSelected;
    public Book(String id, String author, String title, String callnumber, String publisher, String year, String location, String copies, String status, String keywords, String image) {
        this.id = id;
        this.author = author;
        this.title = title;
        this.callnumber = callnumber;
        this.publisher = publisher;
        this.year = year;
        this.location = location;
        this.copies = copies;
        this.status = status;
        this.keywords = keywords;
        this.image = image;
        this.isSelected = false;
    }


    public boolean getIsSelected(){
        return isSelected;
    }
    public void setIsSelect(boolean selected){
        this.isSelected = selected;
    }
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCallnumber() {
        return callnumber;
    }

    public void setCallnumber(String callnumber) {
        this.callnumber = callnumber;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getCopies() {
        return copies;
    }

    public void setCopies(String copies) {
        this.copies = copies;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

}
