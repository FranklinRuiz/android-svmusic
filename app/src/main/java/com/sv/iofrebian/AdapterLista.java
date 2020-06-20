package com.sv.iofrebian;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sv.iofrebian.utils.Cancion;
import java.util.List;

public class AdapterLista extends RecyclerView.Adapter<AdapterLista.ViewHolder>  {

    private Context ctx;
    private List<Cancion> items;
    private OnClickListener onClickListener = null;
    private SparseBooleanArray selected_items;

    public void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView artista, cancion;
        public View lyt_parent;

        public ViewHolder(View view) {
            super(view);
            artista= (TextView) view.findViewById(R.id.artista);
            cancion = (TextView) view.findViewById(R.id.cancion);
            lyt_parent = (View) view.findViewById(R.id.lyt_parent);
        }
    }

    public AdapterLista(Context mContext, List<Cancion> items) {
        this.ctx = mContext;
        this.items = items;
        selected_items = new SparseBooleanArray();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_lista, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final Cancion inbox = items.get(position);

        // displaying text view data
        holder.artista.setText(inbox.artista);
        holder.cancion.setText(inbox.nombreCancion);

        holder.lyt_parent.setActivated(selected_items.get(position, false));

        holder.lyt_parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onClickListener == null) return;
                onClickListener.onItemClick(v, inbox, position);
            }
        });

    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public interface OnClickListener {
        void onItemClick(View view, Cancion obj, int pos);

        //void onItemLongClick(View view, Lista obj, int pos);
    }

}