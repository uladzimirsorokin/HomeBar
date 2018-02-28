package sorokinuladzimir.com.homebarassistant.ui.adapters;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import sorokinuladzimir.com.homebarassistant.BarApp;
import sorokinuladzimir.com.homebarassistant.R;
import sorokinuladzimir.com.homebarassistant.db.entity.Drink;
import sorokinuladzimir.com.homebarassistant.db.entity.Taste;


/**
 * Created by 1 on 10/12/2016.
 */

public class LocalDrinksListAdapter extends RecyclerView.Adapter<LocalDrinksListAdapter.CardViewHolder> {

    public LocalDrinksListAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(Drink item);
    }

    private List<Drink> mDrinkList;
    private final OnItemClickListener listener;

    @Override
    public CardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new CardViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_view_cocktail_item,parent,false));
    }

    @Override
    public void onBindViewHolder(CardViewHolder holder, int position) {
        holder.bind(mDrinkList.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return mDrinkList != null ? mDrinkList.size() : 0;
    }

    @SuppressLint("StaticFieldLeak")
    public void setDrinks(final List<Drink> drinks) {
        if (mDrinkList == null) {
            mDrinkList = drinks;
            notifyItemRangeInserted(0, drinks.size());
        } else {
            BarApp.getInstance().getExecutors().diskIO().execute(
                    () -> {
                        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
                            @Override
                            public int getOldListSize() {
                                return mDrinkList.size();
                            }

                            @Override
                            public int getNewListSize() {
                                return drinks.size();
                            }

                            @Override
                            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                                return mDrinkList.get(oldItemPosition).id == drinks.get(newItemPosition).id;
                            }

                            @Override
                            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                                return mDrinkList.get(oldItemPosition).equals(drinks.get(newItemPosition));
                            }
                        });

                        BarApp.getInstance().getExecutors().mainThread().execute(() -> {
                            mDrinkList = drinks;
                            diffResult.dispatchUpdatesTo(LocalDrinksListAdapter.this);
                        });
                    }
            );
        }
    }

    public static class CardViewHolder extends RecyclerView.ViewHolder{

        final ImageView cardImage;
        final TextView title;
        final TextView subtitle;
        final RatingBar rating;

        public CardViewHolder(View itemView) {
            super(itemView);

            cardImage = itemView.findViewById(R.id.card_image);
            title = itemView.findViewById(R.id.card_title);
            subtitle = itemView.findViewById(R.id.card_subtitle);
            rating = itemView.findViewById(R.id.card_rating_bar);
        }

        public void bind(final Drink drinkItem, final OnItemClickListener listener) {

            Glide.with(cardImage.getContext())
                    .load(drinkItem.image)
                    .into(cardImage);

            title.setText(drinkItem.name);

            ArrayList<Taste> tastes = drinkItem.tastes;

            if(tastes != null) {
                String tastesStr = tastes.get(0).text;
                for (int i = 1; i < tastes.size(); i++){
                    tastesStr += ", " + tastes.get(i).text;
                }
                subtitle.setText(tastesStr);
            }

            rating.setProgress(90 / 10);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(drinkItem);
                }
            });
        }



    }
}
