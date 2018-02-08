package it.communikein.myunimib.ui.list.building;


import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import it.communikein.myunimib.R;
import it.communikein.myunimib.data.model.Building;
import it.communikein.myunimib.databinding.FragmentBuildingsListBinding;
import it.communikein.myunimib.viewmodel.BuildingsViewModel;

/**
 * A simple {@link Fragment} subclass.
 */
public class BuildingsListFragment extends Fragment implements BuildingsListAdapter.OnListItemClickListener {

    public static final String LOG_TAG = BuildingsListFragment.class.getSimpleName();

    private FragmentBuildingsListBinding mBinding;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_buildings_list, container, false);

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

        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        BuildingsListAdapter mAdapter = new BuildingsListAdapter(this);
        mBinding.listRecyclerview.setAdapter(mAdapter);

        if (getParentViewModel() != null)
            mAdapter.setList(getParentViewModel().getBuildings());
    }

    private BuildingsViewModel getParentViewModel() {
        if (getParentFragment() != null)
            return ((BuildingsFragment) getParentFragment()).getViewModel();
        else
            return null;
    }

    @Override
    public void onListPoiClick(Building building) {

    }

}
