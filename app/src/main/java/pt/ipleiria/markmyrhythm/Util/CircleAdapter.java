package pt.ipleiria.markmyrhythm.Util;


import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.lang.reflect.Type;
import java.util.ArrayList;

import at.grabner.circleprogress.CircleProgressView;
import pt.ipleiria.markmyrhythm.Model.Goal;
import pt.ipleiria.markmyrhythm.R;

public class CircleAdapter extends ArrayAdapter<Goal> {
    private Activity activity;
    private ArrayList<Goal> goals;
    private static LayoutInflater inflater = null;

    public CircleAdapter (Activity activity, int textViewResourceId,ArrayList<Goal> goals) {
        super(activity, textViewResourceId, goals);
        try {
            this.activity = activity;
            this.goals = goals;

            inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        } catch (Exception e) {

        }
    }

    public int getCount() {
        return goals.size();
    }

    public Goal getItem(Goal position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    public static class ViewHolder {
        public CircleProgressView circle;
        public TextView text;
        public TextView title;
        public TextView current;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        final ViewHolder holder;
        try {

            if (convertView == null) {
                vi = inflater.inflate(R.layout.activity_new_challenge_act2, null);
                holder = new ViewHolder();
                holder.circle = vi.findViewById(R.id.circleViewChanllengeRun);
                holder.text = vi.findViewById(R.id.textViewChallenge);
                holder.title = vi.findViewById(R.id.textView3);
                holder.current = vi.findViewById(R.id.textViewCurrent);
                vi.setTag(holder);
            } else {
                holder = (ViewHolder) vi.getTag();
            }

            holder.circle.setBlockCount(2);
            holder.circle.setBlockScale(1);
            holder.circle.setMaxValueAllowed(100);
            holder.text.setTypeface(null, Typeface.BOLD);
            holder.title.setTypeface(null, Typeface.BOLD);

            String recurence = "";
            if (goals.get(position).getRecurence() == 1){
                recurence = "hoje";
            }else {
                recurence = "esta semana";
            }
            if (position != 0) {
                holder.title.setVisibility(View.INVISIBLE);
            }else {
                holder.title.setVisibility(View.VISIBLE);
            }
            if (goals.get(position).getDataType().matches("com.google.distance.delta")) {
                holder.circle.setValue((goals.get(position).getCurrent() / goals.get(position).getValue()) *100 );
                holder.circle.setMaxValueAllowed(100);
                holder.text.setText("Percorrer " + goals.get(position).getValue() / 1000 + "km " + recurence);
                float value = goals.get(position).getCurrent() / 1000;
                holder.current.setText(String.format("%.2f",value)+ "km atualmente");
            }else {
                holder.circle.setValue((goals.get(position).getCurrent() / goals.get(position).getValue()) *100 );
                holder.text.setText("Tem de dar " + (int) goals.get(position).getValue() + " passos "+ recurence );
                holder.circle.setMaxValueAllowed(100);
                holder.current.setText(String.format("%.0f",goals.get(position).getCurrent())+ " passos atualmente");
            }
           // holder.circle.setValue(goals.get(position).getValue());

        } catch (Exception e) {


        }
        return vi;
    }
}