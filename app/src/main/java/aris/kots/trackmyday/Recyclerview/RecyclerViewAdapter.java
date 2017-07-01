package aris.kots.trackmyday.Recyclerview;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import aris.kots.trackmyday.CleverFunctions.ConvertFunctons;
import aris.kots.trackmyday.Database.DBAdapter;
import aris.kots.trackmyday.R;
import aris.kots.trackmyday.TrackActivity;
import aris.kots.trackmyday.ViewRecording;

/**
 * Created by Aris on 07-Apr-15.
 */
public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder> {

    private LayoutInflater inflater;
    List<ListViewItem> items = Collections.emptyList();
    AssetManager am;
    Context con;

    public RecyclerViewAdapter(Context context, List<ListViewItem> items, AssetManager am) {
        inflater = LayoutInflater.from(context);
        this.items = items;
        this.am = am;
        this.con = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = inflater.inflate(R.layout.card, parent, false);
        MyViewHolder holder = new MyViewHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {

        final ListViewItem current = items.get(position);

        holder.date.setText(current.date);
        holder.speed.setText(current.speed);
        holder.elapseTime.setText(ConvertFunctons.elapseTimeToString(current.elapse_time));

        double distance = Double.valueOf(current.distance);
//        if (distance >= 1.0)
        holder.distance.setText(String.format(Locale.getDefault(),"%.2f km", distance));
//        else
//            holder.distance.setText(String.format(Locale.getDefault(),"%.0f meters", distance * 1000));

        //if we click the business card
        holder.cardview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(con, ViewRecording.class);
                Bundle extras = new Bundle();
                extras.putLong("recording_id",current.recording_id);
                intent.putExtras(extras);
                ((Activity)con).startActivityForResult(intent, 0);
                ((Activity)con).overridePendingTransition(android.R.anim.fade_in,
                        android.R.anim.fade_out);

            }
        });

        holder.discard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DeleteFromDB(current.recording_id, position);
            }
        });

    }

    private void DeleteFromDB(final long recording_id, final int position) {
        AlertDialog.Builder alert = new AlertDialog.Builder(con);
        alert.setTitle("Delete Entry");
        alert.setIcon(R.mipmap.ic_delete_white_48dp);
        alert.setMessage("Are you sure you want to delete this Recording?");
        alert.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                DBAdapter db = new DBAdapter(con);
                db.open();
                boolean test = db.deleteRecording(recording_id);
                db.close();
                items.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, items.size());
            }
        });
        alert.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alert.show();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        TextView date;
        TextView distance;
        TextView speed;
        TextView elapseTime;
        TextView distanceLabel;
        TextView speedLabel;
        TextView elapseTimeLabel;
        Button discard;
        CardView cardview;

        public MyViewHolder(View itemView) {
            super(itemView);

            date = (TextView) itemView.findViewById(R.id.card_recording_date);
            distance = (TextView) itemView.findViewById(R.id.card_recording_distance);
            speed = (TextView) itemView.findViewById(R.id.card_recording_average_speed);
            elapseTime = (TextView) itemView.findViewById(R.id.card_recording_elapse_time);
            distanceLabel = (TextView) itemView.findViewById(R.id.card_recording_distance_label);
            speedLabel = (TextView) itemView.findViewById(R.id.card_recording_average_speed_label);
            elapseTimeLabel = (TextView) itemView.findViewById(R.id.card_recording_elapse_time_label);
            discard = (Button) itemView.findViewById(R.id.deleteButton);
            cardview = (CardView) itemView.findViewById(R.id.card_view);

            Typeface light = Typeface.createFromAsset(am, "fonts/Roboto-Light.ttf");
            Typeface bold = Typeface.createFromAsset(am, "fonts/Roboto-Bold.ttf");
            Typeface reg = Typeface.createFromAsset(am, "fonts/Roboto-Regular.ttf");

            date.setTypeface(reg);
            distance.setTypeface(bold);
            speed.setTypeface(bold);
            elapseTime.setTypeface(bold);

            distanceLabel.setTypeface(light);
            elapseTimeLabel.setTypeface(light);
            speedLabel.setTypeface(light);

        }
    }

    public void removeAt(int position) {
        items.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, items.size());
    }
}

