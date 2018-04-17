package sorokinuladzimir.com.homebarassistant.ui.adapters;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.turingtechnologies.materialscrollbar.INameableAdapter;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Objects;

import sorokinuladzimir.com.homebarassistant.BarApp;
import sorokinuladzimir.com.homebarassistant.R;
import sorokinuladzimir.com.homebarassistant.db.entity.Drink;
import sorokinuladzimir.com.homebarassistant.db.entity.Taste;
import sorokinuladzimir.com.homebarassistant.ui.utils.TastesHelper;


public class LocalDrinksListAdapter extends RecyclerView.Adapter<LocalDrinksListAdapter.CardViewHolder> implements INameableAdapter {

    public LocalDrinksListAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public Character getCharacterForElement(int element) {
        Character c = mDrinkList.get(element).getName().equals("") ? Character.MIN_VALUE : mDrinkList.get(element).getName().charAt(0);
        if(Character.isDigit(c)) {
            c = '#';
        }
        return c;
    }

    public interface OnItemClickListener {
        void onItemClick(Drink item);
    }

    private List<Drink> mDrinkList;
    private final OnItemClickListener listener;

    private Deque<List<Drink>> pendingUpdates = new ArrayDeque<>();

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CardViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_view_cocktail_item,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        holder.bind(mDrinkList.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return mDrinkList != null ? mDrinkList.size() : 0;
    }

    @SuppressLint("StaticFieldLeak")
    public void setDrinks(final List<Drink> drinks) {
        pendingUpdates.push(drinks);
        if (pendingUpdates.size() > 1) {
            return;
        }
        if (mDrinkList == null) {
            pendingUpdates.remove(drinks);
            mDrinkList = drinks;
            notifyItemRangeInserted(0, mDrinkList.size()-1);
            if (pendingUpdates.size() > 0) {
                List<Drink> latest = pendingUpdates.pop();
                pendingUpdates.clear();
                setDrinks(latest);
            }
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
                                return Objects.equals(mDrinkList.get(oldItemPosition).getId(), drinks.get(newItemPosition).getId());
                            }

                            @Override
                            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                                return mDrinkList.get(oldItemPosition).equals(drinks.get(newItemPosition));
                            }
                        });

                        BarApp.getInstance().getExecutors().mainThread().execute(() -> {
                            pendingUpdates.remove(drinks);
                            mDrinkList = drinks;
                            diffResult.dispatchUpdatesTo(LocalDrinksListAdapter.this);
                            if (pendingUpdates.size() > 0) {
                                List<Drink> latest = pendingUpdates.pop();
                                pendingUpdates.clear();
                                setDrinks(latest);
                            }
                        });
                    }
            );
        }
    }

    static class CardViewHolder extends RecyclerView.ViewHolder{

        final ImageView cardImage;
        final TextView title;
        final TextView subtitle;
        final RatingBar rating;

        CardViewHolder(View itemView) {
            super(itemView);

            cardImage = itemView.findViewById(R.id.card_image);
            title = itemView.findViewById(R.id.card_title);
            subtitle = itemView.findViewById(R.id.card_subtitle);
            rating = itemView.findViewById(R.id.card_rating_bar);
        }

        void bind(final Drink drinkItem, final OnItemClickListener listener) {

            Glide.with(cardImage.getContext())
                    .load(drinkItem.getImage() != null ? drinkItem.getImage() : R.drawable.camera_placeholder)
                    .apply(RequestOptions.centerCropTransform())
                    .into(cardImage);

            title.setText(drinkItem.getName());

            ArrayList<Taste> tastes = drinkItem.getTastes();

            subtitle.setText(TastesHelper.tastesToString(tastes));

            rating.setProgress(drinkItem.getRating() / 10);

            itemView.setOnClickListener(v -> listener.onItemClick(drinkItem));
        }



    }
}
