package com.example.proyecto_final_gs.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.proyecto_final_gs.R;
import com.musyzian.firebase.User;

import java.util.List;

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.UserViewHolder> {

    private Context mCtx;
    private List<User> userList;
    private UserListListener listener;

    public UserListAdapter(Context mCtx, List<User> userList, UserListListener listener) {
        this.mCtx = mCtx;
        this.userList = userList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(mCtx);
        View view = inflater.inflate(R.layout.layout_users_list, null);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder userViewHolder, int i) {
        final User user = userList.get(i);

        //cargar imagen con glide
        Glide.with(mCtx)
                .load(user.getProfileImageUrl())
                .apply(RequestOptions.circleCropTransform()).into(userViewHolder.imageView);

        //cargar textos
        userViewHolder.textViewName.setText(user.getName());
        userViewHolder.textViewAge.setText(user.getAge() + " years old");
        userViewHolder.textViewCity.setText(user.getCity());
        if(Float.parseFloat(String.format("%.0f",user.getDistance()/1000))<1)
            userViewHolder.textDistance.setText("1 km" );
        else
            userViewHolder.textDistance.setText(String.format("%.0f",user.getDistance()/1000) + " km" );

        String instruments = "";
        for(int it = 0; it < 3; it++){
            if(it<user.getInstruments().size())
                instruments = instruments + "- " + user.getInstruments().get(it) + "\r\n";
            if(it==2 && user.getInstruments().size()>3)
                instruments = instruments + "...";
        }
        userViewHolder.textViewInstruments.setText(instruments);

        String artists = "";
        for(int it = 0; it < 3; it++){
            if(it<user.getArtists().size())
                artists = artists + "- " + user.getArtists().get(it) + "\r\n";
            if(it==2 && user.getArtists().size()>3)
                artists = artists + "...";
        }
        userViewHolder.textViewArtists.setText(artists);

        //listener para el boton de las tarjetas de usuario
        userViewHolder.contactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onClick(user);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    class UserViewHolder extends RecyclerView.ViewHolder{

        ImageView imageView;
        TextView textViewName, textViewAge, textViewCity, textDistance, textViewInstruments, textViewArtists;
        Button contactButton;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.profileImageList);
            textViewName = itemView.findViewById(R.id.textUsername);
            textViewAge = itemView.findViewById(R.id.textAge);
            textViewCity = itemView.findViewById(R.id.textCity);
            textDistance = itemView.findViewById(R.id.textDistance);
            textViewInstruments = itemView.findViewById(R.id.textInstruments);
            textViewArtists = itemView.findViewById(R.id.textArtists);
            contactButton = itemView.findViewById(R.id.contactUserButton);
        }
    }

    public interface UserListListener{
        void onClick(User user);
    }

}
