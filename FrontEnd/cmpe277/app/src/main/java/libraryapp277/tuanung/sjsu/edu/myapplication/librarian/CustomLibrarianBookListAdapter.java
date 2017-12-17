package libraryapp277.tuanung.sjsu.edu.myapplication.librarian;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import libraryapp277.tuanung.sjsu.edu.myapplication.Book;
import libraryapp277.tuanung.sjsu.edu.myapplication.R;

/**
 * Created by VimmiRao on 12/1/2017.
 */

public class CustomLibrarianBookListAdapter extends ArrayAdapter<Book>{

    private ArrayList<Book> dataSet;
    Context mContext;

    // View lookup cache
    private static class ViewHolder {
        TextView title;
        TextView author;
        TextView publisher;
        TextView year;
        TextView copies;
        TextView callnumber;
        TextView status;
        TextView location;
        TextView keywords;
        ImageView image;
    }

    public CustomLibrarianBookListAdapter(ArrayList<Book> data, Context context) {
        super(context, R.layout.librarian_book_list_content, data);
        this.dataSet = data;
        this.mContext=context;

    }

    private int lastPosition = -1;

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Book dataModel = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag

        final View result;

        if (convertView == null) {

            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.librarian_book_list_content, parent, false);
            viewHolder.title = (TextView) convertView.findViewById(R.id.title);
            viewHolder.author = (TextView) convertView.findViewById(R.id.author);
            viewHolder.publisher = (TextView) convertView.findViewById(R.id.publish);
            viewHolder.year = (TextView) convertView.findViewById(R.id.year);
            viewHolder.copies = (TextView) convertView.findViewById(R.id.copies);
            viewHolder.callnumber = (TextView) convertView.findViewById(R.id.callnumber);
            viewHolder.status = (TextView) convertView.findViewById(R.id.status);
            viewHolder.location = (TextView) convertView.findViewById(R.id.location);
            viewHolder.keywords = (TextView) convertView.findViewById(R.id.keywords);
            viewHolder.image = (ImageView) convertView.findViewById(R.id.book_cover);

            result=convertView;

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result=convertView;
        }

        lastPosition = position;
        viewHolder.title.setText("Title: "+dataModel.getTitle());
        viewHolder.author.setText("Author: "+dataModel.getAuthor());
        viewHolder.publisher.setText("Publisher: "+dataModel.getPublisher());
        viewHolder.year.setText("Year: "+dataModel.getYear());
        viewHolder.copies.setText("Copies: "+dataModel.getCopies());
        viewHolder.callnumber.setText("Call Number: "+dataModel.getCallnumber());
        viewHolder.status.setText("Status: "+dataModel.getStatus());
        viewHolder.location.setText("Location: "+dataModel.getLocation());
        viewHolder.keywords.setText("Keywords: "+dataModel.getKeywords());

        byte[] decodedString = Base64.decode(dataModel.getImage(), Base64.DEFAULT);
        Log.d("Decoded", String.valueOf(decodedString));
        Log.d("DecodedLength", String.valueOf(decodedString.length));
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        viewHolder.image.setImageBitmap(decodedByte);

        // Return the completed view to render on screen
        return convertView;
    }
}
