package org.fruct.oss.tsp.activities;

import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.FrameLayout;

import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import org.fruct.oss.tsp.LocationTrackingService;
import org.fruct.oss.tsp.R;
import org.fruct.oss.tsp.fragments.BaseFragment;
import org.fruct.oss.tsp.fragments.CommonFragment;
import org.fruct.oss.tsp.fragments.MapFragment;
import org.fruct.oss.tsp.fragments.PointListFragment;
import org.fruct.oss.tsp.fragments.PrefFragment;
import org.fruct.oss.tsp.fragments.ScheduleListFragment;
import org.fruct.oss.tsp.fragments.SchedulesFragment;
import org.fruct.oss.tsp.fragments.SearchFragment;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Основное окно приложения
 *
 * Приложение имеет двухуровневую навигацию:
 * <ol>
 *     <li>Боковая панель (NavigationDrawer)</li>
 *     <li>Панель tab'ов</li>
 * </ol>
 *
 * MainActivity управляет навигацией по приложению:
 * <ul>
 *     <li>Выполняет переключение фрагментов при выборе пунктов бокового меню и tab'ов</li>
 *     <li>Скрывает панель tab'ов при необходимости</li>
 * </ul>
 *
 */
public class MainActivity extends AppCompatActivity {
	private static final String TRANSACTION_ROOT = "TRANSACTION_ROOT";

	public static final String TAG_COMMON_FRAGMENT = "TAG_COMMON_FRAGMENT";

	@Bind(R.id.toolbar)
	Toolbar toolbar;

	@Bind(R.id.container)
	FrameLayout container;

	@Bind(R.id.tabbar)
	TabLayout tabbar;

	private CommonFragment commonFragment;

	private Drawer drawer;
	private TabLayout.Tab geoTab;
	private TabLayout.Tab tripTab;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		ButterKnife.bind(this);

		setupCommonFragment();

		hideTabbar();
		setupToolbar();
		setupDrawer();

		switchSearchFragment();
	}

	private void setupCommonFragment() {
		commonFragment = (CommonFragment) getSupportFragmentManager()
				.findFragmentByTag(TAG_COMMON_FRAGMENT);
		if (commonFragment == null) {
			commonFragment = new CommonFragment();
			getSupportFragmentManager()
					.beginTransaction()
					.add(commonFragment, TAG_COMMON_FRAGMENT)
					.commit();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		LocationTrackingService.actionStartTracking(this);
	}

	@Override
	protected void onPause() {
		LocationTrackingService.actionStopTracking(this);
		super.onPause();
	}

	@Override
	public void onBackPressed() {
		if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
			getSupportFragmentManager().popBackStack();
		} else {
			finish();
		}
	}

	private void setupToolbar() {
		setSupportActionBar(toolbar);
	}

	private void setupTabbar() {
		tabbar.removeAllTabs();
		tabbar.addTab(geoTab = tabbar.newTab().setText(R.string.tab_geo).setTag(R.id.tab_geo));
		tabbar.addTab(tripTab = tabbar.newTab().setText(R.string.tab_trip).setTag(R.id.tab_trip));
		tabbar.setOnTabSelectedListener(new TabListener());
		tabbar.setVisibility(View.VISIBLE);
	}

	private void hideTabbar() {
		tabbar.setVisibility(View.GONE);
		tabbar.setOnTabSelectedListener(null);
	}

	private void setupDrawer() {
		drawer = new DrawerBuilder(this)
				.withToolbar(toolbar)
				.addDrawerItems(
						new PrimaryDrawerItem()
								.withIdentifier(R.id.drawer_schedules)
								.withName(R.string.nav_schedules),
						new PrimaryDrawerItem()
								.withIdentifier(R.id.drawer_search)
								.withName(R.string.nav_search),
						new PrimaryDrawerItem()
								.withIdentifier(R.id.drawer_list)
								.withName(R.string.nav_list),

						new PrimaryDrawerItem()
								.withIdentifier(R.id.drawer_map)
								.withName(R.string.nav_map),

						new PrimaryDrawerItem()
								.withIdentifier(R.id.drawer_pref)
								.withName(R.string.nav_pref),

						new PrimaryDrawerItem()
								.withIdentifier(R.id.drawer_quit)
								.withName(R.string.nav_quit)
				)
				.withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
					@Override
					public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
						onDrawerItemClicked(position, drawerItem);
						return false;
					}
				})
				.build();
	}

	private void onDrawerItemClicked(int position, IDrawerItem drawerItem) {
		switch (drawerItem.getIdentifier()) {
		case R.id.drawer_schedules:
			switchSchedulesFragment();
			break;

		case R.id.drawer_search:
			switchSearchFragment();
			break;

		case R.id.drawer_list:
			switchGeoFragment();
			break;

		case R.id.drawer_map:
			switchMapFragment();
			break;

		case R.id.drawer_pref:
			switchPrefFragment();
			break;

		case R.id.drawer_quit:
			finish();
			break;
		}
	}

	private void switchSearchFragment() {
		hideTabbar();
		switchFragment(new SearchFragment());
	}

	private void switchSchedulesFragment() {
		hideTabbar();
		switchFragment(new SchedulesFragment());
	}

	private void switchPrefFragment() {
		hideTabbar();
		switchFragment(new PrefFragment());
	}

	private void switchMapFragment() {
		hideTabbar();
		switchFragment(new MapFragment());
	}

	private void switchGeoFragment() {
		if (tabbar.getVisibility() != View.VISIBLE) {
			setupTabbar();
			geoTab.select();
		}
		switchFragment(new PointListFragment());
	}

	private void switchTripFragment() {
		if (tabbar.getVisibility() != View.VISIBLE) {
			setupTabbar();
			tripTab.select();
		}
		switchFragment(new ScheduleListFragment());
	}

	private void switchFragment(Fragment fragment) {
		getSupportFragmentManager().popBackStack(TRANSACTION_ROOT, FragmentManager.POP_BACK_STACK_INCLUSIVE);

		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		transaction.addToBackStack(TRANSACTION_ROOT);
		transaction.replace(R.id.container, fragment);
		transaction.commit();
	}

	private class TabListener implements TabLayout.OnTabSelectedListener {
		@Override
		public void onTabSelected(TabLayout.Tab tab) {
			if (tab == geoTab) {
				switchGeoFragment();
			} else if (tab == tripTab) {
				switchTripFragment();
			}
		}

		@Override
		public void onTabUnselected(TabLayout.Tab tab) {

		}

		@Override
		public void onTabReselected(TabLayout.Tab tab) {

		}
	}
}
