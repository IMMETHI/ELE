package com.example.rohitme.contactsdashboard_rohit;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

/**
 * Created by rohit.me on 16/11/16.
 */

public class GridViewAdapter extends BaseAdapter {
    private List<ContactModel> dataList;
    private Context context;
    private LayoutInflater layoutInflater;

    public GridViewAdapter(List<ContactModel> dataList, Context context) {
        this.dataList = dataList;
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
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
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View layout = convertView;
        if (convertView == null) {
            layout = layoutInflater.inflate(R.layout.item_contact, parent, false);
        }
        ImageView ivPhoto = (ImageView) layout.findViewById(R.id.iv_pic);
        TextView tvName = (TextView) layout.findViewById(R.id.tv_name);
        TextView tvPhone = (TextView) layout.findViewById(R.id.tv_phone_no);
        TextView tvEmail = (TextView) layout.findViewById(R.id.tv_email);
        TextView tvLastContactTime = (TextView) layout.findViewById(R.id.tv_last_contact_time);
        TextView tvTotalContactTime = (TextView) layout.findViewById(R.id.tv_total_talktime);

        ivPhoto.setImageBitmap(getBitmapForContact(dataList.get(position).image_uri));
        tvName.setText(dataList.get(position).name);
        tvPhone.setText("Ph:" +dataList.get(position).phoneNumber);
        tvEmail.setText("Email:" +dataList.get(position).email);
        tvLastContactTime.setText("LCT:" +dataList.get(position).lastContactTime);
        tvTotalContactTime.setText("TTT:" +dataList.get(position).totalTalkTime + " sec");
        return layout;
    }

    private Bitmap getBitmapForContact(String image_uri) {
        Bitmap bitmap = null;
        if (image_uri != null) {
            try {
                bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), Uri.parse(image_uri));
            } catch (FileNotFoundException e) {

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return bitmap;
    }
}
