package com.sorokinuladzimir.homebarassistant.ui.adapters;

import android.graphics.drawable.GradientDrawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sorokinuladzimir.homebarassistant.R;
import com.sorokinuladzimir.homebarassistant.ui.ThemeItem;

import java.util.ArrayList;
import java.util.List;


public class ThemeItemAdapter extends RecyclerView.Adapter<ThemeItemAdapter.ThemeItemVH> {

    private final ArrayList<ThemeItem> mData = new ArrayList();
    private final OnItemClickListener listener;

    public ThemeItemAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public ThemeItemVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ThemeItemVH(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_theme, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ThemeItemVH holder, int position) {
        holder.bind(mData.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public void setData(List<ThemeItem> themeList) {
        mData.clear();
        mData.addAll(themeList);
        notifyDataSetChanged();
    }

    public interface OnItemClickListener {
        void onItemClick(ThemeItem item);
    }

    public static class ThemeItemVH extends RecyclerView.ViewHolder {

        final ImageView image;
        final ImageView selectedIcon;
        final TextView name;


        ThemeItemVH(View itemView) {
            super(itemView);

            image = itemView.findViewById(R.id.color_circle);
            selectedIcon = itemView.findViewById(R.id.ic_done);
            name = itemView.findViewById(R.id.tv_name);
        }

        public void bind(final ThemeItem theme, final OnItemClickListener listener) {

            ((GradientDrawable) image.getBackground()).setColor(theme.getColor());
            selectedIcon.setVisibility(theme.isSelected() ? View.VISIBLE : View.INVISIBLE);
            name.setText(theme.getName());

            itemView.setOnClickListener(v -> listener.onItemClick(theme));
        }
    }
}
