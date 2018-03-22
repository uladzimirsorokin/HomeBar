package sorokinuladzimir.com.homebarassistant.ui.adapters;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import sorokinuladzimir.com.homebarassistant.R;
import sorokinuladzimir.com.homebarassistant.db.entity.WholeCocktail;

public class AddDrinkIngredientItemAdapter extends RecyclerView.Adapter<AddDrinkIngredientItemAdapter.IngredientViewHolder> {

    public AddDrinkIngredientItemAdapter(Context context, OnDeleteClickListener deleteClickListener) {
        this.listener = deleteClickListener;
        this.mContext = context;
        unitArray = mContext.getResources().getStringArray(R.array.ingredient_unit);
    }

    public interface OnDeleteClickListener {
        void onDeleteItemClick(int position, WholeCocktail ingredient);
    }

    private List<WholeCocktail> mIngredients = new ArrayList<>();

    private final OnDeleteClickListener listener;
    private Context mContext;

    private final String[] unitArray;

    public List<WholeCocktail> getIngredients() {
        return mIngredients;
    }

    @Override
    public IngredientViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new IngredientViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.add_drink_ingredient_item, parent,false), new MyAmountListener(), new UnitListener());
    }

    @Override
    public void onBindViewHolder(IngredientViewHolder holder, int position) {
        holder.myAmountListener.updatePosition(holder.getAdapterPosition());
        WholeCocktail cocktail = mIngredients.get(holder.getAdapterPosition());
        if (cocktail.getAmount() != null){
            holder.amount.setText("" + cocktail.getAmount());
        } else {
            holder.amount.setText("");
        }
        if (cocktail.getIngredientName() != null) holder.name.setText(cocktail.getIngredientName());
        holder.unitListener.updatePosition(holder.getAdapterPosition());

        if (cocktail.getUnit() != null) {
            holder.unit.setSelection(Arrays.asList(unitArray).indexOf(cocktail.getUnit()));
        }

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
        public Spinner unit;
        public View deleteButton;
        public MyAmountListener myAmountListener;
        public UnitListener unitListener;


        public IngredientViewHolder(View itemView, MyAmountListener myAmountListener, UnitListener unitListener) {
            super(itemView);

            name = itemView.findViewById(R.id.tv_ingredient_name);
            amount = itemView.findViewById(R.id.et_ingredient_amount);
            unit = itemView.findViewById(R.id.spin_ingridient_unit);
            deleteButton = itemView.findViewById(R.id.ingredient_item_delete);
            this.myAmountListener = myAmountListener;
            amount.addTextChangedListener(myAmountListener);
            this.unitListener = unitListener;
            ArrayAdapter<CharSequence> adapterUnit = ArrayAdapter.createFromResource(mContext,
                    R.array.ingredient_unit, android.R.layout.simple_spinner_item);
            adapterUnit.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            unit.setAdapter(adapterUnit);
            unit.setOnItemSelectedListener(unitListener);
        }

        public void bind(final int position, final OnDeleteClickListener listener) {
            deleteButton.setOnClickListener(v -> listener.onDeleteItemClick(position, mIngredients.get(position)));
        }

    }

    private class UnitListener implements AdapterView.OnItemSelectedListener {

        private int position;

        public void updatePosition(int position) {
            this.position = position;
        }

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
            mIngredients.get(position).setUnit(mContext.getResources().getStringArray(R.array.ingredient_unit)[pos]);
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

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
                mIngredients.get(position).setAmount(charSequence.toString());
            } else {
                mIngredients.get(position).setAmount("");
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    }

}
