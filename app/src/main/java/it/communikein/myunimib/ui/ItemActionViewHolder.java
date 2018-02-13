package it.communikein.myunimib.ui;

import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import it.communikein.myunimib.R;

public abstract class ItemActionViewHolder extends RecyclerView.ViewHolder implements
        ForegroundBackgroundView, View.OnCreateContextMenuListener,
        MenuItem.OnMenuItemClickListener {

    public ItemActionViewHolder(View view) {
        super(view);

        view.setOnCreateContextMenuListener(this);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        MenuItem showAction = menu.add(Menu.NONE, R.id.action_show, 0, R.string.action_show);
        MenuItem editAction = menu.add(Menu.NONE, R.id.action_edit, 0, R.string.action_edit);
        MenuItem deleteAction = menu.add(Menu.NONE, R.id.action_delete, 0, R.string.action_delete);

        showAction.setOnMenuItemClickListener(this);
        editAction.setOnMenuItemClickListener(this);
        deleteAction.setOnMenuItemClickListener(this);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_show:
                return onItemShow();
            case R.id.action_edit:
                return onItemEdit();
            case R.id.action_delete:
                return onItemDelete();
        }

        return false;
    }

    public abstract boolean onItemShow();

    public abstract boolean onItemDelete();

    public abstract boolean onItemEdit();
}
