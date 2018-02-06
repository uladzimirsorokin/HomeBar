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
                .inflate(R.layout.single_drink_ingredient_item,parent,false));
    }

    @Override
    public void onBindViewHolder(IngredientViewHolder holder, int position) {
        holder.bind(mData.get(position),listener);
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

    public static class IngredientViewHolder extends RecyclerView.ViewHolder{

        final TextView ingredientName;
        final ImageView ingredientImage;

        public IngredientViewHolder(View itemView) {
            super(itemView);
            ingredientName = itemView.findViewById(R.id.tv_singledrink_ingredient);
            ingredientImage = itemView.findViewById(R.id.image_singledrink_ingredient_item);
        }

        public void bind(final WholeCocktail item, final OnItemClickListener listener) {

            if(item.ingredientName != null)ingredientName.setText(item.ingredientName);

            if(item.drinkName != null){
                Glide.with(ingredientImage.getContext())
                        .load("data:image/jpeg;base64,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")
                        .apply(RequestOptions.circleCropTransform())
                        .into(ingredientImage);
            }

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(item);
                }
            });
        }
    }
}
