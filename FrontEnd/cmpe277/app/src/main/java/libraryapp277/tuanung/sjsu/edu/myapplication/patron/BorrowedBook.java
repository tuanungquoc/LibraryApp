package libraryapp277.tuanung.sjsu.edu.myapplication.patron;

import libraryapp277.tuanung.sjsu.edu.myapplication.Book;

/**
 * Created by t0u000c on 12/16/17.
 */

public class BorrowedBook extends Book {
    private String dueDate;
    private String patronId;

    public BorrowedBook(String id, String author, String title, String callnumber, String publisher, String year, String location, String copies, String status, String keywords, String image, String dueDate, String patronId) {
        super(id, author, title, callnumber, publisher, year, location, copies, status, keywords, image);
        dueDate = dueDate;
        patronId = patronId;
    }

    public String getDueDate(){
        return dueDate;
    }

    public String getBorrowerId(){
        return patronId;
    }
}
