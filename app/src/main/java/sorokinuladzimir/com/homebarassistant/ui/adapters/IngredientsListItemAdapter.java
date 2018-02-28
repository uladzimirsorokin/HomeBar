package sorokinuladzimir.com.homebarassistant.ui.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import sorokinuladzimir.com.homebarassistant.BarApp;
import sorokinuladzimir.com.homebarassistant.R;
import sorokinuladzimir.com.homebarassistant.db.entity.Ingredient;
import sorokinuladzimir.com.homebarassistant.ui.animator.FlipAnimator;


public class IngredientsListItemAdapter extends RecyclerView.Adapter<IngredientsListItemAdapter.IngredientViewHolder>
        implements Filterable{

    public interface OnItemClickListener {
        void onItemClick(Ingredient item);
    }

    private List<Ingredient> mIngredientsList = new ArrayList();
    private List<Ingredient> mFilteredIngredientsList = new ArrayList();
    private Context mContext;
    private final OnItemClickListener listener;

    private HashSet<Ingredient> selectedSet = new HashSet<>();
    private List<Long> selectedIds = new ArrayList<>();

    public IngredientsListItemAdapter(Context context, OnItemClickListener listener) {
        this.listener = listener;
        this.mContext = context;
    }

    @Override
    public IngredientViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new IngredientViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.ingredients_list_single_ingredient_item, parent,false));
    }

    @Override
    public void onBindViewHolder(IngredientViewHolder holder, int position) {

        holder.bind(mContext,
                mFilteredIngredientsList.get(position),
                listener,
                isIngredientSelected(position));
        //selectedIngredients.get(mFilteredIngredientsList.get(position).hashCode(), false)
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
            BarApp.getInstance().getExecutors().diskIO().execute(
                    () -> {
                        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
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

                        BarApp.getInstance().getExecutors().mainThread().execute(() -> {
                            mIngredientsList = ingredients;
                            mFilteredIngredientsList = ingredients;
                            restoreSelection();
                            diffResult.dispatchUpdatesTo(IngredientsListItemAdapter.this);
                        });
                    }
            );

        }

    }

    public static class IngredientViewHolder extends RecyclerView.ViewHolder{

        final TextView ingredientName;
        final ImageView ingredientImage;
        final RelativeLayout imageBack;
        final RelativeLayout imageFront;

        public IngredientViewHolder(View itemView) {
            super(itemView);
            ingredientName = itemView.findViewById(R.id.tv_ingredient_item);
            ingredientImage = itemView.findViewById(R.id.image_ingredient_item);
            imageBack = itemView.findViewById(R.id.image_back);
            imageFront = itemView.findViewById(R.id.image_front);
        }

        public void bind(Context mContext, final Ingredient item, final OnItemClickListener listener, boolean selected) {

            if(item.name != null) ingredientName.setText(item.name);

            if (selected) {
                itemView.setActivated(true);
                imageFront.setVisibility(View.INVISIBLE);
                imageBack.setVisibility(View.VISIBLE);
                imageBack.setAlpha(1);

            } else {
                itemView.setActivated(false);
                imageFront.setVisibility(View.VISIBLE);
                imageBack.setVisibility(View.INVISIBLE);
                imageBack.setAlpha(1);

                Glide.with(mContext)
                        .load(item.image != null ? item.image : R.drawable.camera_placeholder)
                        .apply(RequestOptions.circleCropTransform())
                        .into(ingredientImage);

            }

            itemView.setOnClickListener(v -> {
                listener.onItemClick(item);
            });
        }
    }

    public void toggleSelection(Ingredient item) {
        int position = mFilteredIngredientsList.indexOf(item);
        if (position != -1){
            if (selectedSet.contains(item)) {
                selectedSet.remove(item);
                selectedIds.remove(item.id);
            } else {
                selectedSet.add(item);
                selectedIds.add(item.id);
            }
            notifyItemChanged(position);
        }
    }

    private boolean isIngredientSelected(int position) {
        return selectedSet.contains(mFilteredIngredientsList.get(position));
    }

    public List<Long> getSelectedIds() {
        return selectedIds;
    }

    public void setSelectedIds(List<Long> selectedIds) {
        this.selectedIds = new ArrayList<>();
        this.selectedIds.addAll(selectedIds);
    }

    public void restoreSelection(){
        if (selectedSet.isEmpty()) {
            for (Ingredient ingredient : mFilteredIngredientsList) {
                if (selectedIds.indexOf(ingredient.id) != -1){
                    selectedSet.add(ingredient);
                    notifyItemChanged(mFilteredIngredientsList.indexOf(ingredient));
                }
            }
        }
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
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
