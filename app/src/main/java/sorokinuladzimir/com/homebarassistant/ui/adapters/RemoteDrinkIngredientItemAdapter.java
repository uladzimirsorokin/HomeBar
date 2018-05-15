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


public class RemoteDrinkIngredientItemAdapter extends RecyclerView.Adapter<RemoteDrinkIngredientItemAdapter.IngredientViewHolder> {

    private final OnItemClickListener listener;
    private ArrayList<WholeCocktail> mData = new ArrayList();

    public RemoteDrinkIngredientItemAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public IngredientViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new IngredientViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.single_drink_ingredient_item, parent, false));
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

    public interface OnItemClickListener {
        void onItemClick(WholeCocktail item);
    }

    public static class IngredientViewHolder extends RecyclerView.ViewHolder {

        final TextView ingredientName;
        final TextView ingredientUnit;
        final TextView ingredientAmount;
        final ImageView ingredientImage;

        IngredientViewHolder(View itemView) {
            super(itemView);
            ingredientName = itemView.findViewById(R.id.tv_singledrink_ingredient);
            ingredientUnit = itemView.findViewById(R.id.tv_singledrink_unit);
            ingredientAmount = itemView.findViewById(R.id.tv_singledrink_amount);
            ingredientImage = itemView.findViewById(R.id.image_singledrink_ingredient_item);
        }

        public void bind(final WholeCocktail item, final OnItemClickListener listener) {

            if (item.getIngredientName() != null) {
                ingredientName.setText(item.getIngredientName());
            } else {
                ingredientName.setText(R.string.noname_drink);
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

            if (item.getImage() != null) {
                Glide.with(ingredientImage.getContext())
                        .load(item.getImage())
                        .apply(RequestOptions.circleCropTransform())
                        .into(ingredientImage);
            }

            itemView.setOnClickListener(v -> listener.onItemClick(item));
        }
    }
}
