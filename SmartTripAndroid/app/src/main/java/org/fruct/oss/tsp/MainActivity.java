package org.fruct.oss.tsp;

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

import org.fruct.oss.tsp.fragments.PointListFragment;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {
	private static final String TRANSACTION_ROOT = "TRANSACTION_ROOT";

	@Bind(R.id.toolbar)
	Toolbar toolbar;

	@Bind(R.id.container)
	FrameLayout container;
	
	@Bind(R.id.tabbar)
	TabLayout tabbar;

	private Drawer drawer;
	private TabLayout.Tab geoTab;
	private TabLayout.Tab tripTab;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		ButterKnife.bind(this);

		hideTabbar();
		setupToolbar();
		setupDrawer();

		switchGeoFragment();
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
	}

	private void setupDrawer() {
		drawer = new DrawerBuilder(this)
				.withToolbar(toolbar)
				.addDrawerItems(
						new PrimaryDrawerItem()
								.withIdentifier(R.id.drawer_list)
								.withName(R.string.nav_list),
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
		case R.id.drawer_list:
			switchGeoFragment();
			break;

		case R.id.drawer_quit:
			finish();
			break;
		}
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
		//switchFragment(new PointListFragment());
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
