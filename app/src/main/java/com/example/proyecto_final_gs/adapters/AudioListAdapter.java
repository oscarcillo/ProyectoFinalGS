package com.example.proyecto_final_gs.adapters;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
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
    private boolean deleteable;

    public AudioListAdapter(Context context, List<String> urls, boolean deleteable){
        this.context = context;
        this.urls = urls;
        this.deleteable = deleteable;
    }

    @NonNull
    @Override
    public AudioViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.layout_audio_list, null);
        return new AudioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final AudioViewHolder audioViewHolder, final int i) {
        audioViewHolder.playButton.setImageResource(android.R.drawable.ic_media_play);

        hilo h = new hilo(audioViewHolder.seekBarAudio, audioViewHolder.textDurationAudio, audioViewHolder.playButton, i);
        h.start();

        if(!deleteable)
            return;

        //listener al hacer pulsacion larga sobre el cardview
        audioViewHolder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                /*AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                final View dialogView = inflater.inflate(R.layout.dialog_filter, null);
                dialogBuilder.setView(dialogView);

                final AlertDialog alertDialog = dialogBuilder.create();
                alertDialog.setTitle(R.string.filter);

                alertDialog.show();*/

                return true;
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
    CardView cardView;

        public AudioViewHolder(@NonNull View itemView) {
            super(itemView);

            playButton = itemView.findViewById(R.id.playButton);
            seekBarAudio = itemView.findViewById(R.id.seekBarAudio);
            textDurationAudio = itemView.findViewById(R.id.textDurationAudio);
            cardView = itemView.findViewById(R.id.cardView);
        }
    }



    class hilo extends Thread{

        SeekBar seekBar;
        TextView textCurrent;
        ImageButton playButton;
        int position;

        public hilo(SeekBar seekBar, TextView textCurrent, ImageButton playButton, int position){
            this.seekBar = seekBar;
            this.textCurrent = textCurrent;
            this.playButton = playButton;
            this.position = position;
        }

        @Override
        public void run() {
            final MediaPlayer mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

            try{
                mediaPlayer.setDataSource(urls.get(position));
                mediaPlayer.prepare();
            }catch(Exception e){}

            //listener para el boton de play
            playButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    seekBar.setMax(mediaPlayer.getDuration());

                    if(mediaPlayer.isPlaying()){
                        mediaPlayer.pause();
                        playButton.setImageResource(android.R.drawable.ic_media_play);
                    } else{
                        mediaPlayer.start();
                        playButton.setImageResource(android.R.drawable.ic_media_pause);
                    }

                }
            });

            //listener del seekbar para avanzar el audio
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    mediaPlayer.seekTo(progress);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });


            while(true) {
                seekBar.setProgress(mediaPlayer.getCurrentPosition());
                textCurrent.post(new Runnable() {

                    int tiempo = mediaPlayer.getCurrentPosition()/1000;
                    int segundos = 0;
                    int minutos = 0;

                    @Override
                    public void run() {

                        minutos = tiempo / 60;
                        segundos = tiempo % 60;

                        ///////
                        if(segundos<10)
                            textCurrent.setText(minutos + ":0" + segundos);
                        else if(segundos<60)
                            textCurrent.setText(minutos + ":" + segundos);
                    }
                });
                try{
                    sleep(1000);
                }catch (Exception e){

                }

            }
        }
    }


}
