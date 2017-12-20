package libraryapp277.tuanung.sjsu.edu.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatTextView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import libraryapp277.tuanung.sjsu.edu.myapplication.generalutilities.InputUtilities;
import libraryapp277.tuanung.sjsu.edu.myapplication.networkutilities.NetworkSingleton;

/**
 * Created by t0u000c on 12/15/17.
 */

public class ConfirmationFragment extends Fragment {

    public static final String EXTRA_CONFIRM_EMAIL =
            "confirm_email";

    private InputUtilities inputUtilities;

    private TextInputLayout confirmEmailTextInputLayout;
    private TextInputEditText confirmEmailTextInputEditText;

    private TextInputLayout tokenTextInputLayout;
    private TextInputEditText tokenTextInputEditText;

    private AppCompatButton confirmBtn;

    private AppCompatTextView textViewLinkResendToken;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_confirmation, parent, false);
        //Init all controls
        inputUtilities = new InputUtilities(getActivity());
        confirmEmailTextInputLayout = (TextInputLayout) v.findViewById(R.id.textInputLayoutConfirmEmail);
        confirmEmailTextInputEditText = (TextInputEditText) v.findViewById(R.id.textInputEditTextConfirmEmail);
        if(getActivity().getIntent().getStringExtra(EXTRA_CONFIRM_EMAIL)!=null){
            confirmEmailTextInputEditText.setText(getActivity().getIntent().getStringExtra(EXTRA_CONFIRM_EMAIL));
        }
        tokenTextInputLayout = (TextInputLayout) v.findViewById(R.id.textInputLayoutToken);
        tokenTextInputEditText = (TextInputEditText) v.findViewById(R.id.textInputEditTextToken);
        confirmBtn = (AppCompatButton) v.findViewById(R.id.appCompatButtonTokenConfirm);
        textViewLinkResendToken = (AppCompatTextView) v.findViewById(R.id.textViewLinkResendToken);

        //Add listners
        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(inputUtilities.isTxtBoxEmpty(confirmEmailTextInputEditText,confirmEmailTextInputLayout,"Email cannot be empty")){
                    return;
                }
                if(!inputUtilities.isEmail(confirmEmailTextInputEditText,confirmEmailTextInputLayout,"Email is not the right format")){
                    return;
                }
                if(inputUtilities.isTxtBoxEmpty(tokenTextInputEditText,tokenTextInputLayout,"Token cannot be empty")){
                    return;
                }
                //send a request to confirm account
                JSONObject payload = new JSONObject();
                try {
                    payload.put("email" , confirmEmailTextInputEditText.getText().toString());
                    payload.put("token", tokenTextInputEditText.getText().toString());

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                        API.confirmURL(), payload,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    JSONObject object = new JSONObject(response.toString());
                                    boolean success = object.getBoolean(("success"));
                                    if(success){
                                        //Send to confirmation page
                                        Toast.makeText(getActivity(), "Account is confirmed successfully.", Toast.LENGTH_LONG).show();
                                        Intent intent = new Intent(getActivity(), LoginActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(intent);
                                    }else{
                                        Toast.makeText(getActivity(),object.getString("msg"), Toast.LENGTH_LONG).show();
                                    }

                                } catch (Exception ex) {
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                JSONObject jsonObj = null;
                                NetworkResponse networkResponse = error.networkResponse;
                                if(networkResponse != null) {
                                    try {
                                        jsonObj = new JSONObject(new String(networkResponse.data));
                                        Toast.makeText(getActivity(), jsonObj.getString("msg"), Toast.LENGTH_LONG).show();
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }else{
                                    Toast.makeText(getActivity(), "There is an error. Please contact admin for more info", Toast.LENGTH_LONG).show();

                                }
                            }
                        });
                NetworkSingleton.get(getActivity()).addRequest(jsonObjectRequest, "Account Confirmation");

            }
        });


        textViewLinkResendToken.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //checking to see if email and token is not empty
                if(inputUtilities.isTxtBoxEmpty(confirmEmailTextInputEditText,confirmEmailTextInputLayout,"Email cannot be empty")){
                    return;
                }

                if(!inputUtilities.isEmail(confirmEmailTextInputEditText,confirmEmailTextInputLayout,"Email is not right format")){
                    return;
                }


                //send a request to confirm account
                JSONObject payload = new JSONObject();
                try {
                    payload.put("email" , confirmEmailTextInputEditText.getText().toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                        API.resendTokenURL(), payload,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    JSONObject object = new JSONObject(response.toString());
                                    Toast.makeText(getActivity(),object.getString("msg"), Toast.LENGTH_LONG).show();

                                } catch (Exception ex) {
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                JSONObject jsonObj = null;
                                NetworkResponse networkResponse = error.networkResponse;
                                if(networkResponse != null) {
                                    try {
                                        jsonObj = new JSONObject(new String(networkResponse.data));
                                        Toast.makeText(getActivity(), jsonObj.getString("msg"), Toast.LENGTH_LONG).show();
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }else{
                                    Toast.makeText(getActivity(), "There is an error. Please contact admin for more info", Toast.LENGTH_LONG).show();

                                }
                            }
                        });
                NetworkSingleton.get(getActivity()).addRequest(jsonObjectRequest, "Resend Token");
            }
        });

        return v;
    }
}
