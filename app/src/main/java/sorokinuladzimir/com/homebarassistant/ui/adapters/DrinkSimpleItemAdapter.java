package sorokinuladzimir.com.homebarassistant.ui.adapters;

import android.support.annotation.NonNull;
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

import sorokinuladzimir.com.homebarassistant.Constants;
import sorokinuladzimir.com.homebarassistant.R;
import sorokinuladzimir.com.homebarassistant.db.entity.Drink;


public class DrinkSimpleItemAdapter extends RecyclerView.Adapter<DrinkSimpleItemAdapter.SimpleDrinkVH> {

    public interface OnItemClickListener {
        void onItemClick(Drink item);
    }

    private ArrayList<Drink> mData = new ArrayList();
    private final OnItemClickListener listener;

    public DrinkSimpleItemAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }


    
    @Override
    public SimpleDrinkVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SimpleDrinkVH(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.simple_drink_item, parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull SimpleDrinkVH holder, int position) {
        holder.bind(mData.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public void setData(List<Drink> cocktails) {
        mData.clear();
        mData.addAll(cocktails);
        notifyDataSetChanged();
    }

    public static class SimpleDrinkVH extends RecyclerView.ViewHolder{

        final ImageView image;
        final TextView name;


        SimpleDrinkVH(View itemView) {
            super(itemView);

            image = itemView.findViewById(R.id.image);
            name = itemView.findViewById(R.id.tv_name);
        }

        public void bind(final Drink drinkItem, final OnItemClickListener listener) {

            Glide.with(image.getContext())
                    .load(drinkItem.getImage() != null ? drinkItem.getImage() : R.drawable.camera_placeholder)
                    .apply(RequestOptions.circleCropTransform())
                    .into(image);

            if (drinkItem.getName() != null) {
                name.setText(drinkItem.getName());
            } else {
                name.setText("");
            }

            itemView.setOnClickListener(v -> listener.onItemClick(drinkItem));
        }
    }
}
