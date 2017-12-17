package libraryapp277.tuanung.sjsu.edu.myapplication.generalutilities;

/**
 * Created by t0u000c on 12/2/17.
 */

import android.app.Activity;
import android.content.Context;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

public class InputUtilities {
    private Context ctx;

    public InputUtilities(Context context){
        this.ctx = context;
    }

    /*
        This method to check if text input is filled. If not, it will show err message as msg
     */
    public boolean isTxtBoxEmpty(TextInputEditText mainTextBox, TextInputLayout layoutTextBox, String msg){
        String textInput = mainTextBox.getText().toString().trim(); //remove all the space
        if(textInput.isEmpty()) {
            layoutTextBox.setError(msg);
            hideKeyboardFrom(mainTextBox);
            return true;
        }else{
            layoutTextBox.setErrorEnabled(false);
            return false;
        }
    }

    /**
     * This method helps to check if password and its confrm password are matching
     * @param mainTextBox
     * @param confirmTextInput
     * @param layoutTextInput
     * @param msg
     * @return
     */
    public boolean isTxtMatchedFrom(TextInputEditText mainTextBox, TextInputEditText confirmTextInput, TextInputLayout layoutTextInput, String msg){
        String mainTxtInput = mainTextBox.getText().toString().trim();
        String confTxtInput = confirmTextInput.getText().toString().trim();
        if(mainTxtInput.equals(confTxtInput) && !mainTxtInput.isEmpty()){
            layoutTextInput.setErrorEnabled(false);
            return true;
        }else{
            layoutTextInput.setError(msg);
            hideKeyboardFrom(confirmTextInput);
            return false;
        }

    }

    public boolean isEmail(TextInputEditText mainTxtBox, TextInputLayout layoutTextInput, String msg) {
        String value = mainTxtBox.getText().toString().trim();
        if (value.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(value).matches()) {
            layoutTextInput.setError(msg);
            hideKeyboardFrom(mainTxtBox);
            return false;
        } else {
            layoutTextInput.setErrorEnabled(false);
            return true;
        }
    }

    private void hideKeyboardFrom(View view) {
        InputMethodManager imm = (InputMethodManager) ctx.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }
}
