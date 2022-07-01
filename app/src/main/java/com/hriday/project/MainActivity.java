package com.hriday.project;

import static android.app.AppOpsManager.MODE_ALLOWED;
import static android.app.AppOpsManager.OPSTR_GET_USAGE_STATS;

import android.app.AppOpsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.tabs.TabLayout;

import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;

public class MainActivity extends AppCompatActivity {

    private ViewPagerAdapter viewPagerAdapter;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private AppBarLayout topBar;
    private Button enableBtn;
    private Button keyboardEnableBtn;
    private TextView permissionDescriptionTv;
    private TextView keyboardPermissionDescriptionTv;
    private ImageButton reloadBtn;
    private AndroidDatabaseManager AndroidDatabaseManager;
    private MoodPrediction moodPredictionFragment;
    private ConstraintLayout constraintLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        viewPager = findViewById(R.id.view_pager);

        // setting up the adapter
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        addDatabaseSentence();
        // add the fragments
        moodPredictionFragment =new MoodPrediction();
        AndroidDatabaseManager =new AndroidDatabaseManager();
        viewPagerAdapter.add(AndroidDatabaseManager,"Text Database");
        viewPagerAdapter.add(moodPredictionFragment,"Average Text Tone");
        //viewPagerAdapter.add(UsageStatsFragment, "Usage Stats");
        //viewPagerAdapter.add(appCategoryStatsFragment, "App Category Stats");
        // Set the adapter
        viewPager.setAdapter(viewPagerAdapter);

        // The Page (fragment) titles will be displayed in the
        // tabLayout hence we need to  set the page viewer
        // we use the setupWithViewPager().
        tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        constraintLayout=findViewById(R.id.constraintLayout);

        enableBtn=findViewById(R.id.enable_btn);
        topBar=findViewById((R.id.appBarLayout2));
        permissionDescriptionTv=findViewById(R.id.permission_description_tv);
        keyboardPermissionDescriptionTv=findViewById(R.id.keyboard_permission_description_tv);
        keyboardEnableBtn=findViewById(R.id.keyboard_enable_btn);
        reloadBtn=findViewById(R.id.reloadButton);

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (getGrantStatus() && isKeyboardAccessGranted()) {
            //showHideWithPermission();
            showHideWithBothPermission();
            reloadBtn.setOnClickListener(view->setReloadBtn());


        } else if(!isKeyboardAccessGranted()){
            //showHideNoPermission();
            showHideNoKeyboardPermission();
            keyboardEnableBtn.setOnClickListener(view -> startActivity(new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)));

        }else {
            //showHideNoPermission();
            showHideNoStatsPermission();
            enableBtn.setOnClickListener(view -> startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)));
        }

    }

    private String Foreground_app(int x) {

        UsageStatsManager usm = (UsageStatsManager) this.getSystemService(Context.USAGE_STATS_SERVICE);

        String currentApp = "NULL";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            long time = System.currentTimeMillis();
            List<UsageStats> appList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000 * 1000, time);
            if (appList != null && appList.size() > 0) {
                SortedMap<Long, UsageStats> mySortedMap = new TreeMap<>();
                for (UsageStats usageStats : appList) {
                    mySortedMap.put(usageStats.getLastTimeUsed(), usageStats);
                }
                if (x == 1) {
                    mySortedMap.remove(mySortedMap.lastKey());
                }
                if (!mySortedMap.isEmpty()) {
                    currentApp = Objects.requireNonNull(mySortedMap.get(mySortedMap.lastKey())).getPackageName();
                }
            }
        }

        return currentApp;
    }

    private void addDatabaseSentence(){
        if(!((ApplicationClass) this.getApplication()).getDatabase_sentence().isEmpty()){
            String appname = Foreground_app(1);
            ((ApplicationClass)this.getApplication()).getDB().insertuserdata(new DataTyped("Entered", ((ApplicationClass) this.getApplication()).getDatabase_sentence(), appname));
            ((ApplicationClass) this.getApplication()).setDatabase_sentence("");
        }
    }

    private void setReloadBtn(){
        /*if(UsageStatsFragment.getUserVisibleHint()){
            UsageStatsFragment.loadStatistics();
        }
        else if(appCategoryStatsFragment.getUserVisibleHint()){
            appCategoryStatsFragment.loadStatistics();
        }*/
        addDatabaseSentence();
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        moodPredictionFragment =new MoodPrediction();
        AndroidDatabaseManager =new AndroidDatabaseManager();
        viewPagerAdapter.add(AndroidDatabaseManager,"Text Database");
        viewPagerAdapter.add(moodPredictionFragment,"Average Text Tone");
        //viewPagerAdapter.add(UsageStatsFragment, "Usage Stats");
        //viewPagerAdapter.add(appCategoryStatsFragment, "App Category Stats");
        // Set the adapter
        viewPager.setAdapter(viewPagerAdapter);
    }
    /*public void loadStatistics() {
        UsageStatsManager usm = (UsageStatsManager) this.getSystemService(USAGE_STATS_SERVICE);
        List<UsageStats> appList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,  System.currentTimeMillis()- 1000*3600*24 ,  System.currentTimeMillis());
        appList = appList.stream().filter(app -> app.getTotalTimeInForeground() > 0).collect(Collectors.toList());

        // Group the usageStats by application and sort them by total time in foreground
        if (appList.size() > 0) {
            Map<String, UsageStats> mySortedMap = new TreeMap<>();
            for (UsageStats usageStats : appList) {
                mySortedMap.put(usageStats.getPackageName(), usageStats);
            }
            showAppsUsage(mySortedMap);
        }
    }


    public void showAppsUsage(Map<String, UsageStats> mySortedMap) {
        //public void showAppsUsage(List<UsageStats> usageStatsList) {
        ArrayList<App> appsList = new ArrayList<>();
        List<UsageStats> usageStatsList = new ArrayList<>(mySortedMap.values());

        // sort the applications by time spent in foreground
        Collections.sort(usageStatsList, (z1, z2) -> Long.compare(z1.getTotalTimeInForeground(), z2.getTotalTimeInForeground()));

        // get total time of apps usage to calculate the usagePercentage for each app
        long totalTime = usageStatsList.stream().map(UsageStats::getTotalTimeInForeground).mapToLong(Long::longValue).sum();

        //fill the appsList
        for (UsageStats usageStats : usageStatsList) {
            try {
                String packageName = usageStats.getPackageName();
                Drawable icon = getDrawable(R.drawable.no_image);
                String[] packageNames = packageName.split("\\.");
                String appName = packageNames[packageNames.length-1].trim();


                if(isAppInfoAvailable(usageStats)){
                    ApplicationInfo ai = getApplicationContext().getPackageManager().getApplicationInfo(packageName, 0);
                    icon = getApplicationContext().getPackageManager().getApplicationIcon(ai);
                    appName = getApplicationContext().getPackageManager().getApplicationLabel(ai).toString();
                }

                String usageDuration = getDurationBreakdown(usageStats.getTotalTimeInForeground());
                int usagePercentage = (int) (usageStats.getTotalTimeInForeground() * 100 / totalTime);

                App usageStatDTO = new App(icon, appName, usagePercentage, usageDuration);
                appsList.add(usageStatDTO);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }


        // reverse the list to get most usage first
        Collections.reverse(appsList);
        // build the adapter
        AppsAdapter adapter = new AppsAdapter(this, appsList);


        // attach the adapter to a ListView
        //ListView listView = findViewById(R.id.apps_list);
        //listView.setAdapter(adapter);

    }

    private boolean isAppInfoAvailable(UsageStats usageStats) {
        try {
            getApplicationContext().getPackageManager().getApplicationInfo(usageStats.getPackageName(), 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private String getDurationBreakdown(long millis) {
        if (millis < 0) {
            throw new IllegalArgumentException("Duration must be greater than zero!");
        }

        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        millis -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        millis -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);

        return (hours + " h " +  minutes + " m " + seconds + " s");
    }*/

    private boolean getGrantStatus() {
        AppOpsManager appOps = (AppOpsManager) getApplicationContext()
                .getSystemService(Context.APP_OPS_SERVICE);

        int mode = appOps.checkOpNoThrow(OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), getApplicationContext().getPackageName());

        if (mode == AppOpsManager.MODE_DEFAULT) {
            return (getApplicationContext().checkCallingOrSelfPermission(android.Manifest.permission.PACKAGE_USAGE_STATS) == PackageManager.PERMISSION_GRANTED);
        } else {
            return (mode == MODE_ALLOWED);
        }
    }
    private boolean isKeyboardAccessGranted(){
        boolean granted=false;
        try {
            InputMethodManager immService = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            List<InputMethodInfo> lst =immService.getEnabledInputMethodList();
            for(int i=0;i< lst.size();i++){
                if(lst.get(i).getServiceName().equals("com.hriday.project.AITextToneKeyboard")){
                    granted=true;
                }
            }

        } catch (Exception e) {
            Log.e("TAG", "Exception in check if input method is enabled", e);
        }
        Log.i("HRIDAY123",Boolean.toString(granted));
        return(granted);
    }

    public void showHideNoStatsPermission() {
        constraintLayout.setVisibility(View.VISIBLE);
        enableBtn.setVisibility(View.VISIBLE);
        permissionDescriptionTv.setVisibility(View.VISIBLE);
        keyboardEnableBtn.setVisibility(View.GONE);
        keyboardPermissionDescriptionTv.setVisibility(View.GONE);
        topBar.setVisibility(View.GONE);
        viewPager.setVisibility(View.GONE);

    }
    public void showHideNoKeyboardPermission() {
        constraintLayout.setVisibility(View.VISIBLE);
        enableBtn.setVisibility(View.GONE);
        permissionDescriptionTv.setVisibility(View.GONE);
        keyboardEnableBtn.setVisibility(View.VISIBLE);
        keyboardPermissionDescriptionTv.setVisibility(View.VISIBLE);
        topBar.setVisibility(View.GONE);
        viewPager.setVisibility(View.GONE);

    }

    public void showHideWithBothPermission() {
        enableBtn.setVisibility(View.GONE);
        permissionDescriptionTv.setVisibility(View.GONE);
        keyboardEnableBtn.setVisibility(View.GONE);
        keyboardPermissionDescriptionTv.setVisibility(View.GONE);
        constraintLayout.setVisibility(View.GONE);
        topBar.setVisibility(View.VISIBLE);
        viewPager.setVisibility(View.VISIBLE);
    }
}