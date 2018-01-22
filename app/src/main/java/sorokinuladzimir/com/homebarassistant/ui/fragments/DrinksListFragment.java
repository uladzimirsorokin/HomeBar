package sorokinuladzimir.com.homebarassistant.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import sorokinuladzimir.com.homebarassistant.R;
import sorokinuladzimir.com.homebarassistant.ui.subnavigation.RouterProvider;


/**
 * Created by sorok on 17.10.2017.
 */

public class DrinksListFragment extends Fragment {

    private static final String EXTRA_NAME = "dlf_extra_name";
    private FloatingActionButton mFab;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fr_drinks_list, container, false);

        initFAB(rootView);

        return rootView;
    }


    public static DrinksListFragment getNewInstance(String name) {
        DrinksListFragment fragment = new DrinksListFragment();

        Bundle arguments = new Bundle();
        arguments.putString(EXTRA_NAME, name);
        fragment.setArguments(arguments);

        return fragment;
    }

    private void initFAB(View view){
        mFab = view.findViewById(R.id.fab);
        mFab.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_add));
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((RouterProvider)getParentFragment()).getRouter().navigateTo(Screens.ADD_DRINK, null);
            }
        });
    }


}
