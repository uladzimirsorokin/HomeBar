package sorokinuladzimir.com.homebarassistant;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;


public class ResourcesActivity extends AppCompatActivity {

    private static final String LINK_ABSOLUTDRINKS = "https://www.absolutdrinks.com";
    private static final String LINK_FREEPIC = "https://www.freepik.com/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resources);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Resources");
        }
        initCards();
    }

    private void initCards() {
        View cardIcons = findViewById(R.id.card_pic);
        View cardAbsolut = findViewById(R.id.card_absolut);
        cardIcons.setOnClickListener(view -> startWebsiteActivity(LINK_FREEPIC));
        cardAbsolut.setOnClickListener(view -> startWebsiteActivity(LINK_ABSOLUTDRINKS));
    }

    private void startWebsiteActivity(String link) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(link)));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return false;
    }
}
