package com.communikein.myunimib.data;

import android.net.Uri;
import android.provider.BaseColumns;


public class ExamContract {

    public static final String CONTENT_AUTHORITY = "com.communikein.myunimib";

    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    private static final String PATH_EXAMS = "exams";
    public static final String PATH_EXAMS_AVAILABLE = PATH_EXAMS + "/available";
    public static final String PATH_EXAMS_ENROLLED = PATH_EXAMS + "/enrolled";

    public static final String PATH_BOOKLET = "booklet";
    public static final String PATH_BOOKLET_COURSES = PATH_BOOKLET + "/courses";

    public static class ExamEntry implements BaseColumns {
        public static final String COLUMN_COURSE_NAME = "course_name";
        public static final String COLUMN_DATE = "date_exam";
        public static final String COLUMN_DESCRIPTION = "description";

        public static final String COLUMN_ADSCE_ID = "ADSCE_ID".toLowerCase();
        public static final String COLUMN_CDS_ESA_ID = "CDS_ESA_ID".toLowerCase();
        public static final String COLUMN_ATT_DID_ESA_ID = "ATT_DID_ESA_ID".toLowerCase();
        public static final String COLUMN_APP_ID = "APP_ID".toLowerCase();
    }

    public static final class AvailableExamEntry extends ExamEntry {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendEncodedPath(PATH_EXAMS_AVAILABLE)
                .build();

        public static final String TABLE_NAME = "exam_available";

        public static final String COLUMN_BEGIN_ENROLLMENT = "begin_enrollment";
        public static final String COLUMN_END_ENROLLMENT = "end_enrollment";

        /**
         * Builds a URI that adds the booklet entry ID to the end of the booklet content URI path.
         * This is used to query details about a single booklet entry by ID. This is what we
         * use for the detail view query.
         *
         * @param adsce_id ID of the booklet entry
         * @return Uri to query details about a single booklet entry
         */
        public static Uri buildExamUriWithId(int adsce_id) {
            return CONTENT_URI.buildUpon()
                    .appendPath(Integer.toString(adsce_id))
                    .build();
        }
    }

    public static final class EnrolledExamEntry extends ExamEntry {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendEncodedPath(PATH_EXAMS_ENROLLED)
                .build();

        public static final String TABLE_NAME = "exam_enrolled";

        public static final String COLUMN_CODE = "code";
        public static final String COLUMN_BUILDING = "building";
        public static final String COLUMN_ROOM = "room";
        public static final String COLUMN_RESERVED = "reserved";
        public static final String COLUMN_TEACHERS = "teachers";
        public static final String COLUMN_CERTIFICATE_FILE_NAME = "certificate_file_name";

        /**
         * Builds a URI that adds the booklet entry ID to the end of the booklet content URI path.
         * This is used to query details about a single booklet entry by ID. This is what we
         * use for the detail view query.
         *
         * @param adsce_id ID of the booklet entry
         * @return Uri to query details about a single booklet entry
         */
        public static Uri buildExamUriWithId(int adsce_id) {
            return CONTENT_URI.buildUpon()
                    .appendPath(Integer.toString(adsce_id))
                    .build();
        }
    }

    public static final class BookletEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_BOOKLET)
                .build();

        public static final String TABLE_NAME = "booklet";

        public static final String COLUMN_COURSE_NAME = "course_name";
        public static final String COLUMN_DATE = "date_exam";
        public static final String COLUMN_ADSCE_ID = "adsce_id";
        public static final String COLUMN_CODE = "code";
        public static final String COLUMN_MARK = "mark";
        public static final String COLUMN_STATE = "state";
        public static final String COLUMN_CFU = "cfu";

        /**
         * Builds a URI that adds the booklet entry ID to the end of the booklet content URI path.
         * This is used to query details about a single booklet entry by ID. This is what we
         * use for the detail view query.
         *
         * @param adsce_id ID of the booklet entry
         * @return Uri to query details about a single booklet entry
         */
        public static Uri buildBookletUriWithId(int adsce_id) {
            return CONTENT_URI.buildUpon()
                    .appendPath(Integer.toString(adsce_id))
                    .build();
        }
    }

}
