package it.communikein.myunimib.data.database;

import android.arch.persistence.room.TypeConverter;
import android.util.SparseArray;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;

/**
 * {@link TypeConverter} for long to {@link Date}
 * <p>
 * This stores the date as a long in the database, but returns it as a {@link Date}
 */
class Converters {

    private static final String KEYS = "keys";
    private static final String VALUES = "values";

    @TypeConverter
    public static Date toDate(Long timestamp) {
        return timestamp == null ? null : new Date(timestamp);
    }

    @TypeConverter
    public static Long toTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }



    @TypeConverter
    public static ArrayList<String> fromStringToArrayList(String value) {
        try {
            JSONArray array = new JSONArray(value);
            ArrayList<String> result = new ArrayList<>();

            for (int i=0; i<array.length(); i++)
                result.add(array.getString(i));

            return result;
        } catch (JSONException e){
            return null;
        }
    }

    @TypeConverter
    public static String fromArrayList(ArrayList<String> list) {
        JSONArray json = new JSONArray();

        for (String item : list)
            json.put(item);

        return json.toString();
    }


    @TypeConverter
    public static SparseArray<String> fromStringToSparseArray(String value) {
        ArrayList<Integer> keys = new ArrayList<>();
        ArrayList<String> vals = new ArrayList<>();

        try {
            JSONObject obj = new JSONObject(value);

            if (obj.has(KEYS)) {
                JSONArray keys_array = obj.getJSONArray(KEYS);
                for (int i=0; i<keys_array.length(); i++)
                    keys.add(keys_array.getInt(i));
            }
            if (obj.has(VALUES)) {
                JSONArray vals_array = obj.getJSONArray(VALUES);
                for (int i=0; i<vals_array.length(); i++)
                    vals.add(vals_array.getString(i));
            }

            SparseArray<String> result = new SparseArray<>();
            for (int i=0; i<keys.size(); i++)
                result.put(keys.get(i), vals.get(i));

            return result;

        } catch (JSONException e) {
            return null;
        }
    }

    @TypeConverter
    public static String fromSparseArray(SparseArray<String> list) {
        JSONObject obj = new JSONObject();

        JSONArray keys_array = new JSONArray();
        JSONArray vals_array = new JSONArray();

        for (int i=0; i<list.size(); i++) {
            keys_array.put(list.keyAt(i));
            vals_array.put(list.valueAt(i));
        }

        try {
            obj.put(KEYS, keys_array);
            obj.put(VALUES, vals_array);
        } catch (JSONException e) {
            obj = new JSONObject();
        }

        return obj.toString();
    }

}