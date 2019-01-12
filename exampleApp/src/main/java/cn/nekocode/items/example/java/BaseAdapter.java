package cn.nekocode.items.example.java;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import cn.nekocode.items.ItemAdapter;

import java.util.ArrayList;

public abstract class BaseAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements ItemAdapter {
    private final ArrayList mList = new ArrayList();

    public ArrayList getList() {
        return mList;
    }

    @NonNull
    @Override
    public <T> T getData(int position) {
        return (T) mList.get(position);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }
}
