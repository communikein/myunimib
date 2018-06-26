package it.communikein.myunimib.ui.login;


import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import it.communikein.myunimib.R;
import it.communikein.myunimib.databinding.FragmentPersonalDataBinding;
import it.communikein.myunimib.viewmodel.LoginViewModel;


/**
 * A simple {@link Fragment} subclass.
 */
public class PersonalDataFragment extends Fragment {

    public static final String TAG = PersonalDataFragment.class.getName();

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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mBinding.confirmButton.setOnClickListener((v) -> savePersonalData());
    }

    public void savePersonalData() {

    }
}
