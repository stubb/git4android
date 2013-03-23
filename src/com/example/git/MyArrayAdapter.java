package com.example.git;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class MyArrayAdapter extends ArrayAdapter<String> {

	private ArrayList<List<String>> repositoryList = new ArrayList<List<String>>();

	public MyArrayAdapter(Context context, int textViewResourceId, ArrayList<List<String>> repositories) {
		super(context, textViewResourceId);
		if(repositories != null) { 
			repositoryList = repositories;
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View row = inflater.inflate(R.layout.list_item, parent, false);
		TextView rowView = (TextView) row.findViewById(R.id.textview);
		List<String> rowData = repositoryList.get(position);
		if (rowData != null) {
			String rowText = "";
			rowText += "Name: ";
			rowText += rowData.get(1) + "\n";
			rowText += "Path: ";
			rowText += rowData.get(0) + "\n";
			rowText += "Creation date: ";
			rowText += rowData.get(2);
			
			rowView.setText(rowText);
		}
		return row;
	}

	@Override
	public int getCount() {
		return repositoryList.size();
	}
}