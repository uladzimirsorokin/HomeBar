package sorokinuladzimir.com.homebarassistant.ui.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;

import sorokinuladzimir.com.homebarassistant.R;
import sorokinuladzimir.com.homebarassistant.db.entity.Ingredient;


public class SelectableIngredientsAdapter extends RecyclerView.Adapter<SelectableIngredientsAdapter.IngredientViewHolder>
        implements Filterable{

    public interface OnItemClickListener {
        void onItemClick(Ingredient item);
    }

    private List<Ingredient> mIngredientsList = new ArrayList();
    private List<Ingredient> mFilteredIngredientsList = new ArrayList();

    private final OnItemClickListener listener;

    private Context mContext;

    public SelectableIngredientsAdapter(Context context, OnItemClickListener listener) {
        this.listener = listener;
        this.mContext = context;
    }

    @Override
    public IngredientViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new IngredientViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.ingredients_list_single_ingredient_item,parent,false));
    }

    @Override
    public void onBindViewHolder(IngredientViewHolder holder, int position) {
        holder.bind(mContext, mFilteredIngredientsList.get(position),listener);
    }

    @Override
    public int getItemCount() {
        return mFilteredIngredientsList.size();
    }

    @SuppressLint("StaticFieldLeak")
    public void setIngredients(final List<Ingredient> ingredients) {
        if (mIngredientsList == null) {
            mIngredientsList = ingredients;
            mFilteredIngredientsList = ingredients;
            notifyItemRangeInserted(0, ingredients.size());
        } else {
            new AsyncTask<Void, Void, DiffUtil.DiffResult>() {

                @Override
                protected DiffUtil.DiffResult doInBackground(Void... voids) {
                    return DiffUtil.calculateDiff(new DiffUtil.Callback() {
                        @Override
                        public int getOldListSize() {
                            return mIngredientsList.size();
                        }

                        @Override
                        public int getNewListSize() {
                            return ingredients.size();
                        }

                        @Override
                        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                            return mIngredientsList.get(oldItemPosition).id == ingredients.get(newItemPosition).id;
                        }

                        @Override
                        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                            return mIngredientsList.get(oldItemPosition).equals(ingredients.get(newItemPosition));
                        }
                    });
                }

                @Override
                protected void onPostExecute(DiffUtil.DiffResult diffResult) {
                    mIngredientsList = ingredients;
                    mFilteredIngredientsList = ingredients;
                    diffResult.dispatchUpdatesTo(SelectableIngredientsAdapter.this);
                }
            }.execute();
        }
    }

    public static class IngredientViewHolder extends RecyclerView.ViewHolder{

        final TextView ingredientName;
        final ImageView ingredientImage;

        public IngredientViewHolder(View itemView) {
            super(itemView);
            ingredientName = itemView.findViewById(R.id.tv_ingredient_item);
            ingredientImage = itemView.findViewById(R.id.image_ingredient_item);
        }

        public void bind(Context mContext, final Ingredient item, final OnItemClickListener listener) {

            if(item.name != null)ingredientName.setText(item.name);

            Glide.with(mContext)
                    .load(item.image != null ? item.image : R.drawable.camera_placeholder)
                    .apply(RequestOptions.circleCropTransform())
                    .into(ingredientImage);


            itemView.setOnClickListener(v -> listener.onItemClick(item));
        }
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    //mFilteredIngredientsList.clear();
                    mFilteredIngredientsList = mIngredientsList;
                } else {
                    List<Ingredient> filteredList = new ArrayList<>();
                    for (Ingredient  ingredient: mIngredientsList) {
                        if (ingredient.name.toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(ingredient);
                        }
                    }
                    mFilteredIngredientsList = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = mFilteredIngredientsList;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                mFilteredIngredientsList = (ArrayList<Ingredient>) filterResults.values ;
                notifyDataSetChanged();
            }
        };
    }
}