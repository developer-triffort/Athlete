package com.athlete.google.android.apps.mytracks;


//class to be used in fragment ="com.athlete.google.android.apps.mytracks.MyMapFragment"
import com.google.android.gms.maps.MapFragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class MyMapFragment extends MapFragment
{
	private static View mapView;
	@Override
	public View onCreateView(LayoutInflater arg0, ViewGroup arg1, Bundle arg2) {
		// TODO Auto-generated method stub
		mapView=super.onCreateView(arg0, arg1, arg2);
		return mapView;
	}
	public View getMapView()
	{
		return mapView;
	}
}
