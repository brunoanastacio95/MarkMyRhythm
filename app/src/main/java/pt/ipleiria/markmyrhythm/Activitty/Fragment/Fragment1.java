package pt.ipleiria.markmyrhythm.Activitty.Fragment;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import pt.ipleiria.markmyrhythm.R;


@SuppressLint("NewApi")
public class Fragment1 extends Fragment {

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_new_challenge_act1, container, false);
    }
}
