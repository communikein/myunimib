package it.communikein.myunimib.ui.login;


import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import it.communikein.myunimib.R;
import it.communikein.myunimib.data.model.User;
import it.communikein.myunimib.databinding.FragmentPersonalDataBinding;
import it.communikein.myunimib.viewmodel.LoginViewModel;


/**
 * A simple {@link Fragment} subclass.
 */
public class PersonalDataFragment extends Fragment {

    public static final String TAG = PersonalDataFragment.class.getName();

    public PersonalDataCallback mCallback;
    public interface PersonalDataCallback {
        void onPersonalDataComplete(User user);
    }

    FragmentPersonalDataBinding mBinding;

    public PersonalDataFragment() {}

    public static PersonalDataFragment newInstance() {
        return new PersonalDataFragment();
    }

    private LoginViewModel getViewModel() {
        return ((LoginActivity) getActivity()).getViewModel();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_personal_data, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof PersonalDataCallback) {
            mCallback = (PersonalDataCallback) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement PersonalDataCallback");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallback = null;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        getViewModel().setTempUsername(mBinding.emailEdittext.getText().toString());
        getViewModel().setTempName(mBinding.nameEdittext.getText().toString());
        getViewModel().setTempPassword(mBinding.passwordEdittext.getText().toString());
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initUI();
    }

    private void initUI() {
        mBinding.confirmButton.setOnClickListener((v) -> savePersonalData());
    }

    public void savePersonalData() {
        String email = mBinding.emailEdittext.getText().toString();
        String password = mBinding.passwordEdittext.getText().toString();
        String name = mBinding.nameEdittext.getText().toString();

        User user = new User(email, password);
        user.setRealName(name);

        mCallback.onPersonalDataComplete(user);
    }
}
