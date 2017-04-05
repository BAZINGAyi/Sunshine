package com.example.bazinga.sunshine;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by bazinga on 2017/4/4.
 */
public class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.ViewHolder> {

    private String[] mdatas ;

    private ForecastAdapterOnClickHandler mClickHandler;

    public ForecastAdapter(ForecastAdapterOnClickHandler forecastAdapterOnClickHandler){

        mClickHandler = forecastAdapterOnClickHandler;
    }

    interface ForecastAdapterOnClickHandler{

        void onClickItem(String s);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        Context context = parent.getContext();

        int layoutIdForListItem = R.layout.forecast_item;

        LayoutInflater inflater = LayoutInflater.from(context);

        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, parent, shouldAttachToParentImmediately);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        holder.textView.setText(mdatas[position]);

    }

    @Override
    public int getItemCount() {

        if (mdatas == null)

            return 0;

        return mdatas.length;

    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView textView;

        public ViewHolder(View itemView) {

            super(itemView);

            textView  = (TextView) itemView.findViewById(R.id.tv_weather_data);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

            String s = mdatas[getAdapterPosition()];

            mClickHandler.onClickItem(s);
        }
    }

    public void setDatas(String [] mdatas){

        this.mdatas = mdatas;

        notifyDataSetChanged();

    }
}
