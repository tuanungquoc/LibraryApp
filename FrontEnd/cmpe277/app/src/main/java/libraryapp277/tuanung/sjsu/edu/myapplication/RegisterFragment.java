package libraryapp277.tuanung.sjsu.edu.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatTextView;
import android.util.Log;
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

public class RegisterFragment extends Fragment {
    private TextInputLayout nameTxtInputLayout;
    private TextInputEditText nameTxtInputEditText;

    private TextInputLayout studentIDTxtInputLayout;
    private TextInputEditText studentIDTxtInputEditText;

    private TextInputLayout emailTxtInputLayout;
    private TextInputEditText emailTxtInputEditText;

    private TextInputLayout pwdTxtInputLayout;
    private TextInputEditText pwdTxtInputEditText;

    private TextInputLayout confPwdTxtInputLayout;
    private TextInputEditText confPwdTxtInputEditText;

    private AppCompatButton registerBtn;

    private AppCompatTextView logInTxt;

    private InputUtilities inputUtilities;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_register, parent, false);
        //Init all controls
        inputUtilities = new InputUtilities(getActivity());


        nameTxtInputLayout = (TextInputLayout) v.findViewById(R.id.textInputLayoutName);
        nameTxtInputEditText = (TextInputEditText) v.findViewById(R.id.textInputEditTextName);

        studentIDTxtInputLayout = (TextInputLayout) v.findViewById(R.id.textInputLayoutStudentId);
        studentIDTxtInputEditText = (TextInputEditText) v.findViewById(R.id.textInputEditTextStudentId);

        emailTxtInputLayout = (TextInputLayout) v.findViewById(R.id.textInputLayoutEmail);
        emailTxtInputEditText = (TextInputEditText) v.findViewById(R.id.textInputEditTextEmail);

        pwdTxtInputLayout = (TextInputLayout) v.findViewById(R.id.textInputLayoutPassword);
        pwdTxtInputEditText = (TextInputEditText) v.findViewById(R.id.textInputEditTextPassword);

        confPwdTxtInputLayout = (TextInputLayout) v.findViewById(R.id.textInputLayoutConfirmPassword);
        confPwdTxtInputEditText = (TextInputEditText) v.findViewById(R.id.textInputEditTextConfirmPassword);

        registerBtn = (AppCompatButton) v.findViewById(R.id.appCompatButtonRegister);
        logInTxt = (AppCompatTextView) v.findViewById(R.id.appCompatTextViewLoginLink);

        //Add all listener for btn
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(inputUtilities.isTxtBoxEmpty(nameTxtInputEditText,nameTxtInputLayout,"Name cannot be empty")){
                    return;
                }
                if(inputUtilities.isTxtBoxEmpty(studentIDTxtInputEditText,studentIDTxtInputLayout,"StudentID cannot be empty")){
                    return;
                }
                if(inputUtilities.isTxtBoxEmpty(emailTxtInputEditText,emailTxtInputLayout,"Email cannot be empty")){
                    return;
                }
                if(!inputUtilities.isEmail(emailTxtInputEditText,emailTxtInputLayout,"Email is not right format")){
                    return;
                }
                if(inputUtilities.isTxtBoxEmpty(pwdTxtInputEditText,pwdTxtInputLayout,"Password cannot be empty")){
                    return;
                }
                if(!inputUtilities.isTxtMatchedFrom(pwdTxtInputEditText,confPwdTxtInputEditText,confPwdTxtInputLayout,"Password is not matching")){
                    return;
                }
                String test = API.registerURL();
                JSONObject payload = new JSONObject();
                try {
                    payload.put("name" , nameTxtInputEditText.getText().toString());
                    payload.put("email" , emailTxtInputEditText.getText().toString());
                    payload.put("password" , pwdTxtInputEditText.getText().toString());
                    payload.put("studentID",studentIDTxtInputEditText.getText().toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }


                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                        API.registerURL(), payload,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    JSONObject object = new JSONObject(response.toString());
                                    boolean success = object.getBoolean(("success"));
                                    if(success){
                                        //Send to confirmation page
                                        Toast.makeText(getActivity(), "Account is created successfully. Please check your email for confirmation token", Toast.LENGTH_LONG).show();
                                        Intent intentConfirmation = new Intent(getActivity(), ConfirmationActivity.class);
                                        intentConfirmation.putExtra(ConfirmationFragment.EXTRA_CONFIRM_EMAIL , emailTxtInputEditText.getText().toString());
                                        startActivity(intentConfirmation);
                                    }else{
                                        Toast.makeText(getActivity(),object.getString("msg"), Toast.LENGTH_LONG).show();
                                    }

                                } catch (Exception ex) {
                                    Log.e("err:", ex.getMessage());
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
                NetworkSingleton.get(getActivity()).addRequest(jsonObjectRequest, "Sign up");


            }
        });

        logInTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().finish();
            }
        });

        return v;
    }
}
