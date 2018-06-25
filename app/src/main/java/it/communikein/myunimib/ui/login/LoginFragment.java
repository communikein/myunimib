package it.communikein.myunimib.ui.login;


import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import it.communikein.myunimib.R;
import it.communikein.myunimib.databinding.FragmentLoginBinding;
import it.communikein.myunimib.utilities.NetworkHelper;
import it.communikein.myunimib.utilities.Utils;

/**
 * A simple {@link Fragment} subclass.
 */
public class LoginFragment extends Fragment {

    public static final String TAG = LoginFragment.class.getName();

    public static final int ERROR_NO_INTERNET = -1;

    public LoginProcessCallback mCallback;
    public interface LoginProcessCallback {
        void onLoginProcessBlocked(int errorCode);
        void onLoginProcessOk(String username, String password);
        void onLoginAsGuest();
    }

    private FragmentLoginBinding mBinding;


    public LoginFragment() {}

    public static LoginFragment newInstance() {
        return new LoginFragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_login, container, false);

        mBinding.usernameEdittext.requestFocus();
        mBinding.usernameEdittext.setText("");
        mBinding.passwordEdittext.setText("");

        return mBinding.getRoot();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof LoginProcessCallback) {
            mCallback = (LoginProcessCallback) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement LoginProcessCallback");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallback = null;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initUI();
    }

    private void initUI() {
        mBinding.buttonLogin.setOnClickListener(view -> attemptLogin());
        mBinding.guestAccessButton.setOnClickListener(view -> mCallback.onLoginAsGuest());

        initTermsCheck();
    }

    private void attemptLogin() {

        /* If the device is online */
        if (NetworkHelper.isDeviceOnline(getActivity())) {
            Utils.hideKeyboard(getActivity());

            /* Reset errors. */
            mBinding.usernameTextInputLayout.setError(null);
            mBinding.passwordTextInputLayout.setError(null);

            boolean cancel = false;
            View focusView = null;

            String password = mBinding.passwordEdittext.getText().toString();
            /* Check for a valid password, if the user entered one. */
            if (!isPasswordValid(mBinding.passwordTextInputLayout)) {
                focusView = mBinding.passwordTextInputLayout;
                cancel = true;
            }

            /* Check for a valid username, if the user entered one. */
            String username = validateUsername(mBinding.usernameTextInputLayout);
            if (username == null) {
                focusView = mBinding.usernameTextInputLayout;
                cancel = true;
            }

            /* If there are errors to show */
            if (cancel) {
                /* Cancel the login process and focus on the field with an error. */
                focusView.requestFocus();
            }
            /* No errors found */
            else
                mCallback.onLoginProcessOk(username, password);
        }
        else
            mCallback.onLoginProcessBlocked(ERROR_NO_INTERNET);
    }

    @Nullable
    private String validateUsername(@Nullable TextInputLayout view) {
        /* If there is no view to work with, return */
        if (view == null || view.getEditText() == null) return null;

        /* Get the username */
        String user = view.getEditText().getText().toString().trim();

        /* Check the username for errors */
        if (TextUtils.isEmpty(user)){
            /* The username is empty, set the error and return. */
            user = null;
            view.setError(getString(R.string.error_user_empty));
        } else if (user.contains(" ")) {
            /* The username contains whitespaces, set the error and return. */
            user = null;
            view.setError(getString(R.string.error_user_with_blank_spaces));
        } else if (user.contains("@")) {
            /*
             * The user has input the university email, but the app only needs the username,
             * so remove everything that is after the '@', including '@', and return.
             */
            user = user.substring(0, user.indexOf("@"));
        }

        return user;
    }

    private boolean isPasswordValid(@Nullable TextInputLayout view) {
        if (view == null || view.getEditText() == null) return false;

        String password = view.getEditText().getText().toString().trim();
        boolean valid = true;

        if (TextUtils.isEmpty(password)){
            valid = false;
            view.setError(getString(R.string.error_password_empty));
        } else if (password.length() < 10){
            valid = false;
            view.setError(getString(R.string.error_password_less_then_10_chars));
        } else if (password.length() > 20){
            valid = false;
            view.setError(getString(R.string.error_password_more_then_20_chars));
        } else if (!password.matches("(.)*[0-9](.)*[0-9](.)*")){
            valid = false;
            view.setError(getString(R.string.error_password_less_then_2_digits));
        }

        return valid;
    }

    public void signalWrongCredentials() {
        mBinding.passwordTextInputLayout.setError(getString(R.string.error_username_password_incorrect));
        mBinding.passwordTextInputLayout.requestFocus();
    }

    private void initTermsCheck() {
        String text = getString(R.string.terms_and_conditions);
        SpannableString ss = new SpannableString(text);
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                Intent browserIntent = new Intent(
                        Intent.ACTION_VIEW, Uri.parse(Utils.TERMS_CONDITIONS_URL));
                startActivity(browserIntent);
            }
        };
        ss.setSpan(clickableSpan,
                getResources().getInteger(R.integer.start_terms_and_conditions),
                getResources().getInteger(R.integer.end_terms_and_conditions),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        mBinding.termsCheck.setText(ss);
        mBinding.termsCheck.setMovementMethod(LinkMovementMethod.getInstance());
    }
}
