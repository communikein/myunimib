package it.communikein.myunimib.ui.login;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import it.communikein.myunimib.R;
import it.communikein.myunimib.data.model.Faculty;

public class FacultiesArrayAdapter extends ArrayAdapter<Faculty> {

    @LayoutRes
    private static final int layoutResource = R.layout.simple_spinner_item;


    FacultiesArrayAdapter(@NonNull Context context, ArrayList<Faculty> list) {
        super(context, layoutResource, list);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final Faculty faculty = getItem(position);

        if(convertView == null)
            convertView = LayoutInflater.from(getContext()).inflate(layoutResource, parent, false);

        TextView facultyNameTextView = convertView.findViewById(R.id.textView);
        facultyNameTextView.setText(faculty.getName());

        return convertView;
    }

    @Override
    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
        final Faculty faculty = getItem(position);

        if(convertView == null)
            convertView = LayoutInflater.from(getContext()).inflate(layoutResource, parent, false);

        TextView facultyNameTextView = convertView.findViewById(R.id.textView);
        facultyNameTextView.setText(faculty.getName());

        return convertView;
    }

}
