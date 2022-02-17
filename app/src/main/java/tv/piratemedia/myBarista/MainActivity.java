package tv.piratemedia.myBarista;

import android.Manifest;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.WindowManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import pub.devrel.easypermissions.EasyPermissions;
import tv.piratemedia.myBarista.connection.BLE;
import tv.piratemedia.myBarista.databinding.ActivityMainBinding;
import tv.piratemedia.myBarista.ui.app.AppViewModel;
import tv.piratemedia.myBarista.ui.settings.RemoteSettingsUtils;
import tv.piratemedia.myBarista.ui.settings.SettingsFragment;
import tv.piratemedia.myBarista.ui.settings.SettingsViewModel;

public class MainActivity extends AppCompatActivity implements
        PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    private AppBarConfiguration mAppBarConfiguration;
    private ExecutorService connectionExecutor;
    private Handler connectionHandler;
    private BLE bleConnection;
    private Runnable connectionRunnable;
    private boolean serviceRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_content_main);
        assert navHostFragment != null;
        NavController navController = navHostFragment.getNavController();

        NavigationView navigationView = binding.navView;
        if (navigationView != null) {
            mAppBarConfiguration = new AppBarConfiguration.Builder(
                    R.id.nav_app, R.id.nav_settings)
                    .setOpenableLayout(binding.drawerLayout)
                    .build();
            NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
            NavigationUI.setupWithNavController(navigationView, navController);
        }

        BottomNavigationView bottomNavigationView = binding.appBarMain.contentMain.bottomNavView;
        if (bottomNavigationView != null) {
            mAppBarConfiguration = new AppBarConfiguration.Builder(R.id.nav_app, R.id.nav_settings)
                    .build();
            NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
            NavigationUI.setupWithNavController(bottomNavigationView, navController);
        }

        createConnectionExecutor();
        RemoteSettingsUtils.setupDefaultPrefs(this);
    }

    @Override
    public boolean onPreferenceStartFragment(@NonNull PreferenceFragmentCompat caller, Preference pref) {
        // Instantiate the new Fragment
        SettingsFragment sf = (SettingsFragment) (getSupportFragmentManager().getPrimaryNavigationFragment().getChildFragmentManager().getFragments().get(0));
        sf.setPrefScreen(pref.getFragment());
        return true;
    }

    public void createConnectionExecutor() {
        RemoteSettingsUtils rsu = new RemoteSettingsUtils(this);
        connectionExecutor = Executors.newSingleThreadExecutor();
        connectionHandler = new ConnectionMessageHandler(
                AppViewModel::addTempInfo,
                AppViewModel::setPidPower,
                AppViewModel::setShotTime,
                rsu::storePreferenceInput,
                new ConnectionMessageHandler.OnStatusChange() {
                    @Override
                    public void onStatus(String status) {
                        AppViewModel.setConnectionStatus(status);
                    }
                    @Override
                    public void onDisconnect() {
                        stopService();
                        startServiceIfPermission();
                        disableScreenAlwaysOn();
                        SettingsViewModel.setConnected(false);
                    }
                    @Override
                    public void OnConnected() {
                        enableScreenAlwaysOn();
                        SettingsViewModel.setConnected(true);
                    }
                });
        bleConnection = new BLE(getApplicationContext(), connectionHandler);
        connectionRunnable = () -> bleConnection.discover();
    }

    public void sendRemotePrefValue(String name) {
        bleConnection.syncPref(name);
    }

    private void enableScreenAlwaysOn() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void disableScreenAlwaysOn() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private boolean checkPermissions() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            //on android 12+ we need to request scan permission
            if(EasyPermissions.hasPermissions(this, Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)) {
                return true;
            }
            EasyPermissions.requestPermissions(this, getString(R.string.rationale_scan_permission),501, Manifest.permission.BLUETOOTH_SCAN);
        } else {
            //on android <12 we need to request location permission
            if(EasyPermissions.hasPermissions(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                return true;
            }
            EasyPermissions.requestPermissions(this, getString(R.string.rationale_location_permission),501, Manifest.permission.ACCESS_FINE_LOCATION);
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        startServiceIfPermission();
    }

    @Override
    protected void onStart() {
        super.onStart();
        startServiceIfPermission();
    }

    private void startServiceIfPermission() {
        boolean hasPermissions = checkPermissions();
        if(hasPermissions && !serviceRunning) {
            connectionExecutor.execute(connectionRunnable);
            serviceRunning = true;
        }
    }

    private void stopService() {
        bleConnection.close();
        connectionExecutor.shutdownNow();
        connectionExecutor = Executors.newSingleThreadExecutor();
        serviceRunning = false;
        AppViewModel.hideTimer();
    }

    @Override
    protected void onStop() {
        stopService();
        super.onStop();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.nav_settings) {
            NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
            navController.navigate(R.id.nav_settings);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}