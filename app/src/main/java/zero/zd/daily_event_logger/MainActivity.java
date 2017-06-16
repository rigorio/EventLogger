package zero.zd.daily_event_logger;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.codetroopers.betterpickers.radialtimepicker.RadialTimePickerDialogFragment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import zero.zd.daily_event_logger.adapter.EventArrayAdapter;
import zero.zd.daily_event_logger.database.EventDbManager;

public class MainActivity extends AppCompatActivity {

    private static final String FRAG_TAG_TIME_PICKER = "FRAG_TAG_TIME_PICKER";

    ArrayAdapter<Event> mEventArrayAdapter;
    private List<Event> mEventList;
    private EventDbManager mEventDbManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initObjects();
        updateListView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_about:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initObjects() {
        mEventList = new ArrayList<>();
        mEventDbManager = new EventDbManager(this);

        mEventDbManager.open();
        mEventList = mEventDbManager.getEventList();

        ListView listView = (ListView) findViewById(R.id.list_event);
        mEventArrayAdapter = new EventArrayAdapter(this, R.layout.item_event, mEventList);
        listView.setAdapter(mEventArrayAdapter);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showEventDialog();
            }
        });
    }

    private void updateListView() {
        sortEvents();
        mEventArrayAdapter.notifyDataSetChanged();
    }

    private void sortEvents() {
        Collections.sort(mEventList, new Comparator<Event>() {
            @Override
            public int compare(Event o1, Event o2) {
                return o1.getDate().compareTo(o2.getDate());
            }
        });
    }

    private void showEventDialog() {
        final Event event = new Event();

        ViewGroup dialogRootView = (ViewGroup) findViewById(R.id.root_dialog_create_event);
        final View dialogView = getLayoutInflater()
                .inflate(R.layout.dialog_event, dialogRootView);
        final EditText eventEditText = (EditText) dialogView.findViewById(R.id.edit_event);
        Button timeButton = (Button) dialogView.findViewById(R.id.button_time);
        timeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTimePickerDialog(view, event);
            }
        });
        timeButton.setText(event.getStringDate());

        AlertDialog eventDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.title_event)
                .setView(dialogView)
                .setCancelable(false)
                .setNegativeButton(R.string.action_discard, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(R.string.action_save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String eventText = eventEditText.getText().toString();
                        if (eventText.isEmpty()) {
                            Toast.makeText(MainActivity.this, R.string.err_input_event,
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            eventText = formatText(eventText);
                            event.setEvent(eventText);
                            addEvent(event);
                            dialog.dismiss();
                        }
                    }
                })
                .create();
        eventDialog.show();
    }

    private void showTimePickerDialog(View view, final Event event) {
        final Button timeButton = (Button) view;
        RadialTimePickerDialogFragment timePickerDialog = new RadialTimePickerDialogFragment()
                .setOnTimeSetListener(new RadialTimePickerDialogFragment.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(RadialTimePickerDialogFragment dialog,
                                          int hourOfDay, int minute) {
                        event.setDate(getDateFromTime(hourOfDay, minute));
                        timeButton.setText(event.getStringDate());
                    }
                })
                .setStartTime(getCurrentHour(), getCurrentMinute())
                .setDoneText("Save")
                .setCancelText("Discard");
        timePickerDialog.show(getSupportFragmentManager(), FRAG_TAG_TIME_PICKER);
    }

    private Date getDateFromTime(int hourOfDay, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);
        return calendar.getTime();
    }

    private int getCurrentHour() {
        return Calendar.getInstance().get(Calendar.HOUR);
    }

    private int getCurrentMinute() {
        return Calendar.getInstance().get(Calendar.MINUTE);
    }

    private void addEvent(Event event) {
        mEventList.add(event);
        mEventDbManager.addEvent(event);

        updateListView();
    }

    private String formatText(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}
