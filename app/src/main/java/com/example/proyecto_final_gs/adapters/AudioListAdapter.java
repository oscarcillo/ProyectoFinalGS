package com.example.proyecto_final_gs.adapters;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.proyecto_final_gs.R;

import java.util.List;

public class AudioListAdapter extends RecyclerView.Adapter<AudioListAdapter.AudioViewHolder>{

    private Context context;
    private List<String> urls;
    MediaPlayer mediaPlayer = new MediaPlayer();

    public AudioListAdapter(Context context, List<String> urls){
        this.context = context;
        this.urls = urls;
    }

    @NonNull
    @Override
    public AudioViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.layout_audio_list, null);
        return new AudioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AudioViewHolder audioViewHolder, final int i) {

        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        //listener para el boton de play
        audioViewHolder.playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.stop();
                mediaPlayer = new MediaPlayer();
                try{
                    mediaPlayer.setDataSource(urls.get(i));
                    mediaPlayer.prepare();
                }catch(Exception e){
                    e.printStackTrace();
                }

                mediaPlayer.start();
            }
        });
    }

    @Override
    public int getItemCount() {
        return urls.size();
    }


    ///////
    class AudioViewHolder extends RecyclerView.ViewHolder{

    ImageButton playButton;
    SeekBar seekBarAudio;
    TextView textDurationAudio;

        public AudioViewHolder(@NonNull View itemView) {
            super(itemView);

            playButton = itemView.findViewById(R.id.playButton);
            seekBarAudio = itemView.findViewById(R.id.seekBarAudio);
            textDurationAudio = itemView.findViewById(R.id.textDurationAudio);
        }
    }



}
