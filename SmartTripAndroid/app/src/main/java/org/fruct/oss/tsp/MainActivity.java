package org.fruct.oss.tsp;

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

	private Drawer drawer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		ButterKnife.bind(this);

		setupToolbar();
		setupDrawer();
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
			switchFragment(new PointListFragment());
			break;

		case R.id.drawer_quit:
			finish();
			break;
		}
	}

	private void switchFragment(Fragment fragment) {
		getSupportFragmentManager().popBackStack(TRANSACTION_ROOT, FragmentManager.POP_BACK_STACK_INCLUSIVE);

		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		transaction.addToBackStack(TRANSACTION_ROOT);
		transaction.replace(R.id.container, fragment);
		transaction.commit();
	}
}
