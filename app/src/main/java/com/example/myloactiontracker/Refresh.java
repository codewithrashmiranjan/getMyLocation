package com.example.myloactiontracker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;
import android.util.Log;

public class Refresh extends AppCompatActivity {

    SwipeRefreshLayout swipeRefreshLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_refresh);

        swipeRefreshLayout = findViewById(R.id.swiperefresh);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
               // Log.i(LOG_TAG, "onRefresh called from SwipeRefreshLayout");

                // This method performs the actual data-refresh operation.
                // The method calls setRefreshing(false) when it's finished.

                stopLoading();
                Log.d("TAG", "onRefresh:onRefresh called from SwipeRefreshLayout ");
            }

            private void stopLoading() {

                swipeRefreshLayout.setRefreshing(false);

                            }
        });


    }
}