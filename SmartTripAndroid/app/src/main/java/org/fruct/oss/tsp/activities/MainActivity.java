package org.fruct.oss.tsp.activities;

import android.app.SearchManager;
import android.content.Intent;
import android.os.PersistableBundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;

import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import org.fruct.oss.tsp.R;
import org.fruct.oss.tsp.fragments.CommonFragment;
import org.fruct.oss.tsp.fragments.root.MapFragment;
import org.fruct.oss.tsp.fragments.root.PrefFragment;
import org.fruct.oss.tsp.fragments.root.MovementsListFragment;
import org.fruct.oss.tsp.fragments.root.SchedulesFragment;
import org.fruct.oss.tsp.fragments.root.SearchFragment;

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
	public static final String TAG_COMMON_FRAGMENT = "TAG_COMMON_FRAGMENT";

	private static final String TRANSACTION_ROOT = "TRANSACTION_ROOT";
	private static final String TRANSACTION_SECONDARY = "TRANSACTION_SECONDARY";

	@Bind(R.id.toolbar)
	Toolbar toolbar;

	@Bind(R.id.container)
	FrameLayout container;

	@Bind(R.id.tabbar)
	TabLayout tabbar;

	private CommonFragment commonFragment;
	private Drawer drawer;

	private int transactionStackLevel = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		ButterKnife.bind(this);

		setupCommonFragment();

		if (savedInstanceState != null) {
			transactionStackLevel = savedInstanceState.getInt("transactionStackLevel");
		}

		hideTabbar();
		setupToolbar();
		setupDrawer();

		switchSearchFragment();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		if (intent.getAction() != null && intent.getAction().equals(Intent.ACTION_SEARCH)) {
			String searchString = intent.getStringExtra(SearchManager.QUERY);
			if (searchString == null) {
				searchString = intent.getStringExtra(SearchManager.EXTRA_DATA_KEY);
			}

			if (TextUtils.isEmpty(searchString)) {
				return;
			}

			Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.container);
			if (currentFragment instanceof SearchFragment) {
				((SearchFragment) currentFragment).search(searchString);
			}
		}
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
	public void onBackPressed() {
		if (drawer.isDrawerOpen()) {
			drawer.closeDrawer();
		} else if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
			getSupportFragmentManager().popBackStack();
			switchAppbarIconDrawer();
		} else {
			finish();
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
		outState.putInt("transactionStackLevel", transactionStackLevel);
	}

	private void setupToolbar() {
		setSupportActionBar(toolbar);
	}

	private void hideTabbar() {
		tabbar.setVisibility(View.GONE);
		tabbar.setOnTabSelectedListener(null);
	}

	private void setupDrawer() {
		drawer = new DrawerBuilder(this)
				.withToolbar(toolbar)
				.withActionBarDrawerToggle(true)
				.withActionBarDrawerToggleAnimated(true)
				.addDrawerItems(
						new PrimaryDrawerItem()
								.withIdentifier(R.id.drawer_schedules)
								.withName(R.string.nav_schedules),
						new PrimaryDrawerItem()
								.withIdentifier(R.id.drawer_search)
								.withName(R.string.nav_search),
						new PrimaryDrawerItem()
								.withIdentifier(R.id.drawer_movements)
								.withName(R.string.nav_movements),
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
				.withOnDrawerNavigationListener(new Drawer.OnDrawerNavigationListener() {
					@Override
					public boolean onNavigationClickListener(View clickedView) {
						onBackPressed();
						return true;
					}
				})
				.withSelectedItem(R.id.drawer_search)
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

		case R.id.drawer_movements:
			switchMovementsFragment();
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

	private void switchMovementsFragment() {
		hideTabbar();
		switchFragment(new MovementsListFragment());
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

	private void switchFragment(Fragment fragment) {
		getSupportFragmentManager().popBackStack(TRANSACTION_ROOT, FragmentManager.POP_BACK_STACK_INCLUSIVE);

		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		transaction.addToBackStack(TRANSACTION_ROOT);
		transaction.replace(R.id.container, fragment);
		transaction.commit();

		switchAppbarIconDrawer();
	}

	public void switchSecondaryFragment(Fragment fragment) {
		getSupportFragmentManager().popBackStack(TRANSACTION_SECONDARY, FragmentManager.POP_BACK_STACK_INCLUSIVE);

		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		transaction.addToBackStack(TRANSACTION_SECONDARY);
		transaction.setCustomAnimations(R.anim.slide_left_enter, R.anim.slide_left_exit,
				R.anim.slide_right_enter, R.anim.slide_right_exit);
		transaction.replace(R.id.container, fragment);
		transaction.commit();

		switchAppbarIconBack();
	}

	private void switchAppbarIconBack() {
		drawer.getActionBarDrawerToggle().setDrawerIndicatorEnabled(false);
		if (getSupportActionBar() != null) {
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	private void switchAppbarIconDrawer() {
		if (getSupportActionBar() != null) {
			getSupportActionBar().setDisplayHomeAsUpEnabled(false);
		}
		drawer.getActionBarDrawerToggle().setDrawerIndicatorEnabled(true);
	}
}