package ml.testsite_vic.PopUp;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import ml.testsite_vic.LinkHistory;
import ml.testsite_vic.MainActivity;
import ml.testsite_vic.ShareActivity;
import ml.testsite_vic.linkscanner.R;

public class ViewDialog {

    public void showDialog(final Activity activity, final String[] title, Integer[] image){
        final Dialog dialog = new Dialog(activity);

        ListView listView = new ListView(activity);
        CustomListAdapter adapter = new CustomListAdapter(activity, title, image);

        listView.setAdapter(adapter);
        listView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(listView);
        dialog.setCanceledOnTouchOutside(true);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch(position) {
                    case 0:
                        dialog.hide();
                        Intent share = new Intent(activity.getApplicationContext(), ShareActivity.class);
                        activity.startActivity(share);
                        break;
                    case 1:
                        Intent intent = new Intent(activity.getApplicationContext(), LinkHistory.class);
                        intent.putExtra(activity.getString(R.string.intent_tinyDB), MainActivity.arrayListToString(MainActivity.tinydb.getListString(activity.getString(R.string.tinyDB_cache_name)), activity));
                        activity.startActivity(intent);
                        break;
                    case 2:
                        Toast.makeText(activity.getApplicationContext(), activity.getString(R.string.WIP), Toast.LENGTH_LONG).show();
                        break;
                    default:
                        break;
                }
            }
        });

        dialog.show();
    }
}
