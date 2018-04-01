package sorokinuladzimir.com.homebarassistant.ui.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
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
import com.turingtechnologies.materialscrollbar.INameableAdapter;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

import sorokinuladzimir.com.homebarassistant.BarApp;
import sorokinuladzimir.com.homebarassistant.R;
import sorokinuladzimir.com.homebarassistant.db.entity.Ingredient;
import sorokinuladzimir.com.homebarassistant.ui.fragments.Screens;


public class IngredientsListItemAdapter extends RecyclerView.Adapter<IngredientsListItemAdapter.IngredientViewHolder>
        implements Filterable, INameableAdapter {

    @Override
    public Character getCharacterForElement(int element) {
        Character c = mFilteredIngredientsList.get(element).getName().charAt(0);
        if(Character.isDigit(c)) {
            c = '#';
        }
        return c;
    }

    public interface OnItemClickListener {
        void onItemClick(Ingredient item);
    }

    private List<Ingredient> mIngredientsList = new ArrayList();
    private List<Ingredient> mFilteredIngredientsList = new ArrayList();
    private Context mContext;
    private String mContainerName;
    private final OnItemClickListener listener;

    private HashSet<Ingredient> selectedSet = new HashSet<>();
    private List<Long> selectedIds = new ArrayList<>();

    private Deque<List<Ingredient>> pendingUpdates = new ArrayDeque<>();

    public IngredientsListItemAdapter(Context context, String containerName, OnItemClickListener listener) {
        this.listener = listener;
        this.mContext = context;
        this.mContainerName = containerName;
    }

    @NonNull
    @Override
    public IngredientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new IngredientViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(mContainerName.equals(Screens.INGREDIENTS) ? R.layout.ingredients_list_ingredient_item :
                                R.layout.ingredients_list_single_ingredient_item,
                        parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull IngredientViewHolder holder, int position) {

        holder.bind(mContext,
                mFilteredIngredientsList.get(position),
                listener,
                isIngredientSelected(position));
    }

    @Override
    public int getItemCount() {
        return mContainerName.equals(Screens.INGREDIENTS) ? mIngredientsList.size() : mFilteredIngredientsList.size();
    }

    @SuppressLint("StaticFieldLeak")
    public void setIngredients(final List<Ingredient> ingredients) {
        pendingUpdates.push(ingredients);
        if (pendingUpdates.size() > 1) {
            return;
        }
        if (mIngredientsList == null) {
            mIngredientsList = ingredients;
            mFilteredIngredientsList = ingredients;
            notifyItemRangeInserted(0, mFilteredIngredientsList.size()-1);
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
                                return Objects.equals(mIngredientsList.get(oldItemPosition).getId(), ingredients.get(newItemPosition).getId());
                            }

                            @Override
                            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                                return mIngredientsList.get(oldItemPosition).equals(ingredients.get(newItemPosition));
                            }
                        });

                        BarApp.getInstance().getExecutors().mainThread().execute(() -> {
                            pendingUpdates.remove(ingredients);
                            mIngredientsList = ingredients;
                            mFilteredIngredientsList = ingredients;
                            restoreSelection();
                            diffResult.dispatchUpdatesTo(IngredientsListItemAdapter.this);
                            if (pendingUpdates.size() > 0) {
                                List<Ingredient> latest = pendingUpdates.pop();
                                pendingUpdates.clear();
                                setIngredients(latest);
                            }
                        });
                    }
            );
        }
    }

    static class IngredientViewHolder extends RecyclerView.ViewHolder{

        final TextView ingredientName;
        final ImageView ingredientImage;
        final RelativeLayout imageBack;
        final RelativeLayout imageFront;

        IngredientViewHolder(View itemView) {
            super(itemView);
            ingredientName = itemView.findViewById(R.id.tv_ingredient_item);
            ingredientImage = itemView.findViewById(R.id.image_ingredient_item);
            imageBack = itemView.findViewById(R.id.image_back);
            imageFront = itemView.findViewById(R.id.image_front);
        }

        void bind(Context mContext, final Ingredient item, final OnItemClickListener listener, boolean selected) {

            if(item.getName() != null) ingredientName.setText(item.getName());

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
                        .load(item.getImage() != null ? item.getImage() : R.drawable.camera_placeholder)
                        .apply(RequestOptions.circleCropTransform())
                        .into(ingredientImage);

            }

            itemView.setOnClickListener(v -> listener.onItemClick(item));
        }
    }

    public void toggleSelection(Ingredient item) {
        int position = mFilteredIngredientsList.indexOf(item);
        if (position != -1){
            if (selectedSet.contains(item)) {
                selectedSet.remove(item);
                selectedIds.remove(item.getId());
            } else {
                selectedSet.add(item);
                selectedIds.add(item.getId());
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

    private void restoreSelection(){
        if (selectedSet.isEmpty()) {
            for (Ingredient ingredient : mFilteredIngredientsList) {
                if (selectedIds.indexOf(ingredient.getId()) != -1){
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
                        if (ingredient.getName().toLowerCase().contains(charString.toLowerCase())) {
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
                mFilteredIngredientsList = (ArrayList<Ingredient>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }
}
