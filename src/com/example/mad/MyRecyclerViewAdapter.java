package com.example.mad;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.DataObjectHolder> 
{
    private static String LOG_TAG = "MyRecyclerViewAdapter";
    private ArrayList<DataObject> mDataset;
    private static MyClickListener myClickListener;
    
    public interface MyClickListener {
        public void onItemClick(int position, View v);
    }
 
    // Provide a suitable constructor (depends on the kind of dataset)
    public MyRecyclerViewAdapter(ArrayList<DataObject> myDataset) {
        mDataset = myDataset;
    }
    
    public static class DataObjectHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        TextView label;
        TextView dateTime;

        public DataObjectHolder(View itemView) {
            super(itemView);
            label = (TextView) itemView.findViewById(R.id.textView);
            dateTime = (TextView) itemView.findViewById(R.id.textView2);
            Log.i(LOG_TAG, "Adding Listener");
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            myClickListener.onItemClick(getAdapterPosition(), v);
        }
    }
    
    //called from onResume()
    public void setOnItemClickListener(MyClickListener myClickListener)
    {
        this.myClickListener = myClickListener;
    }
    
 
    // Create new views (invoked by the layout manager)
    @Override
    public DataObjectHolder onCreateViewHolder(ViewGroup parent,int viewType) 
    {
    	// create a new view
    	View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_row, parent, false);
    	// set the view's size, margins, paddings and layout parameters
    	Log.d("recyc adapter","parent layout -->"+parent.getContext());
        DataObjectHolder dataObjectHolder = new DataObjectHolder(view);
        return dataObjectHolder;
    }
    
    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(DataObjectHolder holder, int position)
    {
        holder.label.setText(mDataset.get(position).getmText1());
        holder.dateTime.setText(mDataset.get(position).getmText2());
    }

    public void addItem(DataObject dataObj, int index)
    {
        mDataset.add(index, dataObj);
        notifyItemInserted(index);
    }

    public void deleteItem(int index) {
        mDataset.remove(index);
        notifyItemRemoved(index);
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }


}