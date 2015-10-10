package com.example.mad;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;

import java.util.ArrayList;

import com.facebook.share.widget.SendButton;
import com.facebook.share.widget.ShareButton;

public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.DataObjectHolder> 
{
	private static String LOG_TAG = "MyRecyclerViewAdapter";
	private ArrayList<DataObject> mDataset;
	private static MyClickListener myClickListener;
	//private GestureDetector gestureDetector;
	private static Context context;
	//private static SendButton sendbutton;

	public interface MyClickListener {
		public void onItemClick(int position, View v, SendButton sendbutton);
		public void onCardClick(int position, View v, SendButton sendbutton, MotionEvent event);

	}

	// Provide a suitable constructor (depends on the kind of dataset)
	public MyRecyclerViewAdapter(ArrayList<DataObject> myDataset) {
		mDataset = myDataset;
	}

	public static class DataObjectHolder extends RecyclerView.ViewHolder implements View.OnClickListener,View.OnTouchListener/*,OnGestureListener*/
	{
		TextView label;
		TextView nots;
		View mMessengerButton;
		View mCardView;
		SendButton sendbutton;
		//ShareButton sendbutton;
		GestureDetector gestureDetector;
		public DataObjectHolder(View itemView) {
			super(itemView);
			label = (TextView) itemView.findViewById(R.id.music_key);
			nots = (TextView) itemView.findViewById(R.id.nots);
			mMessengerButton=itemView.findViewById(R.id.messenger_send_button);
			//sendbutton = (SendButton)itemView.findViewById(R.id.fbsendButton);
			// dateTime = (TextView) itemView.findViewById(R.id.textView2);
			Log.i(LOG_TAG, "Adding Listener");
			mCardView=itemView.findViewById(R.id.card_view);
			mCardView.setOnTouchListener(this);
			mMessengerButton.setOnClickListener(this);
			//gestureDetector=new GestureDetector(context,this);
		}

		@Override
		public void onClick(View v) {
			if(v==mMessengerButton)
				myClickListener.onItemClick(getAdapterPosition(), v, sendbutton);
			//else if(v==mCardView)
			//myClickListener.onCardClick(getAdapterPosition(), v, sendbutton);
		}

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			// TODO Auto-generated method stub
			if(! (event.getAction() == android.view.MotionEvent.ACTION_SCROLL) && ! (event.getAction() == android.view.MotionEvent.ACTION_MOVE)
					&& event.getAction() == android.view.MotionEvent.ACTION_DOWN || event.getAction() == android.view.MotionEvent.ACTION_UP)
				myClickListener.onCardClick(getAdapterPosition(), v, sendbutton,event);
			return false;
		}
/*
		@Override
		public boolean onDown(MotionEvent e) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void onShowPress(MotionEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void onLongPress(MotionEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			// TODO Auto-generated method stub
			return false;
		}
*/
	}

	//called from onResume()
	public void setOnItemClickListener(MyClickListener myClickListener,Context context)
	{
		this.myClickListener = myClickListener;
		this.context=context;
		
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
		//means graph api has returned null due to some error
		if(mDataset.get(position).getmText2()==null)
			mDataset.get(position).setmText2("0");
		holder.nots.setText(mDataset.get(position).getmText2()+" NoTS");
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