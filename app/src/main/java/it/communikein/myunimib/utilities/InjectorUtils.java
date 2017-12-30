package it.communikein.myunimib.utilities;

import android.content.Context;

import it.communikein.myunimib.AppExecutors;
import it.communikein.myunimib.data.UnimibRepository;
import it.communikein.myunimib.data.database.ExamID;
import it.communikein.myunimib.data.database.UnimibDatabase;
import it.communikein.myunimib.data.network.UnimibNetworkDataSource;
import it.communikein.myunimib.ui.detail.availableexam.AvailableExamViewModelFactory;
import it.communikein.myunimib.ui.detail.enrolledexam.EnrolledExamViewModelFactory;
import it.communikein.myunimib.ui.list.availableexam.AvailableExamsViewModelFactory;
import it.communikein.myunimib.ui.list.booklet.BookletViewModelFactory;
import it.communikein.myunimib.ui.list.enrolledexam.EnrolledExamsViewModelFactory;

public class InjectorUtils {

    public static UnimibRepository provideRepository(Context context) {
        UnimibDatabase database = UnimibDatabase
                .getInstance(context.getApplicationContext());
        AppExecutors executors = AppExecutors.getInstance();
        UnimibNetworkDataSource networkDataSource = UnimibNetworkDataSource
                .getInstance(context.getApplicationContext(), executors);
        NotificationHelper notificationHelper = NotificationHelper
                .getInstance(context.getApplicationContext());

        return UnimibRepository.getInstance(database.unimibDao(), networkDataSource, executors, notificationHelper);
    }

    public static UnimibNetworkDataSource provideNetworkDataSource(Context context) {
        provideRepository(context.getApplicationContext());
        AppExecutors executors = AppExecutors.getInstance();

        return UnimibNetworkDataSource.getInstance(context.getApplicationContext(), executors);
    }

    public static BookletViewModelFactory provideBookletViewModelFactory(Context context) {
        UnimibRepository repository = provideRepository(context.getApplicationContext());
        return new BookletViewModelFactory(repository);
    }

    public static EnrolledExamsViewModelFactory provideEnrolledExamsViewModelFactory(Context context) {
        UnimibRepository repository = provideRepository(context.getApplicationContext());
        return new EnrolledExamsViewModelFactory(repository);
    }

    public static AvailableExamsViewModelFactory provideAvailableExamsViewModelFactory(Context context) {
        UnimibRepository repository = provideRepository(context.getApplicationContext());
        return new AvailableExamsViewModelFactory(repository);
    }

    public static EnrolledExamViewModelFactory provideEnrolledExamViewModelFactory(Context context, ExamID examID) {
        UnimibRepository repository = provideRepository(context.getApplicationContext());
        return new EnrolledExamViewModelFactory(repository, examID);
    }

    public static AvailableExamViewModelFactory provideAvailableExamViewModelFactory(Context context, ExamID examID) {
        UnimibRepository repository = provideRepository(context.getApplicationContext());
        return new AvailableExamViewModelFactory(repository, examID);
    }
}
