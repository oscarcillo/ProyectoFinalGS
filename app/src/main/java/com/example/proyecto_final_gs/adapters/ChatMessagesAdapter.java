package com.example.proyecto_final_gs.adapters;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.proyecto_final_gs.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class ChatMessagesAdapter extends ArrayAdapter<String> {

    private Activity context;
    List<String> messages;
    List<Boolean> owner;
    List<String> time;

    public ChatMessagesAdapter(Activity context, List<String> messages, List<Boolean> owner, List<String> time){
        super(context, R.layout.layout_left_message, messages);
        this.context = context;
        this.messages = messages;
        this.owner = owner;
        this.time = time;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = context.getLayoutInflater();

        Log.e("time", time.get(position));

        if(owner.get(position)){
            View listViewItem = inflater.inflate(R.layout.layout_right_message, null, true);

            //
            TextView messageText = listViewItem.findViewById(R.id.messageText);
            TextView timeMessageText = listViewItem.findViewById(R.id.timeMessageText);

            messageText.setText(messages.get(position));

            long second = (Long.parseLong(time.get(position)) / 1000) % 60;
            long minute = (Long.parseLong(time.get(position)) / (1000 * 60)) % 60;
            long hour = (Long.parseLong(time.get(position)) / (1000 * 60 * 60)) % 24;

            String time = String.format("%02d:%02d:%02d", hour, minute, second);

            timeMessageText.setText(time);

            return listViewItem;
        } else {
            View listViewItem = inflater.inflate(R.layout.layout_left_message, null, true);

            //
            TextView messageText = listViewItem.findViewById(R.id.messageText);
            TextView timeMessageText = listViewItem.findViewById(R.id.timeMessageText);

            messageText.setText(messages.get(position));

            long second = (Long.parseLong(time.get(position)) / 1000) % 60;
            long minute = (Long.parseLong(time.get(position)) / (1000 * 60)) % 60;
            long hour = (Long.parseLong(time.get(position)) / (1000 * 60 * 60)) % 24;

            String time = String.format("%02d:%02d:%02d", hour, minute, second);

            timeMessageText.setText(time);

            return listViewItem;
        }
    }

}
