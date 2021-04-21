package com.example.hw2.ui.map;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hw2.R;
import com.mancj.materialsearchbar.adapter.SuggestionsAdapter;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;

public class CustomSuggestionsAdapter extends SuggestionsAdapter<CarmenFeature, CustomSuggestionsAdapter.AddressHolder> {
    private SuggestionsAdapter.OnItemViewClickListener listener;

    public CustomSuggestionsAdapter(LayoutInflater inflater) {
        super(inflater);
    }

    public void setListener(SuggestionsAdapter.OnItemViewClickListener listener) {
        this.listener = listener;
    }

    private String getFeatureText(CarmenFeature feature) {
        return feature.placeName();
    }

    @Override
    public void onBindSuggestionHolder(CarmenFeature feature, AddressHolder holder, int position) {
        holder.title.setText(getFeatureText(feature));
    }

    @Override
    public int getSingleViewHeight() {
        return 40;
    }

    @NonNull
    @Override
    public AddressHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = getLayoutInflater().inflate(R.layout.address_card, parent, false);
        return new AddressHolder(view);
    }

    public interface OnItemViewClickListener {
        void OnItemClickListener(int position, View v);
    }


    public class AddressHolder extends RecyclerView.ViewHolder{
        protected TextView title;

        public AddressHolder(@NonNull View itemView) {
            super(itemView);

            this.title = itemView.findViewById(R.id.address_title);
            itemView.setOnClickListener(v -> {
                v.setTag(getSuggestions().get(getAdapterPosition()));
                listener.OnItemClickListener(getAdapterPosition(), v);
            });
        }
    }
}