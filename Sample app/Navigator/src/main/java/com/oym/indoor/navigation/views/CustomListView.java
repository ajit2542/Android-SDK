package com.oym.indoor.navigation.views;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.oym.indoor.navigation.R;
import com.oym.indoor.RoutePoint;

public class CustomListView {
	
	public static interface CustomItems {
		public String getTitle();
		public View getView(LayoutInflater li, ViewGroup parent);
	}

	public static class CustomSection implements CustomItems {
		public final String title;

		public CustomSection(String title) {
			this.title = title;
		}

		@Override
		public String getTitle() {
			return title;
		}

		@Override
		public View getView(LayoutInflater li, ViewGroup parent) {
			// Set view
			View v = li.inflate(R.layout.list_section, parent, false);
			v.setOnClickListener(null);
			v.setOnLongClickListener(null);
			v.setLongClickable(false);

			// Set fields
			final TextView sectionView = (TextView) v.findViewById(R.id.LSTitle);
			sectionView.setText(title);
			
			return v;
		}
	}
	
	public static class CustomSingleItem implements CustomItems {
		public final String title;
		public final RoutePoint point;
		private final boolean isClickable;
		private View.OnClickListener listener;

		public CustomSingleItem(String title) {
			this(title, true);
		}
		
		public CustomSingleItem(String title, boolean clickable) {
			this.title = title;
			point = null;
			this.isClickable = clickable;
		}
		
		public CustomSingleItem(String title, RoutePoint point) {
			this(title, point, true);
		}
		
		public CustomSingleItem(String title, RoutePoint point, boolean clickable) {
			this.title = title;
			this.point = point;
			this.isClickable = clickable;
		}

		public CustomSingleItem(String title, View.OnClickListener listener) {
			this(title, true);
			this.listener = listener;
		}
		
		@Override
		public String getTitle() {
			return title;
		}

		@Override
		public View getView(LayoutInflater li, ViewGroup parent) {
			// Set View
			View v = li.inflate(R.layout.list_item_simple, parent, false);
			if (isClickable && listener != null) {
				v.setOnClickListener(listener);
			} else if (!isClickable) {
				v.setOnClickListener(null);
			}
			v.setOnLongClickListener(null);
			v.setLongClickable(false);

			// Set fields
			final TextView sectionView = (TextView) v.findViewById(R.id.LSITitle);
			sectionView.setText(title);
			
			return v;
		}
	}

	public static class CustomItem implements CustomItems {		
		public final String title;
		public final String subtitle;		
		public final int imageResource;
		private final boolean isClickable;
		
		public CustomItem(String title, String subtitle, int imageResource) {
			this(title, subtitle, imageResource, true);
		}
		
		public CustomItem(String title, String subtitle, int imageResource, boolean isClickable) {
			this.title = title;
			this.subtitle = subtitle;
			this.imageResource = imageResource;
			this.isClickable = isClickable;
		}

		@Override
		public String getTitle() {
			return title;
		}

		@Override
		public View getView(LayoutInflater li, ViewGroup parent) {
			// Set view
			View v = li.inflate(R.layout.list_item, parent, false);
			if (!isClickable) {
				v.setOnClickListener(null);
			}
			
			// Set fields
			final TextView ttv = (TextView) v.findViewById(R.id.LITitle);
			final TextView stv = (TextView) v.findViewById(R.id.LISubtitle);
			final ImageView iv = (ImageView) v.findViewById(R.id.LIImage);
			ttv.setText(title);
			stv.setText(subtitle);
			iv.setImageResource(imageResource);
			
			return v;
		}
	}
	
	public static class CustomSingleItemImage implements CustomItems {		
		public final String title;	
		public final int imageResource;
		private final boolean isClickable;
		
		public CustomSingleItemImage(String title, int imageResource) {
			this(title, imageResource, true);
		}
		
		public CustomSingleItemImage(String title, int imageResource, boolean isClickable) {
			this.title = title;
			this.imageResource = imageResource;
			this.isClickable = isClickable;
		}

		@Override
		public String getTitle() {
			return title;
		}

		@Override
		public View getView(LayoutInflater li, ViewGroup parent) {
			// Set view
			View v = li.inflate(R.layout.list_item_simple_image, parent, false);
			if (!isClickable) {
				v.setOnClickListener(null);
			}
			
			// Set fields
			final TextView ttv = (TextView) v.findViewById(R.id.LISITitle);
			final ImageView iv = (ImageView) v.findViewById(R.id.LISIImage);
			ttv.setText(title);
			iv.setImageResource(imageResource);
			
			return v;
		}
	}

	public static class EntryAdapter extends ArrayAdapter<CustomItems> {

		private ArrayList<CustomItems> items;
		private LayoutInflater li;

		public EntryAdapter(Context context,ArrayList<CustomItems> items) {
			super(context, 0, items);
			this.items = items;
			li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;

			final CustomItems i = items.get(position);
			if (i != null) {
				v = i.getView(li, parent);				
			}
			return v;
		}
	}

	public static class CustomDivider implements CustomItems {

		@Override
		public String getTitle() {
			return null;
		}

		@Override
		public View getView(LayoutInflater li, ViewGroup parent) {
			// Set view
			View v = li.inflate(R.layout.list_divider, parent, false);
			v.setOnClickListener(null);

			return v;
		}
	}

}
