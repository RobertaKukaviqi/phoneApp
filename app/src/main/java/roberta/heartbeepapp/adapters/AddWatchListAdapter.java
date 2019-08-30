package roberta.heartbeepapp.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Objects;

import roberta.heartbeepapp.R;
import roberta.heartbeepapp.models.WatchEntity;

public class AddWatchListAdapter extends RecyclerView.Adapter<AddWatchListAdapter.WatchViewHolder> {

    ArrayList<WatchEntity> data = new ArrayList<>();

    public void setData(ArrayList<WatchEntity> data){
        this.data = data;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public WatchViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.view_item_watch, viewGroup, false);
        return new WatchViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull WatchViewHolder watchViewHolder, int i) {
        watchViewHolder.render(data.get(i));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class WatchViewHolder extends RecyclerView.ViewHolder{

        TextView name;

        public WatchViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.name);
        }

        public void render(final WatchEntity entity){
            name.setText(entity.getWatchUserName());

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showRequestDialog(itemView.getContext(), entity);
                }
            });
        }
    }

    private void showRequestDialog(final Context context, final WatchEntity entity){
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.DialogTheme)
                .setMessage("Do you want to send a request to " + entity.getWatchUserName() + "?")
                .setPositiveButton("Send", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        FirebaseDatabase.getInstance().getReference("users")
                                .child("wear")
                                .child(entity.getWatchuUserID())
                                .child("requests")
                                .child(Objects.requireNonNull(user).getUid())
                                .setValue(user.getDisplayName())
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        FirebaseDatabase.getInstance().getReference("users")
                                                .child("phone")
                                                .child(user.getUid())
                                                .child("wears")
                                                .child(entity.getWatchuUserID())
                                                .setValue(false);

                                        showSuccessDialog(context);
                                    }
                                });
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

        builder.show();
    }

    private void showSuccessDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.DialogTheme)
                .setMessage("Request sent successfully!")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        builder.show();
    }
}
