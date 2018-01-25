package sorokinuladzimir.com.homebarassistant.ui.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import sorokinuladzimir.com.homebarassistant.R;
import sorokinuladzimir.com.homebarassistant.net.entity.Glass;


/**
 * Created by sorok on 09.11.2017.
 */

public class AddDrinkGlassAdapter extends ArrayAdapter<Glass> {

    private List<Glass> mGlasses = new ArrayList<>();
    private Context mContext;
    private int mLayout;

    public AddDrinkGlassAdapter(@NonNull Context context, int layoutResource, @NonNull List objects) {
        super(context, layoutResource, objects);
        mGlasses.addAll(objects);
        mContext = context;
        mLayout = layoutResource;
    }

    @Override
    public View getDropDownView(int position, View convertView,
                                ViewGroup parent) {

        return getCustomView(position, convertView, parent);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        return getCustomView(position, convertView, parent);
    }

    private View getCustomView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View row = inflater.inflate(mLayout, parent, false);

        ImageView icon = row.findViewById(R.id.icon);
        TextView glassName = row.findViewById(R.id.glassName);

        icon.setImageResource(mGlasses.get(position).image);
        glassName.setText(mGlasses.get(position).name);

        return row;
    }

}
