package com.radar.radarshop;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class OffersAdapter extends RecyclerView.Adapter<OffersAdapter.VH> {

    public interface OnClick { void run(offer o); }

    private final List<offer> items;
    private final OnClick onClick;

    public OffersAdapter(List<offer> items, OnClick onClick) {
        this.items = items; this.onClick = onClick;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_offer_card, parent, false);
        return new VH(v);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int pos) {
        offer o = items.get(pos);
        h.title.setText(o.title);
        h.subtitle.setText(o.subtitle);
        h.img.setImageResource(o.imageRes);
        h.itemView.setOnClickListener(v -> onClick.run(o));
    }

    @Override public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        ImageView img; TextView title; TextView subtitle;
        VH(@NonNull View v) {
            super(v);
            img = v.findViewById(R.id.img);
            title = v.findViewById(R.id.title);
            subtitle = v.findViewById(R.id.subtitle);
        }
    }
}
