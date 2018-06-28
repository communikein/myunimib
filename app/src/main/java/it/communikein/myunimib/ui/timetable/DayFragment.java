package it.communikein.myunimib.ui.timetable;


import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import it.communikein.myunimib.R;
import it.communikein.myunimib.data.model.Lesson;
import it.communikein.myunimib.databinding.FragmentDayBinding;
import it.communikein.myunimib.ui.RecyclerItemTouchHelper;
import it.communikein.myunimib.utilities.DAY_OF_WEEK;
import it.communikein.myunimib.viewmodel.MainActivityViewModel;

/**
 * A simple {@link Fragment} subclass.
 */
public class DayFragment extends Fragment implements
        LessonsListAdapter.ListItemViewHolder.OnItemActionListener,
        RecyclerItemTouchHelper.RecyclerItemTouchHelperListener {

    private static final String LOG_TAG = DayFragment.class.getSimpleName();

    private FragmentDayBinding mBinding;

    private DAY_OF_WEEK day_of_week;

    public interface DeleteLessonListener {
        void onDeleteLessonComplete();
    }

    private void setDay(DAY_OF_WEEK day_of_week) {
        this.day_of_week = day_of_week;
    }

    public DAY_OF_WEEK getDay() {
        return day_of_week;
    }

    public static DayFragment create(DAY_OF_WEEK day_of_week) {
        DayFragment dayFragment = new DayFragment();
        dayFragment.setDay(day_of_week);

        return dayFragment;
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_day, container, false);

        initList();

        return mBinding.getRoot();
    }

    private void initList() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(
                getActivity(),
                LinearLayoutManager.VERTICAL,
                false);
        mBinding.listRecyclerview.setLayoutManager(layoutManager);

        /*
         * Improve performance if sure that changes in content do not
         * change the child layout size in the RecyclerView
         */
        mBinding.listRecyclerview.setHasFixedSize(true);

        ItemTouchHelper.SimpleCallback itemTouchHelperCallback =
                new RecyclerItemTouchHelper(0, ItemTouchHelper.LEFT, this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(mBinding.listRecyclerview);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        /*
         * Ensures a loader is initialized and active and shows the loading view.
         * If the loader doesn't already exist, one is created and (if the activity/fragment is
         * currently started) starts the loader. Otherwise the last created loader is re-used.
         */
        if (getViewModel() != null) {
            LessonsListAdapter mAdapter = new LessonsListAdapter(this);

            getViewModel().getTimetable(day_of_week.getDay()).observe(this, list -> {
                if (list != null) {
                    mAdapter.setList((ArrayList<Lesson>) list);
                }
            });

            /* Setting the adapter attaches it to the RecyclerView in our layout. */
            mBinding.listRecyclerview.setAdapter(mAdapter);
        }
    }

    private MainActivityViewModel getViewModel() {
        if (getParentFragment() != null)
            return ((TimetableFragment) getParentFragment()).getViewModel();
        else
            return null;
    }

    private CoordinatorLayout getParentCoordinatorLayout() {
        if (getParentFragment() != null)
            return ((TimetableFragment) getParentFragment()).getCoordinatorLayout();
        else
            return null;
    }


    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (viewHolder instanceof LessonsListAdapter.ListItemViewHolder) {
            onItemDelete(
                    ((LessonsListAdapter.ListItemViewHolder) viewHolder).mBinding.getLesson(),
                    position);
        }
    }

    @Override
    public boolean onItemShow(Lesson lesson, int position) {

        return false;
    }

    @Override
    public boolean onItemEdit(Lesson lesson, int position) {
        if (getActivity() != null) {
            Intent intent = new Intent(getActivity(), AddLessonActivity.class);
            intent.putExtra(AddLessonActivity.LESSON_ID, lesson.getId());
            getActivity().startActivity(intent);

            return true;
        }
        return false;
    }

    @Override
    public boolean onItemDelete(final Lesson lesson, final int position) {
        if (getParentCoordinatorLayout()!= null) {
            LessonsListAdapter adapter = (LessonsListAdapter) mBinding.listRecyclerview.getAdapter();

            // get the removed item name to display it in snack bar
            String name = lesson.getCourseName();

            // remove the item from recycler view
            adapter.removeItem(position);
            if (getViewModel() != null) {
                getViewModel().deleteLesson(lesson, () -> {
                    // showing snack bar with Undo option
                    Snackbar snackbar = Snackbar.make(getParentCoordinatorLayout(),
                            getString(R.string.label_item_removed, name), Snackbar.LENGTH_LONG);
                    snackbar.setAction(R.string.action_undo, view -> {
                        // undo is selected, restore the deleted item
                        getViewModel().restoreLesson(lesson,
                                () -> adapter.restoreItem(lesson, position));
                    });
                    snackbar.setActionTextColor(Color.YELLOW);
                    snackbar.show();
                });
            }

            return true;
        }

        return false;
    }
}
