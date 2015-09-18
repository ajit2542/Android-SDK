package com.oym.indoor.navigation;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class FragmentInfo extends Fragment {

	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_info, container, false);
		
		ActionBar aba = ((ActionBarActivity) getActivity()).getSupportActionBar();
		aba.show();
		aba.setTitle("Instructions");
		aba.setDisplayHomeAsUpEnabled(true);
		
		return view;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		((ActionBarActivity) getActivity()).getSupportActionBar().hide();
	}

	
	
}
