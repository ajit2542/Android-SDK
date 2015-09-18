package com.oym.indoor.navigation.views;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.oym.indoor.navigation.R;
import com.oym.indoor.navigation.views.CustomListView.CustomItem;
import com.oym.indoor.navigation.views.CustomListView.CustomItems;

public class CustomRecyclerViewAdapter extends RecyclerView.Adapter<CustomRecyclerViewAdapter.ViewHolder> {

    private ArrayList<CustomItems> list;
    private Context context;
    private int color;
    

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView image;
    	public TextView title;
    	public TextView subtitle;
    	
        public ViewHolder(View view, CustomItems item) {
            super(view);
            if (item instanceof CustomItem) {
            	image = (ImageView) view.findViewById(R.id.LIImage);
            	title = (TextView) view.findViewById(R.id.LITitle);
            	subtitle = (TextView) view.findViewById(R.id.LISubtitle);
            } else {
            	title = (TextView) view.findViewById(R.id.LSTitle);
            }
        }
    }

    public CustomRecyclerViewAdapter(ArrayList<CustomItems> list, Context ctx, int resourceColor) {
        this.list = list;
        context = ctx;
        color = ctx.getResources().getColor(resourceColor);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, final int i) {
        // create a new view
        View v;
        if (list.get(i) instanceof CustomItem) {
        	v = LayoutInflater.from(viewGroup.getContext())
        			.inflate(R.layout.list_item, viewGroup, false);
        } else {
        	v = LayoutInflater.from(viewGroup.getContext())
        			.inflate(R.layout.list_section, viewGroup, false);
        }
        
        ViewHolder vh = new ViewHolder(v, list.get(i));
        return vh;

    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        if (list.get(i) instanceof CustomItem) {
        	CustomItem item = (CustomItem) list.get(i);
        	viewHolder.title.setText(item.title);
        	viewHolder.subtitle.setText(item.subtitle);
        	Drawable d = context.getResources().getDrawable(item.imageResource);
        	d.mutate();
        	d.setColorFilter(color, Mode.MULTIPLY);
        	viewHolder.image.setImageDrawable(d);
        } 
    	
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
