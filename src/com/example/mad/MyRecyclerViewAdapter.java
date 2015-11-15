package com.example.mad;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
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
	private int mPreviousposition=0;
	private int no_of_cards;
	private static Toast toast;
	
	public interface MyClickListener {
		public void onItemClick(int position, View v, SendButton sendbutton);
		public void onCardClick(int position, View v/*, SendButton sendbutton, MotionEvent event*/);

	}

	// Provide a suitable constructor (depends on the kind of dataset)
	public MyRecyclerViewAdapter(ArrayList<DataObject> myDataset) {
		mDataset = myDataset;
		this.no_of_cards=getItemCount();
		Log.d(LOG_TAG,"no_of_cards in setOnItemClickListener:"+no_of_cards);
		
	}

	public static class DataObjectHolder extends RecyclerView.ViewHolder implements View.OnClickListener,View.OnTouchListener,OnGestureListener
	{
		TextView label;
		TextView nots, played, splitter;
		View mMessengerButton;
		View mCardView;
		SendButton sendbutton;
		//ShareButton sendbutton;
		GestureDetector gestureDetector;
		private View view;
		 private static final int SWIPE_DISTANCE_THRESHOLD = 100;
	     private static final int SWIPE_VELOCITY_THRESHOLD = 100;
		public DataObjectHolder(View itemView) {
			super(itemView);
			label = (TextView) itemView.findViewById(R.id.music_key);
			nots = (TextView) itemView.findViewById(R.id.nots);
			played=(TextView) itemView.findViewById(R.id.played);
			splitter=(TextView) itemView.findViewById(R.id.splitter);
			mMessengerButton=itemView.findViewById(R.id.messenger_send_button);
			//sendbutton = (SendButton)itemView.findViewById(R.id.fbsendButton);
			// dateTime = (TextView) itemView.findViewById(R.id.textView2);
			Log.i(LOG_TAG, "Adding Listener");
			mCardView=itemView.findViewById(R.id.card_view);
			mCardView.setOnTouchListener(this);
			mMessengerButton.setOnClickListener(this);
			gestureDetector=new GestureDetector(context,this);
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
			/*if(! (event.getAction() == android.view.MotionEvent.ACTION_SCROLL) && ! (event.getAction() == android.view.MotionEvent.ACTION_MOVE)
					&& event.getAction() == android.view.MotionEvent.ACTION_DOWN || event.getAction() == android.view.MotionEvent.ACTION_UP)
				myClickListener.onCardClick(getAdapterPosition(), v, sendbutton,event);
			return false;*/
			this.view=v;
			return gestureDetector.onTouchEvent(event);
			
		}

		@Override
		public boolean onDown(MotionEvent e) {
			// TODO Auto-generated method stub
			Log.d(LOG_TAG,"ondown");
			return true;
		}

		@Override
		public void onShowPress(MotionEvent e) {
			// TODO Auto-generated method stub
			Log.d(LOG_TAG,"onShowPress");
			
		}

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			// TODO Auto-generated method stub
			Log.d(LOG_TAG,"onSingleTapUp");
			return false;
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
			// TODO Auto-generated method stub
			Log.d(LOG_TAG,"onScroll");
			return false;
		}

		@Override
		public void onLongPress(MotionEvent e) {
			// TODO Auto-generated method stub
			Log.d(LOG_TAG,"onLongPress");
		 	myClickListener.onCardClick(getAdapterPosition(),this.view);
			
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			// TODO Auto-generated method stub
			Log.d(LOG_TAG,"onFling");
			/*float distanceX = e2.getX() - e1.getX();
            float distanceY = e2.getY() - e1.getY();
            if (Math.abs(distanceX) > Math.abs(distanceY) && Math.abs(distanceX) > SWIPE_DISTANCE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                if (distanceX < 0)
                	myClickListener.onCardClick(getAdapterPosition());
                
                return true;
            }*/
            
			return false;
		}

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
		if(toast!=null)
			toast.cancel();
		holder.label.setText(mDataset.get(position).getmText1());
		//means graph api has returned null due to some error
		if(mDataset.get(position).getmText2()==null)
			mDataset.get(position).setmText2("0");
		holder.nots.setText(mDataset.get(position).getmText2()+" NoTS");
		holder.splitter.setText("-");
		holder.played.setText(mDataset.get(position).getmText3()+" Plays");
		
		Log.d(LOG_TAG,"PrevPosition:"+mPreviousposition);
		//add transition if scrolled down
		if(position>mPreviousposition)
		{
			ObjectAnimator animatorTranslateY = ObjectAnimator.ofFloat(holder.itemView, "translationY",300, 0);
			animatorTranslateY.start();
		}
		//add transition if scrolled up
		else if (position<mPreviousposition)
		{
			
			ObjectAnimator animatorTranslateY = ObjectAnimator.ofFloat(holder.itemView, "translationY",-300, 0);
			animatorTranslateY.start();
		}
		mPreviousposition = position;
		Log.d(LOG_TAG,"position:"+position);
		toast=Toast.makeText(context, ""+(position+1)+" of "+ no_of_cards, Toast.LENGTH_SHORT);
		toast.show();
		
		Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
           @Override
           public void run() {
               toast.cancel(); 
           }
        }, 500);
		
		
			
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