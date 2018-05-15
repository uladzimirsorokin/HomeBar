package sorokinuladzimir.com.homebarassistant.ui.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;

import sorokinuladzimir.com.homebarassistant.R;
import sorokinuladzimir.com.homebarassistant.db.entity.WholeCocktail;


public class LocalDrinkIngredientItemAdapter extends RecyclerView.Adapter<LocalDrinkIngredientItemAdapter.IngredientViewHolder> {

    public LocalDrinkIngredientItemAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(WholeCocktail item);
    }

    private List<WholeCocktail> mData = new ArrayList();
    private final OnItemClickListener listener;

    @Override
    public IngredientViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new IngredientViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_single_drink_ingredient, parent, false));
    }

    @Override
    public void onBindViewHolder(IngredientViewHolder holder, int position) {
        holder.bind(mData.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public void setData(List<WholeCocktail> ingredients) {
        mData.clear();
        mData.addAll(ingredients);
        notifyDataSetChanged();
    }

    public static class IngredientViewHolder extends RecyclerView.ViewHolder {

        final TextView ingredientName;
        final ImageView ingredientImage;
        final TextView ingredientAmount;
        final TextView ingredientUnit;

        public IngredientViewHolder(View itemView) {
            super(itemView);
            ingredientName = itemView.findViewById(R.id.tv_singledrink_ingredient);
            ingredientImage = itemView.findViewById(R.id.image_singledrink_ingredient_item);
            ingredientAmount = itemView.findViewById(R.id.tv_singledrink_amount);
            ingredientUnit = itemView.findViewById(R.id.tv_singledrink_unit);
        }

        public void bind(final WholeCocktail item, final OnItemClickListener listener) {

            if (item.getIngredientName() != null) {
                ingredientName.setText(item.getIngredientName());
            } else {
                ingredientName.setText("");
            }

            if (item.getAmount() != null) {
                ingredientAmount.setText(item.getAmount());
            } else {
                ingredientAmount.setText("");
            }

            if (item.getUnit() != null) {
                ingredientUnit.setText(item.getUnit());
            } else {
                ingredientUnit.setText("");
            }

            Glide.with(ingredientImage.getContext())
                    .load(item.getImage() != null ? item.getImage() : R.drawable.camera_placeholder)
                    .apply(RequestOptions.circleCropTransform())
                    .into(ingredientImage);

            itemView.setOnClickListener(v -> listener.onItemClick(item));
        }
    }
}
