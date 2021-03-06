package com.pyz.audiosample.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.google.android.material.navigation.NavigationView;
import com.pyz.audiosample.R;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
	private final static String TAG = "MainActivity";

	public static final int REQ_CODE_RECORD_AUDIO = 303;
	public static final int REQ_CODE_WRITE_EXTERNAL_STORAGE = 404;

	private DrawerLayout drawerLayout;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);

		initDrawerAndNav();
		if(checkRecordPermission()){
			checkStoragePermission();
		}
	}

	private void initDrawerAndNav() {
		drawerLayout = findViewById(R.id.drawer_layout);
		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout,
				toolbar,
				R.string.navigation_drawer_open,
				R.string.navigation_drawer_close);

		drawerLayout.addDrawerListener(toggle);
		toggle.syncState();
		NavigationView navigationView = findViewById(R.id.nav_view);
		navigationView.setNavigationItemSelectedListener(this);
	}

	private boolean checkRecordPermission() {
		if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
				requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, REQ_CODE_RECORD_AUDIO);
				return false;
			}
		}
		return true;
	}

	private boolean checkStoragePermission() {
		if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
				requestPermissions(
						new String[]{
								Manifest.permission.WRITE_EXTERNAL_STORAGE,
								Manifest.permission.READ_EXTERNAL_STORAGE},
						REQ_CODE_WRITE_EXTERNAL_STORAGE);
				return false;
			}
		}
		return true;
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		if (requestCode == REQ_CODE_RECORD_AUDIO && grantResults.length > 0) {
			if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
				checkStoragePermission();
			} else {
				Log.i(TAG, "REQ_CODE_RECORD_AUDIO");
				finish();
			}
		}
		if ((requestCode == REQ_CODE_WRITE_EXTERNAL_STORAGE && grantResults.length > 0)
				&& (grantResults[0] == PackageManager.PERMISSION_DENIED
				|| grantResults[1] == PackageManager.PERMISSION_DENIED)) {
			Log.i(TAG, "REQ_CODE_WRITE_EXTERNAL_STORAGE");
			finish();
		}
	}

	@Override
	public boolean onNavigationItemSelected(@NonNull MenuItem item) {
		return false;
	}
}