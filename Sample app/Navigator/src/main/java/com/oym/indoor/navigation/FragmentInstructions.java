package com.oym.indoor.navigation;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.oym.indoor.LocationResult;
import com.oym.indoor.Instruction;
import com.oym.indoor.InstructionAdapter;
import com.oym.indoor.NotificationResult;
import com.oym.indoor.RouteProjectedPoint;
import com.oym.indoor.navigation.views.CustomListView.CustomItem;
import com.oym.indoor.navigation.views.CustomListView.CustomItems;
import com.oym.indoor.navigation.views.CustomRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.List;

public class FragmentInstructions extends Fragment implements IndoorLocationListener {
	
	// Flags
	private boolean isFirstTime = true;
	
	// View
	private LinearLayout progressBar;
	private RecyclerView recyclerView;
	
	// Data
	private List<Instruction> instructions;
	private GlobalState gs;


	public FragmentInstructions(List<Instruction> ins) {
		instructions = ins;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_instructions, container, false);
		
		recyclerView = (RecyclerView) view.findViewById(R.id.FICardList);
		progressBar = (LinearLayout) view.findViewById(R.id.FILoadingSpinner);
		
		return view;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		gs = (GlobalState) getActivity().getApplication();
		
		recyclerView.setHasFixedSize(true);
		LinearLayoutManager llm = new LinearLayoutManager(getActivity());
		llm.setOrientation(LinearLayoutManager.VERTICAL);
		recyclerView.setLayoutManager(llm);
//		recyclerView.setAdapter(new CustomRecyclerViewAdapter(items, getActivity(), R.color.themeColor));
		
		gs.addLocationCallback(this);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		gs.removeLocationCallback(this);
	}

	@Override
	public void onLocationUpdate(LocationResult location) {
		if (isFirstTime) {
			recyclerView.setVisibility(View.VISIBLE);
			progressBar.setVisibility(View.GONE);
			isFirstTime = false;
		}

		RouteProjectedPoint rpp = gs.getRoute().getProjectedPoint(location);
		ArrayList<CustomItems> list = new ArrayList<CustomItems>();
		for (Instruction i: instructions) {
			int dist = (int) (i.distance - rpp.distanceFromStart);
			if (dist == 0) {
				list.add(new CustomItem(InstructionAdapter.getString(getActivity(), i), 
						getString(R.string.routing_now),
						InstructionAdapter.getImageResource(i)));
			} else if (dist > 0) {
				list.add(new CustomItem(InstructionAdapter.getString(getActivity(), i), 
						getResources().getQuantityString(R.plurals.routing_in_meters, dist, dist), 
						InstructionAdapter.getImageResource(i)));
			}
		}
		recyclerView.setAdapter(new CustomRecyclerViewAdapter(list, getActivity(), R.color.primaryColor));
	}

	@Override
	public void onNotification(NotificationResult nr) {}
}
