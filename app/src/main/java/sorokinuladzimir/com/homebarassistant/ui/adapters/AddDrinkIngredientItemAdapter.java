package sorokinuladzimir.com.homebarassistant.ui.adapters;

import android.annotation.SuppressLint;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import sorokinuladzimir.com.homebarassistant.BarApp;
import sorokinuladzimir.com.homebarassistant.R;
import sorokinuladzimir.com.homebarassistant.db.entity.Drink;
import sorokinuladzimir.com.homebarassistant.db.entity.Ingredient;
import sorokinuladzimir.com.homebarassistant.db.entity.WholeCocktail;

/**
 * Created by sorok on 04.11.2017.
 */

public class AddDrinkIngredientItemAdapter extends RecyclerView.Adapter<AddDrinkIngredientItemAdapter.IngredientViewHolder> {

    public AddDrinkIngredientItemAdapter(OnDeleteClickListener deleteClickListener) {
        this.listener = deleteClickListener;
    }

    public interface OnDeleteClickListener {
        void onDeleteItemClick(int position, WholeCocktail ingredient);
    }

    private List<WholeCocktail> mIngredients = new ArrayList<>();

    private final OnDeleteClickListener listener;


    public List<WholeCocktail> getIngredients() {
        return mIngredients;
    }

    @Override
    public IngredientViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new IngredientViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.add_drink_ingredient_item, parent,false), new MyAmountListener());
    }

    @Override
    public void onBindViewHolder(IngredientViewHolder holder, int position) {
        holder.myAmountListener.updatePosition(holder.getAdapterPosition());
        WholeCocktail cocktail = mIngredients.get(holder.getAdapterPosition());
        if (cocktail.amount != null){
            holder.amount.setText("" + cocktail.amount);
        } else {
            holder.amount.setText("");
        }
        if (cocktail.ingredientName != null) holder.name.setText(cocktail.ingredientName);
        holder.bind(holder.getAdapterPosition(), listener);
    }

    @Override
    public int getItemCount() {
        return mIngredients.size();
    }

    public void deleteItem(int pos) {
        mIngredients.remove(pos);
        notifyDataSetChanged();
    }

    public void setData(List<WholeCocktail> cocktails) {
        mIngredients.clear();
        mIngredients.addAll(cocktails);
        notifyDataSetChanged();
    }

    public class IngredientViewHolder extends RecyclerView.ViewHolder {

        public TextView name;
        public EditText amount;
        public View deleteButton;
        public MyAmountListener myAmountListener;


        public IngredientViewHolder(View itemView, MyAmountListener myAmountListener) {
            super(itemView);

            name = itemView.findViewById(R.id.tv_ingredient_name);
            amount = itemView.findViewById(R.id.et_ingredient_amount);
            deleteButton = itemView.findViewById(R.id.ingredient_item_delete);
            this.myAmountListener = myAmountListener;
            amount.addTextChangedListener(myAmountListener);
        }

        public void bind(final int position, final OnDeleteClickListener listener) {
            deleteButton.setOnClickListener(v -> listener.onDeleteItemClick(position, mIngredients.get(position)));
        }

    }

    private class MyAmountListener implements TextWatcher {

        private int position;


        public void updatePosition(int position) {
            this.position = position;
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            if(charSequence.length() > 0){
                mIngredients.get(position).amount = charSequence.toString();
            } else {
                mIngredients.get(position).amount = "";
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    }

}
