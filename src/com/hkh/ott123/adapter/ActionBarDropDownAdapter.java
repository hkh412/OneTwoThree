package com.hkh.ott123.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.hkh.ott123.R;
import com.hkh.ott123.data.CityData;

public class ActionBarDropDownAdapter extends ArrayAdapter<CityData>{
	Context mContext;

    public ActionBarDropDownAdapter (Context context, int resource, List<CityData> objects) {
        super(context, resource, objects);
        mContext = context;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent){
        View row = convertView;
        if (row == null) {
        	LayoutInflater inflater=(LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        	row = inflater.inflate(R.layout.layout_citydata_list_item, null);
        }
    	TextView tv = (TextView) row.findViewById(R.id.tv_cityname);
    	String cityName = getItem(position).getCity();
    	tv.setText(cityName);
        return row;
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        View row = convertView;
        if (row == null) {
        	LayoutInflater inflater=(LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        	row = inflater.inflate(R.layout.layout_citydata_button, null);
        }
        TextView tv = (TextView) row.findViewById(R.id.tv_selected_city);
        String cityName = getItem(position).getCity();
    	tv.setText(cityName);
        return row;
    }
}
