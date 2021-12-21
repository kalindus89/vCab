package com.vcab.driver.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.vcab.driver.Messages_Common_Class;
import com.vcab.driver.R;

public class ProfileFragment extends Fragment {

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v= inflater.inflate(R.layout.fragment_profile, container, false);

      //  Messages_Common_Class.sendNotifyToCustomer(getActivity(),v,"rwPmUIaCDZOw5oJvSx7mMFXrTxx2");
        return v;
    }

}