package com.example.git;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * This adapter is used to fill a given view with single views generated for
 * each entry of the Git repository list.
 */
public class GitRepositoryArrayAdapter extends ArrayAdapter<String> {

	/**
	 * The list of repositories to represent in the ListView.
	 */
	private ArrayList<List<String>> repositoryList = new ArrayList<List<String>>();
	
	/**
	 * The current context within the application.
	 */
	private Context currentContext = null;
	
	/**
	 * Creates a new adapter.
	 * @param context	The current context.
	 * @param textViewResourceId	The resource ID for a layout file containing a TextView to use when instantiating views.
	 * @param repositories 	The list of repositories to represent in the ListView. 
	 */
	public GitRepositoryArrayAdapter(Context context, int textViewResourceId, ArrayList<List<String>> repositories) {
		super(context, textViewResourceId);
		currentContext = context;
		if(repositories != null) { 
			repositoryList = repositories;
		}
	}

	@Override
	/**
	 * Get the view for a specific element of the list.
	 * @param position The position within the list.
	 * @param convertView	A previously returned view, that can be reused.
	 * @param parent	The parent ViewGroup.
	 * @return	The view for the list item.
	 */
	public View getView(int position, View convertView, ViewGroup parent) {
		View rowView = convertView;
		if (rowView == null) {
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			rowView = inflater.inflate(R.layout.list_item, parent, false);
		}
		TextView rowTextView = (TextView) rowView.findViewById(R.id.textview);
		List<String> rowData = repositoryList.get(position);
		if (rowData != null) {
			StringBuffer rowBuffer = new StringBuffer("");
			rowBuffer.append(currentContext.getResources().getString(R.string.name) + ": ");
			rowBuffer.append(rowData.get(1) + "\n");
			rowBuffer.append(currentContext.getResources().getString(R.string.path) + ": ");
			rowBuffer.append(rowData.get(0) + "\n");
			rowBuffer.append(currentContext.getResources().getString(R.string.creation_date) + ": ");
			rowBuffer.append(rowData.get(2));
			rowTextView.setText(rowBuffer.toString());
		}
		return rowView;
	}

	@Override
	/** 
	 * Counts the amount of list items.
	 * @return The amount of list items.
	 */
	public int getCount() {
		return repositoryList.size();
	}
}