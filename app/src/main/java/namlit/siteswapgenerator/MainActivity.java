package namlit.siteswapgenerator;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.CheckBox;
import android.text.TextWatcher;
import android.text.Editable;

import java.util.LinkedList;

import siteswaplib.*;

public class MainActivity extends AppCompatActivity {

    private LinkedList<Filter> mFilterList;

    private EditText mNumberOfObjects;
    private EditText mPeriodLength;
    private EditText mMaxThrow;
    private EditText mMinThrow;
    private EditText mNumberOfJugglers;
    private EditText mMaxResults;
    private EditText mTimeout;
    private CheckBox mZipsCheckbox;
    private CheckBox mZapsCheckbox;
    private CheckBox mHoldsCheckbox;
    private LinearLayout mFilterListLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNumberOfObjects    = (EditText) findViewById(R.id.number_of_objects);
        mPeriodLength       = (EditText) findViewById(R.id.period_length);
        mMaxThrow           = (EditText) findViewById(R.id.max_throw);
        mMinThrow           = (EditText) findViewById(R.id.min_throw);
        mNumberOfJugglers   = (EditText) findViewById(R.id.number_of_jugglers);
        mMaxResults         = (EditText) findViewById(R.id.max_results);
        mTimeout            = (EditText) findViewById(R.id.timeout);
        mZipsCheckbox       = (CheckBox) findViewById(R.id.include_zips_checkbox);
        mZapsCheckbox       = (CheckBox) findViewById(R.id.include_zaps_checkbox);
        mHoldsCheckbox      = (CheckBox) findViewById(R.id.include_holds_checkbox);
        mFilterListLayout   = (LinearLayout) findViewById(R.id.filter_list_layout);

        if (savedInstanceState != null) {
            mFilterList = (LinkedList<Filter>) savedInstanceState.getSerializable(
                    getString(R.string.main_activity__saved_filter_list));
        }
        else {
            mFilterList = new LinkedList<Filter>();

            SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
            int numberOfObjects  = sharedPref.getInt(getString(R.string.main_activity__settings_number_of_objects),  7);
            int periodLength     = sharedPref.getInt(getString(R.string.main_activity__settings_period_length),      5);
            int maxThrow         = sharedPref.getInt(getString(R.string.main_activity__settings_max_throw),         10);
            int minThrow         = sharedPref.getInt(getString(R.string.main_activity__settings_min_throw),          2);
            int numberOfJugglers = sharedPref.getInt(getString(R.string.main_activity__settings_number_of_jugglers), 2);
            int maxResults       = sharedPref.getInt(getString(R.string.main_activity__settings_max_results),      100);
            int timeout          = sharedPref.getInt(getString(R.string.main_activity__settings_timeout),            5);
            boolean isZips       = sharedPref.getBoolean(getString(R.string.main_activity__settings_is_zips), true);
            boolean isZaps       = sharedPref.getBoolean(getString(R.string.main_activity__settings_is_zaps), false);
            boolean isHolds      = sharedPref.getBoolean(getString(R.string.main_activity__settings_is_holds), false);

            mNumberOfObjects.setText(String.valueOf(numberOfObjects));
            mPeriodLength.setText(String.valueOf(periodLength));
            mMaxThrow.setText(String.valueOf(maxThrow));
            mMinThrow.setText(String.valueOf(minThrow));
            mNumberOfJugglers.setText(String.valueOf(numberOfJugglers));
            mMaxResults.setText(String.valueOf(maxResults));
            mTimeout.setText(String.valueOf(timeout));
            mZipsCheckbox.setChecked(isZips);
            mZapsCheckbox.setChecked(isZaps);
            mHoldsCheckbox.setChecked(isHolds);
        }

        updateAutoFilters();

        mNumberOfJugglers.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                try {
                    int numberOfJugglers = Integer.valueOf(s.toString());
                    Filter.removeDefaultFilters(mFilterList, numberOfJugglers);
                    Filter.addZips(mFilterList, numberOfJugglers);
                    Filter.addZaps(mFilterList, numberOfJugglers);
                    Filter.addHolds(mFilterList, numberOfJugglers);
                    updateFilterListView();
                }
                catch (NumberFormatException e) {
                }
            }
            @Override
            public void afterTextChanged(Editable s) {
                updateAutoFilters();
            }
        });


    }

    @Override
    protected void onStop() {
        super.onStop();

        try {
            SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();

            int numberOfObjects = Integer.valueOf(mNumberOfObjects.getText().toString());
            int periodLength = Integer.valueOf(mPeriodLength.getText().toString());
            int maxThrow = Integer.valueOf(mMaxThrow.getText().toString());
            int minThrow = Integer.valueOf(mMinThrow.getText().toString());
            int numberOfJugglers = Integer.valueOf(mNumberOfJugglers.getText().toString());
            int maxResults = Integer.valueOf(mMaxResults.getText().toString());
            int timeout = Integer.valueOf(mTimeout.getText().toString());
            boolean isZips = mZipsCheckbox.isChecked();
            boolean isZaps = mZapsCheckbox.isChecked();
            boolean isHolds = mHoldsCheckbox.isChecked();

            editor.putInt(getString(R.string.main_activity__settings_number_of_objects), numberOfObjects);
            editor.putInt(getString(R.string.main_activity__settings_period_length), periodLength);
            editor.putInt(getString(R.string.main_activity__settings_max_throw), maxThrow);
            editor.putInt(getString(R.string.main_activity__settings_min_throw), minThrow);
            editor.putInt(getString(R.string.main_activity__settings_number_of_jugglers), numberOfJugglers);
            editor.putInt(getString(R.string.main_activity__settings_max_results), maxResults);
            editor.putInt(getString(R.string.main_activity__settings_timeout), timeout);
            editor.putBoolean(getString(R.string.main_activity__settings_is_zips), isZips);
            editor.putBoolean(getString(R.string.main_activity__settings_is_zaps), isZaps);
            editor.putBoolean(getString(R.string.main_activity__settings_is_holds), isHolds);

            editor.commit();
        }
        catch (NumberFormatException e) {
            Toast.makeText(this, getString(R.string.main_activity__invalid_input_value),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void addFilterToListView(Filter filter) {
        LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.filter_item_view, null);
        TextView filterView = (TextView) view.findViewById(R.id.filter_text_view);
        filterView.setText(filter.toString());
        mFilterListLayout.addView(view);
    }

    private void removeFilterFromListView(int filterIndex) {
        mFilterListLayout.removeViewAt(filterIndex);
    }

    private void updateFilterListView() {
        mFilterListLayout.removeAllViews();
        for(Filter filter: mFilterList) {
            addFilterToListView(filter);
        }
    }

    public void generateSiteswaps(View view) {
        Intent intent = new Intent(this, ShowSiteswaps.class);

        try {
            int numberOfObjects = Integer.valueOf(mNumberOfObjects.getText().toString());
            int periodLength = Integer.valueOf(mPeriodLength.getText().toString());
            int maxThrow = Integer.valueOf(mMaxThrow.getText().toString());
            int minThrow = Integer.valueOf(mMinThrow.getText().toString());
            int numberOfJugglers = Integer.valueOf(mNumberOfJugglers.getText().toString());
            int maxResults = Integer.valueOf(mMaxResults.getText().toString());
            int timeout = Integer.valueOf(mTimeout.getText().toString());


            SiteswapGenerator siteswapGenerator = new SiteswapGenerator(periodLength, maxThrow,
                    minThrow, numberOfObjects, numberOfJugglers, mFilterList);
            siteswapGenerator.setMaxResults(maxResults);
            siteswapGenerator.setTimeoutSeconds(timeout);

            intent.putExtra(getString(R.string.intent__siteswap_generator), siteswapGenerator);
            startActivity(intent);

        }
        catch (NumberFormatException e) {
            Toast.makeText(this, getString(R.string.main_activity__invalid_input_value),
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void onCheckboxClicked(View view) {

        try {
            boolean checked = ((CheckBox) view).isChecked();
            int numberOfJugglers = Integer.valueOf(mNumberOfJugglers.getText().toString());

            switch (view.getId()) {
                case R.id.include_zips_checkbox:
                    if (checked)
                        Filter.addZips(mFilterList, numberOfJugglers);
                    else
                        Filter.removeZips(mFilterList, numberOfJugglers);
                    break;
                case R.id.include_zaps_checkbox:
                    if (checked)
                        Filter.addZaps(mFilterList, numberOfJugglers);
                    else
                        Filter.removeZaps(mFilterList, numberOfJugglers);
                    break;
                case R.id.include_holds_checkbox:
                    if (checked)
                        Filter.addHolds(mFilterList, numberOfJugglers);
                    else
                        Filter.removeHolds(mFilterList, numberOfJugglers);
                    break;
            }
            updateFilterListView();
        }
        catch (NumberFormatException e) {
        }

    }

    private void updateAutoFilters() {
        try {
            int numberOfJugglers = Integer.valueOf(mNumberOfJugglers.getText().toString());
            Filter.addDefaultFilters(mFilterList, numberOfJugglers);
            onCheckboxClicked(mZipsCheckbox);   // Updates the filter list corresponding to checkbox
            onCheckboxClicked(mZapsCheckbox);   // Updates the filter list corresponding to checkbox
            onCheckboxClicked(mHoldsCheckbox);  // Updates the filter list corresponding to checkbox
            updateFilterListView();
        }
        catch (NumberFormatException e) {
        }
    }
}
