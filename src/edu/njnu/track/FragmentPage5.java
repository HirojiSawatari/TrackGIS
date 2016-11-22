package edu.njnu.track;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;

public class FragmentPage5 extends Fragment{

	private LinearLayout mUser;
	private LinearLayout mMessage;
	private LinearLayout mSystem;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {	
		View view = inflater.inflate(R.layout.fragment_5,container, false);
		return view;
	}	
	
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		View view1 = (View)getActivity().findViewById(R.id.info_set);
		
		mSystem = (LinearLayout) view1.findViewById(R.id.system);
		mSystem.setOnClickListener(new OnClickListener(){
			public void onClick(View arg0) {
				// TODO 自动生成的方法存根
				Intent intent = new Intent(getActivity(), SystemSet.class);
				getActivity().startActivity(intent);
			}
		});
	}
}