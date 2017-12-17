package libraryapp277.tuanung.sjsu.edu.myapplication.librarian;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import libraryapp277.tuanung.sjsu.edu.myapplication.API;
import libraryapp277.tuanung.sjsu.edu.myapplication.Book;
import libraryapp277.tuanung.sjsu.edu.myapplication.MainActivity;
import libraryapp277.tuanung.sjsu.edu.myapplication.R;
import libraryapp277.tuanung.sjsu.edu.myapplication.generalutilities.SessionManagement;
import libraryapp277.tuanung.sjsu.edu.myapplication.networkutilities.NetworkSingleton;

/**
 * Created by t0u000c on 12/16/17.
 */

public class AddBookFragment extends Fragment {
    EditText title;
    EditText author;
    EditText publisher;
    EditText year;
    EditText copies;
    EditText callnumber;
    EditText status;
    EditText location;
    EditText keywords;
    TextView heading;
    ImageView image;
    private Button submit;
    private Button cancel;
    private Button upload;
    String simage=null;
    int bookpos = 96321456;
    SessionManagement sessionManagement;
    public static final int RESULT_GALLERY = 0;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_add_book, parent, false);
        sessionManagement = new SessionManagement(getActivity());
        heading = (TextView) v.findViewById(R.id.heading);
        title = (EditText) v.findViewById(R.id.title);
        author = (EditText) v.findViewById(R.id.author);
        publisher = (EditText) v.findViewById(R.id.publish);
        year = (EditText) v.findViewById(R.id.year);
        copies = (EditText) v.findViewById(R.id.copies);
        callnumber = (EditText) v.findViewById(R.id.callnumber);
        status = (EditText) v.findViewById(R.id.status);
        location = (EditText) v.findViewById(R.id.location);
        keywords = (EditText) v.findViewById(R.id.keywords);
        submit = (Button) v.findViewById(R.id.add);
        cancel = (Button) v.findViewById(R.id.cancel);
        upload = (Button) v.findViewById(R.id.upload);
        //image = (ImageView) v.findViewById(R.id.book_cover);

        clearinput();

        Intent i = getActivity().getIntent();
        bookpos = i.getIntExtra("Objectpos",96321456);
        Bundle temp = i.getExtras();
        if (temp != null && bookpos!= 96321456 ){
            ArrayList<Book> bookList = BookListSingleton.get(getActivity()).getBooks();
            title.setText(bookList.get(bookpos).getTitle());
            author.setText(bookList.get(bookpos).getAuthor());
            publisher.setText(bookList.get(bookpos).getPublisher());
            year.setText(bookList.get(bookpos).getYear());
            copies.setText(bookList.get(bookpos).getCopies());
            callnumber.setText(bookList.get(bookpos).getCallnumber());
            status.setText(bookList.get(bookpos).getStatus());
            location.setText(bookList.get(bookpos).getLocation());
            keywords.setText(bookList.get(bookpos).getKeywords());
            //byte[] decodedString = Base64.decode(bookList.get(bookpos).getImage(), Base64.DEFAULT);
            //Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            //image.setImageBitmap(decodedByte);
            submit.setText("Edit");
            heading.setText("Edit Book");
        }

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                JSONObject payload = new JSONObject();
                try {
                    payload.put("author", author.getText());
                    payload.put("title", title.getText());
                    payload.put("callNumber", callnumber.getText());
                    payload.put("publisher", publisher.getText());
                    payload.put("year", year.getText());
                    payload.put("location", location.getText());
                    payload.put("copies", copies.getText());
                    payload.put("status", status.getText());
                    payload.put("keywords", keywords.getText());
                    payload.put("enteredby", sessionManagement.getSessionDetails().get(SessionManagement.KEY_USER_ID));
//                    Bitmap bm =((BitmapDrawable) image.getDrawable()).getBitmap();
//                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
//                    bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);
//                    byte[] b = baos.toByteArray();
//                    payload.put("image",Base64.encodeToString(b, Base64.NO_WRAP));
                }catch (JSONException e) {
                    e.printStackTrace();
                }
                if(!submit.getText().equals("Add")){
                    // TODO GET LIBRARIAN ID FROM SESSION!!!!!!!!
                    String url = API.UpdateBooks ;

                    try {
                        payload.put("oauthor", BookListSingleton.get(getActivity()).getBooks().get(bookpos).getAuthor());
                        payload.put("otitle",  BookListSingleton.get(getActivity()).getBooks().get(bookpos).getTitle());

                    }catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Log.d("URL", url);
                    volleyStringRequest(url,payload,false);
                }else {
                    // TODO GET LIBRARIAN ID FROM SESSION!!!!!!!!
                    String url = API.PostBooks ;
                    Log.d("URL", url);
                    volleyStringRequest(url,payload,true);
                }
                Log.d("AFTER VOLLEY","k");

            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mainIntent = new Intent(getActivity(), MainActivity.class);
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                getActivity().finish();
                startActivity(mainIntent);
            }
        });



        return v;
    }

    //Volley to Add Book data
    public void volleyStringRequest(String url,JSONObject payload, boolean isAdd){
        Log.d("before VOLLEY","q");
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(isAdd ? Request.Method.POST : Request.Method.PUT,
                url, payload, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    Log.d("VolleyREsponse", response.toString());
                    Toast.makeText(getActivity(),response.getString("msg"),Toast.LENGTH_LONG).show();

                } catch (Exception ex) {
                }
                clearinput();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                JSONObject jsonObj = null;

                NetworkResponse networkResponse = error.networkResponse;
                if(networkResponse != null) {
                    try {
                        jsonObj = new JSONObject(new String(networkResponse.data));
                        Toast.makeText(getActivity(),jsonObj.getString("msg"),Toast.LENGTH_LONG).show();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }else{
                    Toast.makeText(getActivity(), "There is an error. Please contact admin for more info", Toast.LENGTH_LONG).show();

                }
                clearinput();
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                //headers.put("Content-Type", "application/json");
                headers.put("x-access-token", sessionManagement.getSessionDetails().get(SessionManagement.KEY_TOKEN));
                headers.put("Content-Type","application/json");
                return headers;
            }
        };
        int socketTimeout = 30000;
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonObjectRequest.setRetryPolicy(policy);
        NetworkSingleton.get(getActivity()).addRequest(jsonObjectRequest, "Add Book");

    }
    private void clearinput(){

        title.setText("");
        title.setText("");
        author.setText("");
        publisher.setText("");
        year.setText("");
        copies.setText("");
        callnumber.setText("");
        status.setText("");
        location.setText("");
        keywords.setText("");
        //image.setImageDrawable(null);
    }
}

