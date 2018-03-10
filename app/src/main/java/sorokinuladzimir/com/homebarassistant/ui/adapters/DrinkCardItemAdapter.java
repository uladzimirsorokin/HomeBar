package sorokinuladzimir.com.homebarassistant.ui.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import sorokinuladzimir.com.homebarassistant.Constants;
import sorokinuladzimir.com.homebarassistant.R;
import sorokinuladzimir.com.homebarassistant.db.entity.Taste;
import sorokinuladzimir.com.homebarassistant.net.entity.DrinkEntity;


/**
 * Created by 1 on 10/12/2016.
 */

public class DrinkCardItemAdapter extends RecyclerView.Adapter<DrinkCardItemAdapter.CardViewHolder> {

    public DrinkCardItemAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(DrinkEntity item);
    }

    private ArrayList<DrinkEntity> mData = new ArrayList();
    private final OnItemClickListener listener;
    
    @Override
    public CardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new CardViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_view_cocktail_item,parent,false));
    }

    @Override
    public void onBindViewHolder(CardViewHolder holder, int position) {
        holder.bind(mData.get(position),listener);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public void setData(ArrayList<DrinkEntity> cocktails) {
        mData.clear();
        mData.addAll(cocktails);
        notifyDataSetChanged();
    }

    public static class CardViewHolder extends RecyclerView.ViewHolder{

        final ImageView cardImage;
        final TextView title;
        final TextView subtitle;
        final RatingBar rating;

        public CardViewHolder(View itemView) {
            super(itemView);

            cardImage = itemView.findViewById(R.id.card_image);
            title = itemView.findViewById(R.id.card_title);
            subtitle = itemView.findViewById(R.id.card_subtitle);
            rating = itemView.findViewById(R.id.card_rating_bar);
        }

        public void bind(final DrinkEntity drinkItem, final OnItemClickListener listener) {

            Glide.with(cardImage.getContext())
                    .load(Constants.Uri.ABSOLUT_DRINKS_IMAGE_ROOT + drinkItem.getId() + ".png")
                    .into(cardImage);


            title.setText(drinkItem.getName());

            ArrayList<Taste> tastes = drinkItem.getTastes();

            if(tastes != null) {
                String tastesStr = tastes.get(0).getText();
                for (int i = 1; i < tastes.size(); i++){
                    tastesStr += ", " + tastes.get(i).getText();
                }
                subtitle.setText(tastesStr);
            }

            rating.setProgress(drinkItem.getRating() / 10);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(drinkItem);
                }
            });
        }



    }
}
