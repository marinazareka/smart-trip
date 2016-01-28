package org.fruct.oss.tsp.adapters;

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.fruct.oss.tsp.R;
import org.fruct.oss.tsp.commondatatype.Point;

import java.util.Collections;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PointAdapter extends RecyclerView.Adapter<PointAdapter.Holder> {
	private final Listener listener;

	private List<Point> pointList = Collections.emptyList();

	public PointAdapter(@Nullable Listener listener) {
		this.listener = listener;
	}

	@Override
	public PointAdapter.Holder onCreateViewHolder(ViewGroup parent, int viewType) {
		return new Holder(LayoutInflater.from(parent.getContext())
				.inflate(R.layout.item_list_simple, parent, false));
	}

	@Override
	public void onBindViewHolder(PointAdapter.Holder holder, int position) {
		holder.bind(pointList.get(position));
	}

	@Override
	public int getItemCount() {
		return pointList.size();
	}

	public void setPointList(List<Point> pointList) {
		this.pointList = pointList;
		notifyDataSetChanged();
	}

	class Holder extends RecyclerView.ViewHolder {
		@Bind(android.R.id.text1)
		TextView textView;

		private final View view;

		private Point point;

		public Holder(View itemView) {
			super(itemView);

			ButterKnife.bind(this, itemView);

			this.view = itemView;
		}

		public void bind(Point point) {
			textView.setText(point.getTitle());
			this.point = point;
		}

		@OnClick(R.id.root)
		void onItemClicked() {
			if (listener != null) {
				listener.onPointClicked(point, view);
			}
		}
	}

	public interface Listener {
		void onPointClicked(Point point, View anchorView);
	}
}