package it.communikein.myunimib.ui.graduation.projection;

import android.app.Dialog;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.List;

import it.communikein.myunimib.R;
import it.communikein.myunimib.data.model.BookletEntry;
import it.communikein.myunimib.databinding.DialogAddProjectionBinding;

import static android.view.Window.FEATURE_NO_TITLE;

public class AddProjectionDialog extends DialogFragment {

    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    public interface NoticeDialogListener {
        void onAddClick(BookletEntry entry);
        void onCancelClick();
    }

    // Use this instance of the interface to deliver action events
    NoticeDialogListener mListener;

    private DialogAddProjectionBinding mBinding;

    private List<String> coursesNames;
    private ArrayAdapter<String> adapterCourses;

    public void setCoursesNames(List<String> courses_names) {
        this.coursesNames = courses_names;
    }

    public AddProjectionDialog setActionListener(NoticeDialogListener listener) {
        this.mListener = listener;

        return this;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        mBinding = DataBindingUtil.inflate(inflater, R.layout.dialog_add_projection, container, false);

        return mBinding.getRoot();
    }

    @Override
    public void onResume() {
        // Get existing layout params for the window
        ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();

        // Assign window properties to fill the parent
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);

        // Call super onResume after sizing
        super.onResume();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getDialog().requestWindowFeature(FEATURE_NO_TITLE);

        mBinding.addProjectionButton.setOnClickListener(v -> {
            createExam();
            getDialog().dismiss();
        });
        mBinding.cancelButton.setOnClickListener(v -> {
            mListener.onCancelClick();
            getDialog().dismiss();
        });

        initCoursesAutoComplete(view.getContext());
    }

    private void createExam() {
        String name = mBinding.courseNameText.getText().toString();
        String score = mBinding.examScoreText.getText().toString();
        String cfu_str = mBinding.examCfuText.getText().toString();
        int cfu = Integer.parseInt(cfu_str);

        BookletEntry entry = new BookletEntry(name, cfu, score);
        mListener.onAddClick(entry);
    }

    private void initCoursesAutoComplete(Context context) {
        adapterCourses = new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line,
                new ArrayList<String>());
        mBinding.courseNameText.setAdapter(adapterCourses);
        mBinding.courseNameText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // update the adapter
                adapterCourses.clear();
                adapterCourses.addAll(getCoursesNames(s.toString()));
                adapterCourses.notifyDataSetChanged();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    public ArrayList<String> getCoursesNames(String like) {
        ArrayList<String> buildings = new ArrayList<>();

        if (coursesNames != null) for (String name : coursesNames)
            if (name.contains(like.toUpperCase()))
                buildings.add(name);

        return buildings;
    }
}