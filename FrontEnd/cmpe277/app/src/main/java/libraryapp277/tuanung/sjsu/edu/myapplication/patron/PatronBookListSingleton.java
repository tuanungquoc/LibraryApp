package libraryapp277.tuanung.sjsu.edu.myapplication.patron;

import android.content.Context;

import java.util.ArrayList;

import libraryapp277.tuanung.sjsu.edu.myapplication.Book;

/**
 * Created by t0u000c on 12/16/17.
 */

public class PatronBookListSingleton {
    private ArrayList<Book> mBooks;
    private static PatronBookListSingleton bookListSingleton;
    private Context mContext;

    private PatronBookListSingleton(Context context){
        this.mContext = context;
        mBooks = new ArrayList<>();
    }

    public static PatronBookListSingleton get(Context context){
        if(bookListSingleton == null){
            bookListSingleton = new PatronBookListSingleton(context);
        }
        return bookListSingleton;
    }

    public ArrayList<Book> getBooks()
    {
        return mBooks;
    }

    public void clearBookList(){
         mBooks.clear();
    }

    public void clearSelected(){
        for(int i = 0 ; i < mBooks.size() ;i++){
            mBooks.get(i).setIsSelect(false);
        }
    }

    public void addBook(Book book){
        mBooks.add(book);
    }

    public void delete(Book book){
        mBooks.remove(book);
    }

    public Book getBook(String id){
        for(Book book: mBooks){
            if(book.getId().equals(id)){
                return book;
            }
        }
        return null;
    }
}
