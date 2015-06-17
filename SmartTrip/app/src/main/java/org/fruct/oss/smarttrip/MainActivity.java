package org.fruct.oss.smarttrip;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import org.fruct.oss.smarttrip.fragments.MapFragment;
import org.fruct.oss.smarttrip.fragments.PlaceHolderFragment;


public class MainActivity extends AppCompatActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		Drawer drawer = new DrawerBuilder()
				.withActivity(this)
				.withToolbar(toolbar)
				.withHeader(R.layout.drawer_header)
				.withFireOnInitialOnClick(true)
				.addDrawerItems(
						new PrimaryDrawerItem().withName("Map").withIcon(R.drawable.ic_nav_map),
						new PrimaryDrawerItem().withName("Goodbye world")
				)
				.withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
					@Override
					public boolean onItemClick(AdapterView<?> adapterView, View view, int i, long l, IDrawerItem iDrawerItem) {
						Fragment fragment;

						switch (i) {
						case 0:
							fragment = MapFragment.newInstance();
							break;

						default:
							fragment = null;
							break;
						}

						if (fragment != null) {
							getSupportFragmentManager()
									.beginTransaction()
									.replace(R.id.content, fragment)
									.commit();
						} else {
							finish();
						}
						return false;
					}
				})
				.build();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_settings) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}
}
