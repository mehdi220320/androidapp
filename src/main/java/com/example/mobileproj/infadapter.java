package com.example.mobileproj;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

public class infadapter extends BaseAdapter {
    private Context context;
    private List<Map<String, Object>> dataList;

    public infadapter(Context context, List<Map<String, Object>> dataList) {
        this.context = context;
        this.dataList = dataList;
    }

    @Override
    public int getCount() {
        return dataList.size();
    }

    @Override
    public Object getItem(int position) {
        return dataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.infraction_item, parent, false);
        }

        ImageView imageView = convertView.findViewById(R.id.infraction_image);
        TextView numserieView = convertView.findViewById(R.id.infraction_numserie);
        TextView dateView = convertView.findViewById(R.id.infraction_date);
        TextView locationView = convertView.findViewById(R.id.infraction_location);

        Map<String, Object> item = dataList.get(position);
        byte[] imageBytes = (byte[]) item.get("image");
        Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);

        imageView.setImageBitmap(bitmap);
        numserieView.setText(item.get("numserie").toString());
        dateView.setText(item.get("date").toString());
        locationView.setText(item.get("location").toString());

        return convertView;
    }
}