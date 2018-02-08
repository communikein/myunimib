package it.communikein.myunimib.utilities;

import android.content.Context;
import android.text.TextUtils;

import it.communikein.myunimib.R;
import it.communikein.myunimib.data.model.EnrolledExam;

public class LocationHelper {

    public static String printLocation(EnrolledExam enrolledExam, Context context){
        StringBuilder ris = new StringBuilder()
                .append(context.getString(R.string.milan))
                .append(", ")
                .append(enrolledExam.getBuilding());

        if (TextUtils.isEmpty(enrolledExam.getBuilding()) && TextUtils.isEmpty(enrolledExam.getRoom()))
            return context.getString(R.string.error_exam_missing_location);

        if (!TextUtils.isEmpty(enrolledExam.getRoom())) {
            try {
                ris.append(enrolledExam.getRoom().substring(0, enrolledExam.getRoom().indexOf("-")));
            } catch (Exception e) {
                ris.append(enrolledExam.getRoom());
            }
        }

        return ris.toString();
    }

}
