package sorokinuladzimir.com.homebarassistant.ui.adapters;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.turingtechnologies.materialscrollbar.INameableAdapter;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Objects;

import sorokinuladzimir.com.homebarassistant.BarApp;
import sorokinuladzimir.com.homebarassistant.R;
import sorokinuladzimir.com.homebarassistant.db.entity.Ingredient;


public class IngredientsItemAdapter extends RecyclerView.Adapter<IngredientsItemAdapter.IngredientViewHolder>
        implements INameableAdapter {

    private final OnItemClickListener listener;
    private List<Ingredient> mIngredientsList;
    private final Deque<List<Ingredient>> pendingUpdates = new ArrayDeque<>();

    public IngredientsItemAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public Character getCharacterForElement(int element) {
        Character c = TextUtils.isEmpty(mIngredientsList.get(element).getName()) ?
                Character.MIN_VALUE : mIngredientsList.get(element).getName().charAt(0);
        if (Character.isDigit(c)) {
            c = '#';
        }
        return c;
    }

    @NonNull
    @Override
    public IngredientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new IngredientViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_simple_drink, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull IngredientViewHolder holder, int position) {
        holder.bind(mIngredientsList.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return mIngredientsList != null ? mIngredientsList.size() : 0;
    }

    @SuppressLint("StaticFieldLeak")
    public void setIngredients(final List<Ingredient> ingredients) {
        pendingUpdates.push(ingredients);
        if (pendingUpdates.size() > 1) {
            return;
        }
        if (mIngredientsList == null) {
            mIngredientsList = ingredients;
            notifyItemRangeInserted(0, mIngredientsList.size() - 1);
            if (!pendingUpdates.isEmpty()) {
                List<Ingredient> latest = pendingUpdates.pop();
                pendingUpdates.clear();
                setIngredients(latest);
            }
        } else {
            BarApp.getExecutors().diskIO().execute(
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
                                return Objects.equals(mIngredientsList.get(oldItemPosition).getId(),
                                        ingredients.get(newItemPosition).getId());
                            }

                            @Override
                            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                                return mIngredientsList.get(oldItemPosition).equals(ingredients.get(newItemPosition));
                            }
                        });

                        BarApp.getExecutors().mainThread().execute(() -> {
                            pendingUpdates.remove(ingredients);
                            mIngredientsList = ingredients;
                            diffResult.dispatchUpdatesTo(IngredientsItemAdapter.this);
                            if (!pendingUpdates.isEmpty()) {
                                List<Ingredient> latest = pendingUpdates.pop();
                                pendingUpdates.clear();
                                setIngredients(latest);
                            }
                        });
                    }
            );
        }
    }

    public interface OnItemClickListener {
        void onItemClick(Ingredient item);
    }

    static class IngredientViewHolder extends RecyclerView.ViewHolder {

        final TextView ingredientName;
        final ImageView ingredientImage;

        IngredientViewHolder(View itemView) {
            super(itemView);
            ingredientName = itemView.findViewById(R.id.tv_name);
            ingredientImage = itemView.findViewById(R.id.image);
        }

        void bind(final Ingredient item, final OnItemClickListener listener) {
            if (item.getName() != null) ingredientName.setText(item.getName());
            Glide.with(itemView.getContext())
                    .load(item.getImage() != null ? item.getImage() : R.drawable.camera_placeholder)
                    .apply(RequestOptions.circleCropTransform())
                    .into(ingredientImage);
            itemView.setOnClickListener(v -> listener.onItemClick(item));
        }
    }
}
