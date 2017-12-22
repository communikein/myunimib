package it.communikein.myunimib.utilities;

import android.content.Context;

import it.communikein.myunimib.AppExecutors;
import it.communikein.myunimib.data.UnimibRepository;
import it.communikein.myunimib.data.database.UnimibDatabase;
import it.communikein.myunimib.data.network.UnimibNetworkDataSource;
import it.communikein.myunimib.ui.availableexam.AvailableExamsViewModelFactory;
import it.communikein.myunimib.ui.booklet.BookletViewModelFactory;
import it.communikein.myunimib.ui.enrolledexam.EnrolledExamsViewModelFactory;

public class InjectorUtils {

    private static UnimibRepository provideRepository(Context context) {
        UnimibDatabase database = UnimibDatabase
                .getInstance(context.getApplicationContext());
        AppExecutors executors = AppExecutors.getInstance();
        UnimibNetworkDataSource networkDataSource = UnimibNetworkDataSource
                .getInstance(context.getApplicationContext(), executors);

        return UnimibRepository.getInstance(database.unimibDao(), networkDataSource, executors);
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

}
