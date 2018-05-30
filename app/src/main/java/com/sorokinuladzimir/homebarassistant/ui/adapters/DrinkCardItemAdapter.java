package com.sorokinuladzimir.homebarassistant.ui.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.sorokinuladzimir.homebarassistant.Constants;
import com.sorokinuladzimir.homebarassistant.R;
import com.sorokinuladzimir.homebarassistant.net.entity.DrinkEntity;
import com.sorokinuladzimir.homebarassistant.ui.utils.TastesHelper;

import java.util.ArrayList;
import java.util.List;

public class DrinkCardItemAdapter extends RecyclerView.Adapter<DrinkCardItemAdapter.CardViewHolder> {

    private final ArrayList<DrinkEntity> mData = new ArrayList();
    private final OnItemClickListener listener;
    private final LoadMoreListener loadMoreListener;

    public DrinkCardItemAdapter(OnItemClickListener listener, LoadMoreListener loadMoreListener) {
        this.listener = listener;
        this.loadMoreListener = loadMoreListener;
    }

    @Override
    public CardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new CardViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_card_view_cocktail, parent, false));
    }

    @Override
    public void onBindViewHolder(CardViewHolder holder, int position) {
        holder.bind(mData.get(position), listener);
        if (position == getItemCount() - 1) loadMoreListener.loadMoreCocktails();
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public void setData(List<DrinkEntity> cocktails) {
        mData.clear();
        mData.addAll(cocktails);
        notifyDataSetChanged();
    }

    public interface OnItemClickListener {
        void onItemClick(DrinkEntity item);
    }

    public interface LoadMoreListener {
        void loadMoreCocktails();
    }

    public static class CardViewHolder extends RecyclerView.ViewHolder {

        final ImageView cardImage;
        final TextView title;
        final TextView subtitle;
        final TextView rating;
        final View carbonatedIcon;

        CardViewHolder(View itemView) {
            super(itemView);
            cardImage = itemView.findViewById(R.id.card_image);
            title = itemView.findViewById(R.id.card_title);
            subtitle = itemView.findViewById(R.id.card_subtitle);
            rating = itemView.findViewById(R.id.tv_rating);
            carbonatedIcon = itemView.findViewById(R.id.icon_carbonated);
        }

        void bind(final DrinkEntity drinkItem, final OnItemClickListener listener) {
            Glide.with(cardImage.getContext())
                    .load(Constants.Uri.ABSOLUT_DRINKS_IMAGE_ROOT + drinkItem.getId() + ".png")
                    .apply(RequestOptions.centerCropTransform())
                    .error(Glide.with(cardImage.getContext()).load(R.drawable.camera_placeholder))
                    .into(cardImage);
            title.setText(drinkItem.getName());
            subtitle.setText(TastesHelper.tastesToString(drinkItem.getTastes()));
            rating.setText(String.valueOf(drinkItem.getRating()));
            if (drinkItem.isCarbonated()) {
                carbonatedIcon.setVisibility(View.VISIBLE);
            } else {
                carbonatedIcon.setVisibility(View.GONE);
            }
            itemView.setOnClickListener(v -> listener.onItemClick(drinkItem));
        }
    }
}
