package org.fruct.oss.tsp.fragments.root;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.fruct.oss.tsp.R;
import org.fruct.oss.tsp.commondatatype.Movement;
import org.fruct.oss.tsp.events.ScheduleStoreChangedEvent;
import org.fruct.oss.tsp.fragments.BaseFragment;
import org.fruct.oss.tsp.stores.ScheduleStore;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Пользовательский интерфейс для маршрута.
 *
 * Фрагмент подписывает на изменение хранилища маршрута и обновляет список при получении обновлений данных.
 */
public class MovementsListFragment extends BaseFragment {
	@Bind(R.id.recycler_view)
	RecyclerView recyclerView;

	private PointsAdapter adapter;
	private Subscription subscribe;

	@Nullable
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
		this.subscribe = getScheduleStore().getObservable()
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(adapter.OBSERVER);
	}

	@Override
	public void onPause() {
		subscribe.unsubscribe();
		super.onPause();
	}

	private void setupRecyclerView() {
		recyclerView.setHasFixedSize(true);
		recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(),
				LinearLayoutManager.VERTICAL, false));

		recyclerView.setAdapter(adapter = new PointsAdapter());
	}

	class PointsAdapter extends RecyclerView.Adapter<PointsAdapter.Holder> {
		private List<Movement> movements = new ArrayList<>();

		public final Action1<? super List<Movement>> OBSERVER = new Action1<List<Movement>>() {
			@Override
			public void call(List<Movement> movements) {
				PointsAdapter.this.movements = movements;
				notifyDataSetChanged();
			}
		};

		@Override
		public PointsAdapter.Holder onCreateViewHolder(ViewGroup parent, int viewType) {
			return new Holder(LayoutInflater.from(parent.getContext())
					.inflate(android.R.layout.simple_list_item_1, parent, false));
		}

		@Override
		public void onBindViewHolder(PointsAdapter.Holder holder, int position) {
			holder.bind(movements.get(position));
		}

		@Override
		public int getItemCount() {
			return movements.size();
		}

		class Holder extends RecyclerView.ViewHolder {
			@Bind(android.R.id.text1)
			TextView textView;

			public Holder(View itemView) {
				super(itemView);
				ButterKnife.bind(this, itemView);
			}

			public void bind(Movement movement) {
				textView.setText("Movement from " + movement.getA().getTitle()
						+ " to " + movement.getB().getTitle() + " time " + movement.getStartDateTime());
			}
		}
	}

}
