package com.athlete.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;

import com.athlete.services.IAppFilter;

/**
 * @author edBaev
 */
public abstract class BaseListAdapter<T> extends ArrayAdapter<T> {

	private Context mContext;
	private List<T> mList;
	private int mLayout;
	/*
	 * use if BaseListAdapter in SeparatedListAdapter
	 */
	protected int startFrom = -1;

	private SearchFilter filter;
	private List<T> listAllData;
	private IAppFilter<T> performingFiltering;

	public BaseListAdapter(Context context, List<T> list, int layout) {
		super(context, android.R.layout.simple_list_item_1, list);
		mContext = context;
		mList = list;
		mLayout = layout;
	}

	public BaseListAdapter(Context context, List<T> list, int layout,
			IAppFilter<T> performingFiltering) {
		super(context, android.R.layout.simple_list_item_1, list);

		mContext = context;
		mList = cloneItems(list);
		listAllData = cloneItems(list);
		mLayout = layout;
		this.performingFiltering = performingFiltering;
	}

	private List<T> cloneItems(List<T> items) {
		List<T> cloneItems = new ArrayList<T>();
		for (T item : items) {
			cloneItems.add(item);
		}
		return cloneItems;
	}

	@Override
	public int getCount() {
		if (mList != null) {
			return mList.size();
		}
		return 0;
	}

	public boolean isFilter() {
		if (performingFiltering == null) {
			return false;
		}
		if (mList != null) {
			return (mList.size() != listAllData.size());
		}
		return false;

	}

	public void addItems(List<T> items) {
		mList.addAll(items);
	}

	public void addItem(T type) {
		mList.add(type);
	}

	@Override
	public T getItem(int position) {
		return position >= 0 && position < mList.size() ? mList.get(position)
				: null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public Context getContext() {
		return mContext;
	}

	public List<T> getList() {
		return mList;
	}

	public void setList(List<T> list) {
		if (performingFiltering != null) {
			synchronized (this) {
				listAllData = list;
			}
		} else {
			this.mList = list;
		}
	}

	public void removeItem(int position) {
		mList.remove(position);
	}

	public View getLayout() {
		LayoutInflater inf = LayoutInflater.from(mContext);
		return inf.inflate(mLayout, null, true);
	}

	@Override
	public void clear() {
		super.clear();
		mList.clear();
	}

	public void setStartFrom(int from) {
		if (startFrom == -1) {
			startFrom = from;
		}
	}

	@Override
	public android.widget.Filter getFilter() {
		if (filter == null) {
			filter = new SearchFilter();
		}
		return filter;
	}

	class SearchFilter extends android.widget.Filter {

		@Override
		protected FilterResults performFiltering(CharSequence constraint) {

			FilterResults result = new FilterResults();

			if (performingFiltering != null && constraint != null
					&& constraint.toString().length() > 0) {
				constraint = constraint.toString().toLowerCase();
				List<T> lItems = new ArrayList<T>();
				List<T> filt = new ArrayList<T>();
				synchronized (this) {

					lItems.addAll(listAllData);
				}
				for (T item : lItems) {
					if (performingFiltering.performFiltering(constraint, item)) {
						filt.add(item);
					}
				}
				result.count = filt.size();
				result.values = filt;
			} else {
				synchronized (this) {
					result.values = cloneItems(listAllData);
					result.count = listAllData.size();
				}
			}

			return result;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void publishResults(CharSequence constraint,
				FilterResults results) {
			synchronized (this) {
				final List<T> localItems = (List<T>) results.values;
				notifyDataSetChanged();
				clear();
				for (T item : localItems) {
					add(item);
					mList.add(item);
				}
			}
		}

	}

}