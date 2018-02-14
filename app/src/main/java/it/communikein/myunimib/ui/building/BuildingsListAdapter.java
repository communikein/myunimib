package it.communikein.myunimib.ui.building;

import android.databinding.DataBindingUtil;
import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import it.communikein.myunimib.R;
import it.communikein.myunimib.data.model.Building;
import it.communikein.myunimib.databinding.ListItemBuildingBinding;
import it.communikein.myunimib.ui.building.BuildingsListAdapter.BuildingViewHolder;

public class BuildingsListAdapter extends RecyclerView.Adapter<BuildingViewHolder> {

    private ArrayList<Building> mList;

    @Nullable
    private final OnListItemClickListener mOnClickListener;
    public interface OnListItemClickListener {
        void onListPoiClick(Building poi);
    }

    BuildingsListAdapter(@Nullable OnListItemClickListener onListItemClickListener) {
        mOnClickListener = onListItemClickListener;
    }

    @Override
    public BuildingViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ListItemBuildingBinding mBinding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.getContext()), R.layout.list_item_building, parent, false);

        return new BuildingViewHolder(mBinding);
    }

    @Override
    public void onBindViewHolder(BuildingViewHolder holder, int position) {
        Building building = mList.get(position);

        holder.mBinding.setBuilding(building);
        holder.mBinding.nameTextview.setText(building.getName());
        holder.mBinding.addressTextview.setText(building.getAddress());
    }

    @Override
    public int getItemCount() {
        return mList == null ? 0 : mList.size();
    }


    public void setList(final List<Building> newList) {
        ArrayList<Building> tempList = new ArrayList<>(newList);

        if (mList == null) {
            mList = tempList;
            notifyItemRangeInserted(0, mList.size());
        }
        else {
            DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
                @Override
                public int getOldListSize() {
                    return mList.size();
                }

                @Override
                public int getNewListSize() {
                    return tempList.size();
                }

                @Override
                public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                    return mList.get(oldItemPosition).equals(tempList.get(newItemPosition));
                }

                @Override
                public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                    Building newItem = tempList.get(newItemPosition);
                    Building oldItem = mList.get(oldItemPosition);
                    return oldItem.displayEquals(newItem);
                }
            });
            mList = tempList;
            result.dispatchUpdatesTo(this);
        }
    }


    class BuildingViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        final ListItemBuildingBinding mBinding;

        BuildingViewHolder(ListItemBuildingBinding binding) {
            super(binding.getRoot());

            binding.getRoot().setOnClickListener(this);

            this.mBinding = binding;
        }

        @Override
        public void onClick(View v) {
            if (mOnClickListener != null) {
                mOnClickListener.onListPoiClick(mBinding.getBuilding());
            }
        }
    }
}

