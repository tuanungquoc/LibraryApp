package libraryapp277.tuanung.sjsu.edu.myapplication.librarian;

import android.content.Context;

import java.util.ArrayList;

import libraryapp277.tuanung.sjsu.edu.myapplication.Book;

/**
 * Created by t0u000c on 12/16/17.
 */

public class BookListSingleton {
    private ArrayList<Book> mBooks;
    private static BookListSingleton bookListSingleton;
    private Context mContext;

    private BookListSingleton(Context context){
        this.mContext = context;
        mBooks = new ArrayList<>();
    }

    public static BookListSingleton get(Context context){
        if(bookListSingleton == null){
            bookListSingleton = new BookListSingleton(context);
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
