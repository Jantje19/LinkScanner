package ml.testsite_vic;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;

import ml.testsite_vic.linkscanner.R;

public class LinkHistory extends AppCompatActivity {

    // TODO: In order

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_link_history);
        getSupportActionBar().setTitle(getText(R.string.history));

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.floatingActionButton);

        // Get history
        String hist;
        hist = getIntent().getStringExtra(getString(R.string.intent_tinyDB));

        try {
            hist = hist.substring(0, hist.length() - 1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        final List<String> historyArray = Arrays.asList(hist.toLowerCase().split(getString(R.string.arrayListToStringSplitChar)));

        // Check if there is any history
        if (hist == null || hist == "") {
            TextView textView = (TextView) findViewById(R.id.text_view);
            ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);

            progressBar.setVisibility(View.GONE);
            textView.setText(getString(R.string.noHist));
        } else {
            // Get array and add textViews to listView from array
            load(historyArray);
        }

        // Fab onclick for clearing the history
        fab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                MainActivity.deleteHistory(getApplicationContext());
                load(historyArray);
            }
        });
    }

    void load(List<String> arrayList) {
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.linear_layout);
        linearLayout.removeAllViews();

        // ListView
        ListView listView = new ListView(this);
        ArrayAdapter adapter = new ArrayAdapter<>(this, R.layout.activity_listview, arrayList);

        listView.setClickable(true);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MainActivity.openWebPage(((TextView) view).getText().toString(), LinkHistory.this);
            }
        });

        linearLayout.addView(listView);
    }

    void log(String s) {
        Log.d("LinkHistory", s);
    }
}