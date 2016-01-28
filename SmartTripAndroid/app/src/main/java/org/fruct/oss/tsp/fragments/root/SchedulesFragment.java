package org.fruct.oss.tsp.fragments.root;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;

import org.fruct.oss.tsp.R;
import org.fruct.oss.tsp.commondatatype.Schedule;
import org.fruct.oss.tsp.fragments.AddScheduleFragment;
import org.fruct.oss.tsp.fragments.BaseFragment;
import org.fruct.oss.tsp.util.Pref;

import java.util.Collections;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class SchedulesFragment extends BaseFragment {
	private static final String TAG_ADD_SCHEDULE_FRAGMENT = "TAG_ADD_SCHEDULE_FRAGMENT_2";

	@Bind(R.id.recycler_view)
	RecyclerView recyclerView;

	private SharedPreferences pref;

	private Subscription subscription;
	private Schedule editingSchedule;

	private ScheduleAdapter adapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		pref = PreferenceManager.getDefaultSharedPreferences(getContext());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_list, container, false);
		ButterKnife.bind(this, view);
		setupRecyclerView();
		return view;
	}

	@Override
	public void onResume() {
		super.onResume();

		this.subscription = getDatabase().loadSchedules()
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(new Action1<List<Schedule>>() {
					@Override
					public void call(List<Schedule> schedules) {
						setScheduleList(schedules);
					}
				});

		updateCurrentSchedule();
	}

	@Override
	public void onPause() {
		subscription.unsubscribe();

		super.onPause();
	}

	@Override
	public void onStart() {
		super.onStart();
		EventBus.getDefault().register(this);
	}

	@Override
	public void onStop() {
		EventBus.getDefault().unregister(this);
		super.onStop();
	}

	private void setupRecyclerView() {
		recyclerView.setHasFixedSize(true);
		recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(),
				LinearLayoutManager.VERTICAL, false));

		recyclerView.setAdapter(adapter = new ScheduleAdapter());
	}

	private void setScheduleList(List<Schedule> scheduleList) {
		adapter.setScheduleList(scheduleList);
	}

	private void setSelectedScheduleId(long scheduleId) {
		adapter.setSelectedScheduleId(scheduleId);
	}

	private void displayEditDialog(Schedule schedule) {
		AddScheduleFragment addScheduleFragment = AddScheduleFragment.newInstance(schedule);
		addScheduleFragment.show(getFragmentManager(), TAG_ADD_SCHEDULE_FRAGMENT);
	}

	public void onEventMainThread(AddScheduleFragment.ScheduleDialogFinishedEvent event) {
		onScheduleEdited(event.getNewSchedule());
	}



	private void updateCurrentSchedule() {
		setSelectedScheduleId(Pref.getCurrentSchedule(pref));
	}

	public void onActivateSchedule(Schedule schedule) {
		Pref.setCurrentSchedule(pref, schedule.getId());
		getDatabase().setCurrentSchedule(schedule.getId());
		updateCurrentSchedule();
	}

	public void onEditSchedule(Schedule schedule) {
		editingSchedule = schedule;

		displayEditDialog(schedule);
	}

	public void onDeleteSchedule(Schedule schedule) {
		Pref.compareAndClearCurrentSchedule(pref, schedule.getId());
		getDatabase().deleteSchedule(schedule.getId());
	}

	public void onScheduleEdited(Schedule newSchedule) {
		getDatabase().updateSchedule(editingSchedule.getId(), newSchedule);
	}


	class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.Holder> {
		private List<Schedule> scheduleList = Collections.emptyList();
		private long selectedScheduleId;

		@Override
		public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
			return new Holder(LayoutInflater.from(parent.getContext())
					.inflate(R.layout.item_list_simple, parent, false));
		}

		@Override
		public void onBindViewHolder(Holder holder, int position) {
			holder.bind(scheduleList.get(position));
		}

		@Override
		public int getItemCount() {
			return scheduleList.size();
		}

		private void setScheduleList(List<Schedule> scheduleList) {
			this.scheduleList = scheduleList;
			notifyDataSetChanged();
		}

		public void setSelectedScheduleId(long id) {
			selectedScheduleId = id;
			notifyDataSetChanged();
		}

		class Holder extends RecyclerView.ViewHolder {
			@Bind(android.R.id.text1)
			TextView textView;

			private Schedule schedule;

			public Holder(View itemView) {
				super(itemView);
				ButterKnife.bind(this, itemView);
			}

			public void bind(Schedule schedule) {
				this.schedule = schedule;
				textView.setText(schedule.getTitle());
				itemView.setSelected(schedule.getId() == selectedScheduleId);
			}

			@OnClick(R.id.root)
			void onItemClicked() {
				PopupMenu popupMenu = new PopupMenu(getContext(), textView);
				popupMenu.inflate(R.menu.schedule);
				popupMenu.setOnMenuItemClickListener(new ScheduleMenuListener(schedule));
				popupMenu.show();
			}
		}
	}

	private class ScheduleMenuListener implements PopupMenu.OnMenuItemClickListener {
		private final Schedule schedule;

		public ScheduleMenuListener(Schedule schedule) {
			this.schedule = schedule;
		}

		@Override
		public boolean onMenuItemClick(MenuItem item) {
			switch (item.getItemId()) {
			case R.id.action_activate_schedule:
				onActivateSchedule(schedule);
				break;

			case R.id.action_edit:
				onEditSchedule(schedule);
				break;

			case R.id.action_delete:
				onDeleteSchedule(schedule);
				break;

			default:
				return false;
			}

			return true;
		}
	}
}
