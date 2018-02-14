package sorokinuladzimir.com.homebarassistant.ui.fragments;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.thebluealliance.spectrum.SpectrumDialog;

import sorokinuladzimir.com.homebarassistant.Constants;
import sorokinuladzimir.com.homebarassistant.R;
import sorokinuladzimir.com.homebarassistant.ui.subnavigation.BackButtonListener;
import sorokinuladzimir.com.homebarassistant.ui.subnavigation.RouterProvider;


/**
 * Created by sorok on 20.10.2017.
 */

public class SearchDrinksFragment extends Fragment implements BackButtonListener {
    private static final String EXTRA_NAME = "extra_name";

    private ActionBar mToolbar;

    private Switch mSwIsCarbonated;

    private int mMinRating = 0;
    private int mMaxRating = 100;
    private int mGlassId;
    private int mTasteId;
    private int mIngredientId;
    private boolean mIsCarbonated = false;
    private boolean mIsAlcoholic = true;
    private int mColor = -2;
    private ImageView mViewColor;
    private Spinner mSpGlass;
    private Spinner mSpTaste;
    private Spinner mSpIngridient;
    private int mSkill;
    private Switch mSwIsAlcoholic;
    private SeekBar mSbSkill;
    private TextView mTvSkill;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fr_search_drinks, container, false);

        if(savedInstanceState != null) {
            mMaxRating = savedInstanceState.getInt(Constants.Extra.MAX_RATING);
            mMinRating = savedInstanceState.getInt(Constants.Extra.MIN_RATING);
            mGlassId = savedInstanceState.getInt(Constants.Extra.GLASS_ID);
            mTasteId = savedInstanceState.getInt(Constants.Extra.TASTE_ID);
            mIngredientId = savedInstanceState.getInt(Constants.Extra.INGREDIENT_ID);
            mColor = savedInstanceState.getInt(Constants.Extra.COLOR);
            mSkill = savedInstanceState.getInt(Constants.Extra.SKILL);
            mIsAlcoholic = savedInstanceState.getBoolean(Constants.Extra.ALCOHOLIC);
            mIsCarbonated = savedInstanceState.getBoolean(Constants.Extra.CARBONATED);
        }

        initToolbar(rootView);
        initViews(rootView);

        return rootView;
    }

    public static SearchDrinksFragment getNewInstance(String name) {
        SearchDrinksFragment fragment = new SearchDrinksFragment();

        Bundle arguments = new Bundle();
        arguments.putString(EXTRA_NAME, name);
        fragment.setArguments(arguments);

        return fragment;
    }

    private void initToolbar(View view){
        setHasOptionsMenu(true);
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        mToolbar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if(mToolbar != null)
            mToolbar.setDisplayHomeAsUpEnabled(true);
        mToolbar.setTitle(R.string.search_drink_fragment_title);
    }

    private void initViews(View rootView) {
        mSpGlass = rootView.findViewById(R.id.spin_glass_type);
        ArrayAdapter<CharSequence> adapterGlass = ArrayAdapter.createFromResource(getContext(),
                R.array.glass_name, android.R.layout.simple_spinner_item);
        adapterGlass.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpGlass.setAdapter(adapterGlass);
        mSpGlass.setOnItemSelectedListener(adapterListener);

        mSpTaste = rootView.findViewById(R.id.spin_taste_type);
        ArrayAdapter<CharSequence> adapterTaste = ArrayAdapter.createFromResource(getContext(),
                R.array.taste_name, android.R.layout.simple_spinner_item);
        adapterTaste.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpTaste.setAdapter(adapterTaste);
        mSpTaste.setOnItemSelectedListener(adapterListener);

        mSpIngridient = rootView.findViewById(R.id.spin_ingridient_type);
        ArrayAdapter<CharSequence> adapterIngridient = ArrayAdapter.createFromResource(getContext(),
                R.array.ingridient_name, android.R.layout.simple_spinner_item);
        adapterIngridient.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpIngridient.setAdapter(adapterIngridient);
        mSpIngridient.setOnItemSelectedListener(adapterListener);

        mSwIsCarbonated = rootView.findViewById(R.id.sw_search_carbonated);
        mSwIsCarbonated.setOnCheckedChangeListener(switchListener);

        mSwIsAlcoholic = rootView.findViewById(R.id.sw_search_alcoholic);
        mSwIsAlcoholic.setOnCheckedChangeListener(switchListener);

        mViewColor = rootView.findViewById(R.id.choose_color);
        mViewColor.setOnClickListener(colorListener);

        mSbSkill = rootView.findViewById(R.id.sb_skill);
        mSbSkill.setOnSeekBarChangeListener(skillListener);
        mTvSkill = rootView.findViewById(R.id.tv_skill_container);
    }



    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.searchfragment_menu,menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            ((RouterProvider)getParentFragment()).getRouter().exit();
            return true;
        }
        if(item.getItemId() == R.id.ab_search){
            Bundle bundle = new Bundle();
            bundle.putInt(Constants.Extra.GLASS_ID, mGlassId);
            bundle.putInt(Constants.Extra.TASTE_ID, mTasteId);
            bundle.putInt(Constants.Extra.INGREDIENT_ID, mIngredientId);
            bundle.putInt(Constants.Extra.MIN_RATING, mMinRating);
            bundle.putInt(Constants.Extra.MAX_RATING, mMaxRating);
            bundle.putInt(Constants.Extra.SKILL, mSkill);
            bundle.putInt(Constants.Extra.COLOR, mColor);
            bundle.putBoolean(Constants.Extra.CARBONATED, mIsCarbonated);
            bundle.putBoolean(Constants.Extra.ALCOHOLIC, mIsAlcoholic);
            ((RouterProvider)getParentFragment()).getRouter().replaceScreen(Screens.FOUND_DRINKS, bundle);
            return true;
        }
        return false;
    }

    @Override
    public boolean onBackPressed() {
        ((RouterProvider)getParentFragment()).getRouter().exit();
        return true;
    }

    private AdapterView.OnItemSelectedListener adapterListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            int parentId = parent.getId();
            if(parentId == mSpGlass.getId()){
                mGlassId = position;
            }
            if(parentId == mSpTaste.getId()){
                mTasteId = position;
            }
            if(parentId == mSpIngridient.getId()){
                mIngredientId = position;
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };

    private CompoundButton.OnCheckedChangeListener switchListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if(buttonView.getId() == mSwIsCarbonated.getId()) {
                mIsCarbonated = isChecked;
            }
            if(buttonView.getId() == mSwIsAlcoholic.getId()) {
                mIsAlcoholic = isChecked;
            }
        }
    };

    private SeekBar.OnSeekBarChangeListener skillListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
            mSkill = progress;
            switch (progress) {
                case 0:
                    mTvSkill.setText(R.string.skill_novice);
                    break;

                case 1:
                    mTvSkill.setText(R.string.skill_middle);
                    break;

                case 2:
                    mTvSkill.setText(R.string.skill_master);
                    break;
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    private View.OnClickListener colorListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final int[] colors = getResources().getIntArray(R.array.colors);
            new SpectrumDialog.Builder(getContext())
                    .setColors(colors)
                    .setDismissOnColorSelected(false)
                    .setFixedColumnCount(3)
                    .setOutlineWidth(2)
                    .setOnColorSelectedListener((positiveResult, color) -> {
                        if (positiveResult) {
                            mColor = color;
                            GradientDrawable colorCircle = (GradientDrawable) getResources().getDrawable(R.drawable.color_circle);
                            colorCircle.setStroke(2, Color.BLACK);
                            colorCircle.setColor(mColor);

                            mViewColor.setBackground(colorCircle);

                        } else {
                            Toast.makeText(getContext(), "Dialog cancelled", Toast.LENGTH_SHORT).show();
                        }
                    }).build().show(getFragmentManager(), "dialog_color");
        }
    };

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(Constants.Extra.MAX_RATING, mMaxRating);
        outState.putInt(Constants.Extra.MIN_RATING, mMinRating);
        outState.putInt(Constants.Extra.GLASS_ID, mGlassId);
        outState.putInt(Constants.Extra.TASTE_ID, mTasteId);
        outState.putInt(Constants.Extra.INGREDIENT_ID, mIngredientId);
        outState.putBoolean(Constants.Extra.CARBONATED,mIsCarbonated);
        outState.putBoolean(Constants.Extra.ALCOHOLIC,mIsAlcoholic);
        outState.putInt(Constants.Extra.COLOR,mColor);
        outState.putInt(Constants.Extra.SKILL,mSkill);
    }

}
