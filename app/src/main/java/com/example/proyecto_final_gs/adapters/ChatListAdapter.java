package com.example.proyecto_final_gs.adapters;

import android.app.Activity;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.proyecto_final_gs.R;
import com.example.proyecto_final_gs.Utils;

import org.w3c.dom.Text;

import java.util.List;

public class ChatListAdapter extends ArrayAdapter<String> {

    private Activity context;
    List<String> urls;
    List<String> username;
    List<String> lastMessage;

    public ChatListAdapter(Activity context, List<String> urls, List<String> username, List<String> lastMessage){
        super(context, R.layout.layout_chat_list, username);
        this.context = context;
        this.urls = urls;
        this.username = username;
        this.lastMessage = lastMessage;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = context.getLayoutInflater();

        View listViewItem = inflater.inflate(R.layout.layout_chat_list, null, true);

        //
        ImageView imagen = listViewItem.findViewById(R.id.profilePhoto);
        TextView usernameText = listViewItem.findViewById(R.id.usernameText);
        TextView lastMessageText = listViewItem.findViewById(R.id.lastMessageText);

        //
        Utils.loadImageWithGlideSize(context, Uri.parse(urls.get(position)), imagen, 500, 500);
        usernameText.setText(username.get(position));
        lastMessageText.setText(lastMessage.get(position));

        return listViewItem;
    }
}
