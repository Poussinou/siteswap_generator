package namlit.siteswapgenerator;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.CheckBox;
import android.text.TextWatcher;
import android.text.Editable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.LinkedList;
import java.util.Locale;

import siteswaplib.*;

public class MainActivity extends AppCompatActivity
        implements AddFilterDialog.FilterDialogListener {

    final static int PATTERN_FILTER_ITEM_NUMBER = 0;

    private LinkedList<Filter> mFilterList;

    private EditText mNumberOfObjects;
    private EditText mPeriodLength;
    private EditText mMaxThrow;
    private EditText mMinThrow;
    private EditText mNumberOfJugglers;
    private EditText mMaxResults;
    private EditText mTimeout;
    private CheckBox mRandomGenerationModeCheckbox;
    private CheckBox mZipsCheckbox;
    private CheckBox mZapsCheckbox;
    private CheckBox mHoldsCheckbox;
    private Spinner mFilterTypeSpinner;
    private NonScrollListView mFilterListView;
    private ArrayAdapter<Filter> mFilterListAdapter;

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
        mFilterTypeSpinner  = (Spinner) findViewById(R.id.filter_type_spinner);
        mFilterListView     = (NonScrollListView) findViewById(R.id.filter_list);
        mRandomGenerationModeCheckbox = (CheckBox) findViewById(R.id.random_generation_mode_checkbox);

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
        boolean isRandomGenerationMode = sharedPref.getBoolean(
                getString(R.string.main_activity__settings_is_random_generation_mode), false);
        int filterSpinnerPosition = sharedPref.getInt(getString(R.string.main_activity__settings_filter_spinner_position), 0);
        String serializedFilterList = sharedPref.getString(getString(R.string.main_activity__settings_filter_list), "");

        if (serializedFilterList != "") {
            try {
                byte b[] = Base64.decode(serializedFilterList, Base64.DEFAULT);
                ByteArrayInputStream bi = new ByteArrayInputStream(b);
                ObjectInputStream si = new ObjectInputStream(bi);
                mFilterList = (LinkedList<Filter>) si.readObject();
                si.close();
            } catch (Exception e) {
                Toast.makeText(this, getString(R.string.main_activity__deserialization_error_toast),
                        Toast.LENGTH_SHORT).show();
            }
        }


        mNumberOfObjects.setText(String.valueOf(numberOfObjects));
        mPeriodLength.setText(String.valueOf(periodLength));
        mMaxThrow.setText(String.valueOf(maxThrow));
        mMinThrow.setText(String.valueOf(minThrow));
        mNumberOfJugglers.setText(String.valueOf(numberOfJugglers));
        mMaxResults.setText(String.valueOf(maxResults));
        mTimeout.setText(String.valueOf(timeout));
        mRandomGenerationModeCheckbox.setChecked(isRandomGenerationMode);
        mZipsCheckbox.setChecked(isZips);
        mZapsCheckbox.setChecked(isZaps);
        mHoldsCheckbox.setChecked(isHolds);
        mFilterTypeSpinner.setSelection(filterSpinnerPosition);


        mFilterListAdapter = new ArrayAdapter<Filter>(
                this, android.R.layout.simple_list_item_1, mFilterList);
        mFilterListView.setAdapter(mFilterListAdapter);
        mFilterListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Filter filter = (Filter) parent.getItemAtPosition(position);

                try {
                    int periodLength = Integer.valueOf(mPeriodLength.getText().toString());
                    int maxThrow = Integer.valueOf(mMaxThrow.getText().toString());
                    int minThrow = Integer.valueOf(mMinThrow.getText().toString());
                    int numberOfJugglers = Integer.valueOf(mNumberOfJugglers.getText().toString());

                    if (filter instanceof NumberFilter) {

                            new NumberFilterDialog().show(getSupportFragmentManager(),
                                    getString(R.string.number_filter__dialog_tag),
                                    minThrow, maxThrow, periodLength, filter);
                    }
                    else if (filter instanceof PatternFilter)
                        new PatternFilterDialog().show(getSupportFragmentManager(),
                                getString(R.string.pattern_filter__dialog_tag), numberOfJugglers, filter);

                } catch (NumberFormatException e) {
                    Toast.makeText(getApplicationContext(), getString(R.string.main_activity__invalid_input_value),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

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
                    mFilterListAdapter.notifyDataSetChanged();
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_about)
        {
            showAboutDialog();
        }
        else if (id == R.id.action_help) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(Html.fromHtml(getString(R.string.help_activity__help_html_text)))
                    .setNeutralButton(getString(R.string.back), null);
            builder.create().show();
        }

        return super.onOptionsItemSelected(item);
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
            boolean isRandomGenerationMode = mRandomGenerationModeCheckbox.isChecked();
            boolean isZips = mZipsCheckbox.isChecked();
            boolean isZaps = mZapsCheckbox.isChecked();
            boolean isHolds = mHoldsCheckbox.isChecked();
            int filterSpinnerPosition = mFilterTypeSpinner.getSelectedItemPosition();

            editor.putInt(getString(R.string.main_activity__settings_number_of_objects), numberOfObjects);
            editor.putInt(getString(R.string.main_activity__settings_period_length), periodLength);
            editor.putInt(getString(R.string.main_activity__settings_max_throw), maxThrow);
            editor.putInt(getString(R.string.main_activity__settings_min_throw), minThrow);
            editor.putInt(getString(R.string.main_activity__settings_number_of_jugglers), numberOfJugglers);
            editor.putInt(getString(R.string.main_activity__settings_max_results), maxResults);
            editor.putInt(getString(R.string.main_activity__settings_timeout), timeout);
            editor.putBoolean(getString(R.string.main_activity__settings_is_random_generation_mode), isRandomGenerationMode);
            editor.putBoolean(getString(R.string.main_activity__settings_is_zips), isZips);
            editor.putBoolean(getString(R.string.main_activity__settings_is_zaps), isZaps);
            editor.putBoolean(getString(R.string.main_activity__settings_is_holds), isHolds);
            editor.putInt(getString(R.string.main_activity__settings_filter_spinner_position), filterSpinnerPosition);

            try {
                ByteArrayOutputStream bo = new ByteArrayOutputStream();
                ObjectOutputStream so = new ObjectOutputStream(bo);
                so.writeObject(mFilterList);
                so.close();
                editor.putString(getString(R.string.main_activity__settings_filter_list), Base64.encodeToString(bo.toByteArray(), Base64.DEFAULT));
            } catch (Exception e) {
                Toast.makeText(this, getString(R.string.main_activity__serialization_error_toast),
                        Toast.LENGTH_SHORT).show();
            }

            editor.commit();
        }
        catch (NumberFormatException e) {
            Toast.makeText(this, getString(R.string.main_activity__invalid_input_value),
                    Toast.LENGTH_SHORT).show();
        }
    }



    public void addFilter(View view) {

        try {
            int periodLength = Integer.valueOf(mPeriodLength.getText().toString());
            int maxThrow = Integer.valueOf(mMaxThrow.getText().toString());
            int minThrow = Integer.valueOf(mMinThrow.getText().toString());
            int numberOfJugglers = Integer.valueOf(mNumberOfJugglers.getText().toString());

            if (mFilterTypeSpinner.getSelectedItemPosition() == PATTERN_FILTER_ITEM_NUMBER) {

                new PatternFilterDialog().show(getSupportFragmentManager(),
                        getString(R.string.pattern_filter__dialog_tag), numberOfJugglers);
            } else {
                new NumberFilterDialog().show(getSupportFragmentManager(),
                        getString(R.string.number_filter__dialog_tag), minThrow, maxThrow, periodLength);

            }
        }
        catch (NumberFormatException e) {
            Toast.makeText(this, getString(R.string.main_activity__invalid_input_value),
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void resetFilters(View view) {
        mFilterList.clear();
        updateAutoFilters();
    }


    public void onAddSiteswapFilter(Filter filter)
    {
        if (!mFilterList.contains(filter))
            mFilterList.add(filter);
        mFilterListAdapter.notifyDataSetChanged();
    }

    public void onRemoveSiteswapFilter(Filter filter)
    {
        // Remove all occurences of Filter
        while (mFilterList.remove(filter))
            ;
        mFilterListAdapter.notifyDataSetChanged();
    }

    public void onChangeSiteswapFilter(Filter oldFilter, Filter newFilter)
    {
        onRemoveSiteswapFilter(oldFilter);
        onAddSiteswapFilter(newFilter);
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
            boolean isRandomGenerationMode = mRandomGenerationModeCheckbox.isChecked();


            SiteswapGenerator siteswapGenerator = new SiteswapGenerator(periodLength, maxThrow,
                    minThrow, numberOfObjects, numberOfJugglers, mFilterList);
            siteswapGenerator.setMaxResults(maxResults);
            siteswapGenerator.setTimeoutSeconds(timeout);
            siteswapGenerator.setRandomGeneration(isRandomGenerationMode);

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
            mFilterListAdapter.notifyDataSetChanged();
        }
        catch (NumberFormatException e) {
        }

    }

    private void updateAutoFilters() {
        try {
            int numberOfJugglers = Integer.valueOf(mNumberOfJugglers.getText().toString());
            int minThrow = Integer.valueOf(mMinThrow.getText().toString());
            Filter.addDefaultFilters(mFilterList, numberOfJugglers, minThrow);
            onCheckboxClicked(mZipsCheckbox);   // Updates the filter list corresponding to checkbox
            onCheckboxClicked(mZapsCheckbox);   // Updates the filter list corresponding to checkbox
            onCheckboxClicked(mHoldsCheckbox);  // Updates the filter list corresponding to checkbox
            mFilterListAdapter.notifyDataSetChanged();
        }
        catch (NumberFormatException e) {
        }
    }


    private void showAboutDialog()
    {

        try
        {

            PackageManager manager = getPackageManager();
            PackageInfo info = manager.getPackageInfo(getPackageName(), 0);
            String appVersion = info.versionName;

            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            View view = getLayoutInflater().inflate(R.layout.layout_about_page, null);
            TextView appNameVersion = (TextView) view.findViewById(R.id.appNameVersion);
            appNameVersion.setText(getString(R.string.app_name) + " " + appVersion);

            builder.setView(view);
            builder.setNeutralButton(getString(R.string.back), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();

        } catch (Throwable t) {

            t.printStackTrace();
        }
    }
}
