package com.yasnosean.schoolprojectapp.activities;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.yasnosean.schoolprojectapp.R;
import com.yasnosean.schoolprojectapp.adapters.SectionsAdapter;
import com.yasnosean.schoolprojectapp.models.TestService;

/**
 * The main page that holds the posts fragment section
 *
 * I wanted to make also settings and notifications sections
 * but I changed my mind at the end.
 */
public class MainActivity extends AppCompatActivity {

    private ViewPager viewPager;
    private EditText toolbarSearch;
    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SectionsAdapter pagerAdapter = new SectionsAdapter(getSupportFragmentManager());

        viewPager = findViewById(R.id.pager);
        toolbarSearch = findViewById(R.id.main_toolbar_searchText);

        viewPager.setAdapter(pagerAdapter);

        tabLayout = findViewById(R.id.tabLayout);
        tabLayoutManagement();

        toolbarSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, SearchActivity.class));
            }
        });

    }

    private void tabLayoutManagement() {
        tabLayout.setupWithViewPager(viewPager);

        tabLayout.getTabAt(0).setIcon(R.drawable.posts_page_icon_fill);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        tab.setIcon(R.drawable.posts_page_icon_fill);
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        tab.setIcon(R.drawable.posts_page_icon_no_fill);
                        break;
                    case 1:
                        tab.setIcon(R.drawable.more_options_page_icon_no_fill);
                        break;
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

}
