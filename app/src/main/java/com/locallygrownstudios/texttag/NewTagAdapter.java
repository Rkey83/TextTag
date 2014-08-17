package com.locallygrownstudios.texttag;


import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class NewTagAdapter extends ArrayAdapter<NewTagBean> {

    private final int row;
    private final Activity activity;
    private NewTagBean newTagBean;
    private final List<NewTagBean> itemList;


    public NewTagAdapter(Activity act, int row, List<NewTagBean> itemList) {

        super(act, row, itemList);
        this.activity = act;
        this.row = row;
        this.itemList = itemList;

    }

    @Override
    public View getView(final int position, final View convertView, ViewGroup parent) {

        View view = convertView;
        final ViewHolder holder;

        if (view == null) {

            LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(row, null);
            holder = new ViewHolder();
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        if ((itemList == null) || ((position + 1) > itemList.size()))
        return view;

        newTagBean = itemList.get(position);
        holder.name = (TextView) view.findViewById(R.id.txt_new_tag_name);
        holder.number = (TextView) view.findViewById(R.id.txt_new_tag_number);

        if (holder.name != null && newTagBean.Nameget() != null && newTagBean.Nameget().trim().length() > 0) {
            holder.name.setText(newTagBean.Nameget());
        }

        if (holder.number != null && newTagBean.PhoneNoget() != null && newTagBean.PhoneNoget().trim().length() > 0) {
            holder.number.setText(newTagBean.PhoneNoget());
        }

        return view;
    }


    public class ViewHolder {

        public TextView name, number;

    }

}
