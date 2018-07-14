package com.boguan.passbox.activity.fragment;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;

import com.boguan.passbox.activity.BaseActivity;

public class BaseFragment extends Fragment {
	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	protected BaseActivity getBaseActivity() {
		return (BaseActivity) getActivity();
	}

	protected void showToast(int resId) {
		getBaseActivity().showToast(resId);
	}

	protected void showToast(int resId, int duration) {
		getBaseActivity().showToast(resId, duration);
	}
}
