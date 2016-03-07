package com.oym.indoor.navigation;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.AppCompatDialogFragment;
import android.support.v7.widget.AppCompatRadioButton;
import android.support.v7.widget.SwitchCompat;
import android.text.InputType;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.oym.indoor.Utils;
import com.oym.indoor.Values;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by joan on 14/07/15.
 */
public class FragmentUserProfile extends Fragment  {

    private GlobalState gs;

    private RelativeLayout tableLayout;
    private LinearLayout emptyLayout;
    private ImageView emptyImage;
    private TableLayout table;
    private FloatingActionButton fab;

    private HashMap<String, View> values = new HashMap<>();
    private HashMap<String, View> valuesNav = new HashMap<>();


    public static class DatePickerFragment extends AppCompatDialogFragment implements DatePickerDialog.OnDateSetListener {

        EditText textView;

        public DatePickerFragment() {
            super();
        }

        public DatePickerFragment(EditText tv) {
            super();
            textView = tv;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar c = Calendar.getInstance();
            int year, month, day;

            try {
                String dateStr = textView.getText().toString() + "T00:00:00.000Z";
                Date date = Utils.getDateFromIso8601UTCString(dateStr);
                c.setTime(date);
            } catch (Exception exc) {
                Log.e("DatePickerFragment", "Wrong date, taking current", exc);
            } finally {
                year = c.get(Calendar.YEAR);
                month = c.get(Calendar.MONTH);
                day = c.get(Calendar.DAY_OF_MONTH);
            }

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        @Override
        public void onDateSet(DatePicker datePicker, int year, int m, int day) {
            int month = m + 1;
            String date = String.format("%04d-%02d-%02d", year, month, day);
            textView.setText(date);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_userprofile, container, false);

        tableLayout = (RelativeLayout) view.findViewById(R.id.FUPTableLayout);
        emptyLayout = (LinearLayout) view.findViewById(R.id.FUPEmptyLayout);
        emptyImage = (ImageView) view.findViewById(R.id.FUPEmptyImage);
        emptyImage.setColorFilter(new LightingColorFilter(Color.WHITE, getResources().getColor(R.color.greyLight)));
        table = (TableLayout) view.findViewById(R.id.FUPTable);
        fab = (FloatingActionButton) view.findViewById(R.id.FUPFab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fabClick();
            }
        });
        showViewAnimated(fab);

        //setHasOptionsMenu(true);  It uses FAB

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_userprofile, menu);
        super.onCreateOptionsMenu(menu,inflater);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        gs = (GlobalState) getActivity().getApplication();

        ContextThemeWrapper ctw = new ContextThemeWrapper(gs, R.style.AppTheme);
        TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, 1);
        boolean isDataAvailable = false;

        if (!gs.getGoIndoor().getSettings().users.isEmpty()) {
            isDataAvailable = true;
            table.addView(getTitleView(R.string.FUPUser, lp));

            for (String key : gs.getGoIndoor().getSettings().users.keySet()) {
                try {
                    TableRow tr = new TableRow(gs);
                    tr.setLayoutParams(new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 3));
                    TextView tvKey = new TextView(gs);
                    tvKey.setText(key);
                    tvKey.setTextAppearance(gs, android.support.v7.appcompat.R.style.TextAppearance_AppCompat_Body1);
                    tvKey.setTextColor(getResources().getColor(R.color.textColor));
                    tvKey.setLayoutParams(lp);
                    tvKey.setPaddingRelative(0, 0, 64, 0);
                    tvKey.setGravity(GravityCompat.END);
                    Values.Settings.UserValue value = gs.getGoIndoor().getLogger().getStatsProp(key);
                    if (value == null) {
                        continue;
                    }
                    View valueView = processValue(key, value, ctw, lp);
                    tr.addView(tvKey);
                    tr.addView(valueView);
                    table.addView(tr);
                    values.put(key, valueView);
                } catch (Exception exc) {

                }
            }
        }

        if (!gs.getGoIndoor().getSettings().edges.isEmpty()) {
            isDataAvailable = true;
            table.addView(getTitleView(R.string.FUPNavigation, lp));

            for (String key : gs.getGoIndoor().getSettings().edges.keySet()) {
                try {
                    TableRow tr = new TableRow(gs);
                    tr.setLayoutParams(new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 3));
                    TextView tvKey = new TextView(gs);
                    tvKey.setText(key);
                    tvKey.setTextAppearance(gs, android.support.v7.appcompat.R.style.TextAppearance_AppCompat_Body1);
                    tvKey.setTextColor(getResources().getColor(R.color.textColor));
                    tvKey.setLayoutParams(lp);
                    tvKey.setPaddingRelative(0, 0, 64, 0);
                    tvKey.setGravity(GravityCompat.END);
                    Values.Settings.UserValue value = gs.getGoIndoor().getLogger().getNavProp(key);
                    if (value == null) {
                        continue;
                    }
                    View valueView = processValue(key, value, ctw, lp);
                    tr.addView(tvKey);
                    tr.addView(valueView);
                    table.addView(tr);
                    valuesNav.put(key, valueView);
                } catch (Exception exc) {

                }
            }
        }

        if (!isDataAvailable) {
            tableLayout.setVisibility(View.GONE);
            emptyLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroyView() {
        hideKeyboard();
        super.onDestroyView();
    }

    private void showViewAnimated(View view) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 0, 1);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 0, 1);
        AnimatorSet animSetXY = new AnimatorSet();
        animSetXY.playTogether(scaleX, scaleY);
        animSetXY.setInterpolator(new OvershootInterpolator());
        animSetXY.setDuration(getResources().getInteger(android.R.integer.config_longAnimTime));
        animSetXY.start();
        view.setVisibility(View.VISIBLE);
    }

    void fabClick() {
        hideKeyboard();

        try {
            HashMap<String, Values.Settings.UserValue> updated = new HashMap<>();
            for (Map.Entry<String, View> entry: values.entrySet()) {
                updated.put(entry.getKey(), getValue(entry, gs.getGoIndoor().getSettings().users));
            }
            gs.getGoIndoor().getLogger().putStatsProp(updated);

            updated = new HashMap<>();
            for (Map.Entry<String, View> entry: valuesNav.entrySet()) {
                updated.put(entry.getKey(), getValue(entry, gs.getGoIndoor().getSettings().edges));
            }
            gs.getGoIndoor().getLogger().putNavProp(updated);

            Toast.makeText(gs, R.string.FUPSaved, Toast.LENGTH_LONG).show();
        } catch (Exception exc) {
            Toast.makeText(gs, R.string.FUPNotSaved, Toast.LENGTH_LONG).show();
        }


    }

    private void hideKeyboard() {
        try {
            View view = getView();
            InputMethodManager imm = (InputMethodManager) gs.getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        } catch (Exception exc) {

        }
    }

    private View getTitleView(@StringRes int text, TableRow.LayoutParams lp) {
        TextView titleUsers = new TextView(gs);
        TableRow trUsers = new TableRow(gs);
        trUsers.setLayoutParams(new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        titleUsers.setText(text);
        titleUsers.setTextAppearance(gs, android.support.v7.appcompat.R.style.TextAppearance_AppCompat_Title);
        titleUsers.setTextColor(getResources().getColor(R.color.textColor));
        titleUsers.setLayoutParams(lp);
        return titleUsers;
    }

    private View processValue(String key, Values.Settings.UserValue value, ContextThemeWrapper ctw, TableRow.LayoutParams lp) {
        View valueView = null;
        switch (value.format) {
            case BOOLEAN:
                SwitchCompat cb = new SwitchCompat(ctw);
                cb.setChecked(Boolean.parseBoolean(value.toString()));
                cb.setLayoutParams(lp);
                cb.setGravity(GravityCompat.START);
                valueView = cb;
                break;
            case DATE:
                final EditText tvDateStr = new EditText(ctw);
                tvDateStr.setText(value.toString().split("T")[0]);
                tvDateStr.setSingleLine();
                tvDateStr.setTextAppearance(gs, android.support.v7.appcompat.R.style.TextAppearance_AppCompat_Body1);
                tvDateStr.setTextColor(getResources().getColor(R.color.textColor));
                tvDateStr.setLayoutParams(lp);
                tvDateStr.setFocusable(false);
                tvDateStr.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        hideKeyboard();
                        DialogFragment newFragment = new DatePickerFragment(tvDateStr);
                        newFragment.show(getActivity().getSupportFragmentManager(), "datePicker");
                    }
                });
                valueView = tvDateStr;
                break;
            case STRING:
                EditText tvStr = new EditText(ctw);
                tvStr.setText(value.toString());
                tvStr.setSingleLine();
                tvStr.setTextAppearance(gs, android.support.v7.appcompat.R.style.TextAppearance_AppCompat_Body1);
                tvStr.setTextColor(getResources().getColor(R.color.textColor));
                tvStr.setLayoutParams(lp);
                valueView = tvStr;
                break;
            case NUMBER:
                EditText etNum = new EditText(ctw);
                etNum.setText(value.toString());
                etNum.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
                etNum.setSingleLine();
                etNum.setTextAppearance(gs, android.support.v7.appcompat.R.style.TextAppearance_AppCompat_Body1);
                etNum.setTextColor(getResources().getColor(R.color.textColor));
                etNum.setLayoutParams(lp);
                valueView = etNum;
                break;
            case FIXED:
                ArrayList<String> vals = gs.getGoIndoor().getSettings().users.get(key).fixedValues;
                RadioGroup rg = new RadioGroup(ctw);
                rg.setOrientation(RadioGroup.VERTICAL);
                for (int i = 0; i < vals.size(); i++) {
                    AppCompatRadioButton rb = new AppCompatRadioButton(ctw);
                    rb.setId(i);
                    rb.setText(vals.get(i));
                    rg.addView(rb);
                    if (Integer.parseInt(value.toString()) == i) {
                        rg.check(i);
                    }
                }
                rg.setLayoutParams(lp);
                rg.setGravity(GravityCompat.START);
                valueView = rg;
                break;

        }
        return valueView;
    }

    private Values.Settings.UserValue getValue(Map.Entry<String, View> entry, HashMap<String, Values.Settings.UserValue> settingsMap) {
        Values.Settings.UserValue res = null;
        Values.Settings.UserValue sv = settingsMap.get(entry.getKey());
        switch (sv.format) {
            case BOOLEAN:
                res = new Values.Settings.UserValue<>(((SwitchCompat)entry.getValue()).isChecked(), Values.Format.BOOLEAN);
                break;
            case DATE:
                String currentDate = ((EditText)entry.getValue()).getText().toString();
                currentDate += "T00:00:00.000Z";
                res = new Values.Settings.UserValue<>(currentDate, Values.Format.DATE);
                break;
            case NUMBER:
                res = new Values.Settings.UserValue<>(Double.parseDouble(((EditText) entry.getValue()).getText().toString()), Values.Format.NUMBER);
                break;
            case FIXED:
                res = new Values.Settings.UserValue<>((((RadioGroup) entry.getValue()).getCheckedRadioButtonId()), Values.Format.FIXED, sv.fixedValues);
                break;
            case STRING:
                res = new Values.Settings.UserValue<>(((EditText)entry.getValue()).getText().toString(), Values.Format.STRING);
                break;
        }
        return res;
    }

}
