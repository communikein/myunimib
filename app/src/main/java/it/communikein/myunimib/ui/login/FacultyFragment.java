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
import android.widget.AdapterView;

import it.communikein.myunimib.R;
import it.communikein.myunimib.data.model.Faculty;
import it.communikein.myunimib.databinding.FragmentChooseFacultyBinding;
import it.communikein.myunimib.viewmodel.LoginViewModel;


public class FacultyFragment extends Fragment implements
        AdapterView.OnItemSelectedListener {

    public static final String TAG = FacultyFragment.class.getName();

    private FragmentChooseFacultyBinding mBinding;

    private FacultiesArrayAdapter mAdapter;
    public FacultyChooseProcessCallback mCallback;

    public interface FacultyChooseProcessCallback {
        void onFacultyChosen();
    }

    public FacultyFragment() {}

    public static FacultyFragment newInstance() {
        return new FacultyFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_choose_faculty, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof FacultyChooseProcessCallback) {
            mCallback = (FacultyChooseProcessCallback) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement FacultyChooseProcessCallback");
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

        Faculty chosen = mAdapter.getItem(mBinding.coursesSpinner.getSelectedItemPosition());
        getViewModel().setFacultyChosen(chosen);
    }

    private LoginViewModel getViewModel() {
        return ((LoginActivity) getActivity()).getViewModel();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        /* Save default faculty selection */
        Faculty base = getViewModel().getFaculties().get(0);
        getViewModel().setFacultyChosen(base);

        /* Show the faculties list */
        mAdapter = new FacultiesArrayAdapter(getActivity(), getViewModel().getFaculties());
        mBinding.coursesSpinner.setAdapter(mAdapter);
        mBinding.coursesSpinner.setOnItemSelectedListener(this);

        Faculty chosen = getViewModel().getFacultyChosen();
        int position = 0;
        if (chosen != null)
            position = mAdapter.getPosition(chosen);
        mBinding.coursesSpinner.setSelection(position);

        /* When the user has chosen the faculty */
        mBinding.dialogButtonOK.setOnClickListener(v -> {
            mCallback.onFacultyChosen();
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        /* Save user selected faculty */
        Faculty faculty = mAdapter.getItem(position);
        getViewModel().setFacultyChosen(faculty);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

}
