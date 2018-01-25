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

import sorokinuladzimir.com.homebarassistant.R;
import sorokinuladzimir.com.homebarassistant.net.entity.IngredientEntity;


public class IngredientsListItemAdapter extends RecyclerView.Adapter<IngredientsListItemAdapter.IngredientViewHolder> {

    public IngredientsListItemAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(IngredientEntity item);
    }

    private ArrayList<IngredientEntity> mData = new ArrayList();
    private final OnItemClickListener listener;
    
    @Override
    public IngredientViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new IngredientViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.ingredients_list_single_ingredient_item,parent,false));
    }

    @Override
    public void onBindViewHolder(IngredientViewHolder holder, int position) {
        holder.bind(mData.get(position),listener);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public void setData(ArrayList<IngredientEntity> ingredients) {
        mData.clear();
        mData.addAll(ingredients);
        notifyDataSetChanged();
    }

    public static class IngredientViewHolder extends RecyclerView.ViewHolder{

        final TextView ingredientName;
        final ImageView ingredientImage;

        public IngredientViewHolder(View itemView) {
            super(itemView);
            ingredientName = itemView.findViewById(R.id.tv_ingredient_item);
            ingredientImage = itemView.findViewById(R.id.image_ingredient_item);
        }

        public void bind(final IngredientEntity item, final OnItemClickListener listener) {

            if(item.name != null)ingredientName.setText(item.name);

            if(item.url != null){
                Glide.with(ingredientImage.getContext())
                        .load(item.url)
                        .apply(RequestOptions.centerCropTransform())
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
