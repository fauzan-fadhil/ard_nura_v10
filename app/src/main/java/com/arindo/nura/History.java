package com.arindo.nura;

import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TableLayout;
import android.support.v4.view.ViewPager;

/**
 * Created by bmaxard on 13/01/2017.
 */

public class History extends AppCompatActivity {
    private String tmDevice, versi, textsql;
    private SqlHelper dbHelper;
    private Cursor cursor;
    private int listindex;
    private String[] arr;
    private String SERVERADDR, MyJSON;
    private TableLayout tableHistory;
    private int timeoutdata = 30000;
    SwipeRefreshLayout swLayout;
    Toolbar toolbar;
    TabLayout tabLayout;
    ViewPager viewPager;
    int tabposition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_history);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setHomeButtonEnabled(true);
        this.setTitle("Histori");

        //TelephonyInfo telephonyInfo = TelephonyInfo.getInstance(this);
        //tmDevice = telephonyInfo.getImsiSIM1();
        dbHelper = new SqlHelper(this);
        //versi = BuildConfig.VERSION_NAME;
        versi = String.valueOf(BuildConfig.VERSION_CODE);

        tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText("ORDER"));
        tabLayout.addTab(tabLayout.newTab().setText("PROSES"));
        tabLayout.addTab(tabLayout.newTab().setText("SELESAI"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        viewPager = (ViewPager) findViewById(R.id.pager);

        ShowTab();

        swLayout = (SwipeRefreshLayout) findViewById(R.id.swlayout);
        swLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                ShowTab();
                swLayout.setRefreshing(false);
            }
        });
    }

    private void ShowTab(){
        viewPager.removeAllViews();
        final PagerAdapter adapter = new PagerAdapter (getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        viewPager.setCurrentItem(tabposition);
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
                tabposition = tab.getPosition();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
