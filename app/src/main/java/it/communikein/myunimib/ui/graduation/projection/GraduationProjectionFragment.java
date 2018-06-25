package it.communikein.myunimib.ui.graduation.projection;


import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.android.support.AndroidSupportInjection;
import it.communikein.myunimib.R;
import it.communikein.myunimib.data.model.BookletEntry;
import it.communikein.myunimib.data.model.User;
import it.communikein.myunimib.databinding.FragmentGraduationProjectionBinding;
import it.communikein.myunimib.ui.MainActivity;
import it.communikein.myunimib.ui.RecyclerItemTouchHelper;
import it.communikein.myunimib.ui.exam.booklet.BookletFragment;
import it.communikein.myunimib.utilities.Utils;
import it.communikein.myunimib.viewmodel.GraduationProjectionViewModel;
import it.communikein.myunimib.viewmodel.MainActivityViewModel;
import it.communikein.myunimib.viewmodel.factory.GraduationProjectionViewModelFactory;

/**
 * A simple {@link Fragment} subclass.
 */
public class GraduationProjectionFragment extends Fragment implements
        AddProjectionDialog.NoticeDialogListener,
        RecyclerItemTouchHelper.RecyclerItemTouchHelperListener,
        GraduationProjectionListAdapter.ListItemViewHolder.OnItemActionListener{


    private static final String LOG_TAG = BookletFragment.class.getSimpleName();

    /*  */
    private FragmentGraduationProjectionBinding mBinding;

    /* */
    @Inject
    GraduationProjectionViewModelFactory viewModelFactory;

    /* */
    private GraduationProjectionViewModel mViewModel;

    public interface AddProjectionListener {
        void onProjectionAddComplete();
    }

    public interface DeleteProjectionListener {
        void onProjectionDeleteComplete();
    }

    /* Required empty public constructor */
    public GraduationProjectionFragment() { }


    @Override
    public void onAttach(Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
    }

    private MainActivityViewModel getViewModel() {
        return ((MainActivity) getActivity()).getViewModel();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        /* Inflate the layout for this fragment */
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_graduation_projection, container, false);

        /*
         * A LinearLayoutManager is responsible for measuring and positioning item views within a
         * RecyclerView into a linear list. This means that it can produce either a horizontal or
         * vertical list.
         *
         * The third parameter (reverseLayout) should be true if you want to reverse your
         * layout. Generally, this is only true with horizontal lists that need to support a
         * right-to-left layout.
         */
        LinearLayoutManager layoutManager = new LinearLayoutManager(
                getActivity(),
                LinearLayoutManager.VERTICAL,
                false);
        mBinding.listRecyclerview.setLayoutManager(layoutManager);

        /*
         * Use this setting to improve performance if you know that changes in content do not
         * change the child layout size in the RecyclerView
         */
        mBinding.listRecyclerview.setHasFixedSize(true);

        ItemTouchHelper.SimpleCallback itemTouchHelperCallback =
                new RecyclerItemTouchHelper(0, ItemTouchHelper.LEFT, this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(mBinding.listRecyclerview);

        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setTitle();
        hideBottomNavigation();
        hideTabs();

        mViewModel = ViewModelProviders
                .of(this, viewModelFactory)
                .get(GraduationProjectionViewModel.class);

        /*
         * Ensures a loader is initialized and active and shows the loading view.
         * If the loader doesn't already exist, one is created and (if the activity/fragment is
         * currently started) starts the loader. Otherwise the last created loader is re-used.
         */
        if (getActivity() != null) {

            final GraduationProjectionListAdapter mAdapter =
                    new GraduationProjectionListAdapter(null);

            mViewModel.getExams().observe(this, list -> {
                if (list != null) {
                    mAdapter.setList((ArrayList<BookletEntry>) list);

                    updateProjection(list);
                }
            });

            /* Setting the adapter attaches it to the RecyclerView in our layout. */
            mBinding.listRecyclerview.setAdapter(mAdapter);

            initFab();
            updateProjection(new ArrayList<>());
        }
    }

    private void initFab() {
        mBinding.fab.setOnClickListener(v -> {
            final AddProjectionDialog dialog = new AddProjectionDialog()
                    .setActionListener(this);

            mViewModel.getCoursesNames().observe(this, dialog::setCoursesNames);

            dialog.show(getChildFragmentManager(), AddProjectionDialog.class.getSimpleName());
        });
    }

    private void updateProjection(List<BookletEntry> exams) {
        mViewModel.getUser().observe(this, (user) -> {
            if (user != null) {
                double result = user.getAverageMark() * user.getTotalCfu();

                int fakeCFU = 0;
                for (BookletEntry exam : exams) {
                    if (exam.getScoreValue() != 0)
                        fakeCFU += exam.getCfu();

                    result += exam.getScoreValue() * exam.getCfu();
                }
                result = result / (fakeCFU + user.getTotalCfu()) * 110 / 30;

                mBinding.futureProjectionTextview.setText(Utils.markFormat.format(result));
            }
        });
    }

    /**
     * Change the Activity's ActionBar title.
     */
    private void setTitle() {
        if (getActivity() != null) {
            /* Get a reference to the MainActivity ActionBar */
            ActionBar actionBar = ((MainActivity) getActivity()).getSupportActionBar();
            /* If there is an ActionBar, set it's title */
            if (actionBar != null)
                actionBar.setTitle(R.string.title_graduation_score_prevision);
        }
    }

    private void hideBottomNavigation() {
        if (getActivity() != null) {
            ((MainActivity) getActivity()).hideBottomNavigation();
        }
    }

    private void hideTabs() {
        if (getActivity() != null) {
            ((MainActivity) getActivity()).hideTabsLayout();
        }
    }

    @Override
    public void onAddClick(BookletEntry entry) {
        mViewModel.addExamProjection(entry, () -> {
            Snackbar.make(mBinding.coordinatorLayout,
                    R.string.label_projection_added, Snackbar.LENGTH_LONG).show();
        });
    }

    @Override
    public void onCancelClick() {
        Snackbar.make(mBinding.coordinatorLayout,
                R.string.action_discarded, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (viewHolder instanceof GraduationProjectionListAdapter.ListItemViewHolder) {
            onItemDelete(
                    ((GraduationProjectionListAdapter.ListItemViewHolder) viewHolder).mBinding.getBookletEntry(),
                    position);
        }
    }

    @Override
    public boolean onItemEdit(BookletEntry entry, int position) {
        return false;
    }

    @Override
    public boolean onItemDelete(BookletEntry entry, int position) {
        GraduationProjectionListAdapter adapter =
                (GraduationProjectionListAdapter) mBinding.listRecyclerview.getAdapter();

        // get the removed item name to display it in snack bar
        String name = entry.getName();

        // remove the item from recycler view
        adapter.removeItem(position);
        mViewModel.deleteExamProjection(entry, () -> {
            // showing snack bar with Undo option
            Snackbar snackbar = Snackbar.make(mBinding.coordinatorLayout,
                    getString(R.string.label_item_removed, name), Snackbar.LENGTH_LONG);
            snackbar.setAction(R.string.action_undo, view -> {
                // undo is selected, restore the deleted item
                mViewModel.restoreExamProjection(entry,
                        () -> adapter.restoreItem(entry, position));
            });
            snackbar.setActionTextColor(Color.YELLOW);
            snackbar.show();
        });

        return true;
    }
}
