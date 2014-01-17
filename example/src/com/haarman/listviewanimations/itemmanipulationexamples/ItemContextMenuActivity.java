package com.haarman.listviewanimations.itemmanipulationexamples;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Random;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.OnNavigationListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.haarman.listviewanimations.ArrayAdapter;
import com.haarman.listviewanimations.MyListActivity;
import com.haarman.listviewanimations.R;
import com.haarman.listviewanimations.itemmanipulation.contextualdialog.ContextualDialogAdapter;
import com.haarman.listviewanimations.itemmanipulation.contextualdialog.ContextualDialogAdapter.ConfirmItemCallback;

public class ItemContextMenuActivity
extends MyListActivity<ItemContextMenuActivity.Message>
implements OnNavigationListener {

	/** Number of sample messages to show in the demo */
	static public final int SAMPLE_COUNT = 20;
	
	
	static public final String[] SAMPLE_SUBJECTS = {
		"Lorem ipsum dolor sit amet"
		, "Aenean ac consectetur sapien"
		, "Morbi et dolor venenatis"
		, "Donec interdum erat lectus"
		, "Nullam adipiscing est ante"
		, "Phasellus suscipit sed nulla sed molestie"
		, "Maecenas at ligula sem"
		, "Vestibulum id consectetur diam"
	};
	
	/** Samples for populating bodies */
	static public final String[] SAMPLE_BODIES = {
		"Consectetur adipiscing elit. Sed bibendum auctor augue elementum iaculis. Maecenas arcu nisi, ullamcorper ac velit eu, sodales porttitor augue. Sed et vestibulum neque. Etiam odio leo, mollis at porta vitae, lacinia vitae mi. Praesent accumsan velit eu nunc semper convallis. Nunc aliquam ligula a odio pellentesque ullamcorper. Fusce eget leo lorem. Sed sit amet posuere quam. Nunc condimentum dignissim justo."
		, "Quis tempor diam. Aliquam erat volutpat. Suspendisse metus justo, pulvinar id venenatis sit amet, bibendum vel augue. Nullam non consectetur purus, eu dapibus lectus. Nullam sit amet libero vehicula, convallis massa vitae, elementum odio. Mauris ornare dictum velit. Duis sed arcu est. Etiam sed enim eu orci ultrices tempor. Praesent mattis massa magna, volutpat mattis lectus porta vel. Aliquam gravida non est ac vehicula."
		, "Ultrices velit in, rhoncus lectus. Maecenas viverra risus ligula, sit amet tincidunt nisi lobortis id. Sed ornare diam sagittis tortor mattis blandit. Duis laoreet, nisl quis semper sodales, neque risus tempus tellus, nec bibendum nisi neque nec felis. Vestibulum id ligula ipsum. Donec lobortis volutpat nulla, in interdum ligula consequat vitae."
		, "Sodales mollis erat posuere vestibulum. In semper eros et elit semper, ut scelerisque massa faucibus. Nullam laoreet, dui sit amet elementum semper, leo metus cursus tortor, ac sollicitudin mauris erat sit amet libero. Ut tempor elit vitae turpis ultricies tincidunt. Vivamus accumsan lacus quam, nec accumsan nunc egestas sed."
		, "Eget scelerisque nunc tristique at. Donec molestie consequat nisl et egestas. Vestibulum fringilla ornare enim, nec faucibus dui gravida vitae. Nulla fermentum blandit risus, ut mattis justo placerat sit amet. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia Curae; Quisque vitae porta diam. Nulla sagittis cursus fringilla."
		, "Fusce pulvinar tempor justo, at sollicitudin risus bibendum vitae. Mauris fermentum, turpis vel commodo aliquet, urna nunc sagittis urna, vel tincidunt odio ante et tellus. Vestibulum tempus urna vitae risus pulvinar tempus. Sed eu justo tellus. Pellentesque quis velit eros. Vivamus sit amet porta arcu. Nullam risus lectus, ullamcorper vel enim et, euismod placerat ligula."
		, "Vivamus sed lacinia libero, sit amet accumsan quam. Sed scelerisque mauris lectus, in vehicula metus sodales in. Ut cursus non nulla quis tincidunt. Aenean congue purus ac dui aliquam aliquet. Phasellus tempus diam tellus, id gravida ipsum ultricies eu."
		, "Cras varius nibh in dolor euismod, id sodales nibh suscipit. Maecenas facilisis semper ipsum sit amet hendrerit. Fusce ornare odio malesuada imperdiet eleifend. Praesent fringilla, odio sed malesuada iaculis, mi eros viverra orci, sit amet pulvinar quam est nec nisi. Donec lacinia et nibh vel congue. Duis scelerisque imperdiet lorem at commodo. Sed nec pretium elit, sit amet luctus tortor. Phasellus viverra adipiscing magna in auctor."
	};
	
	
	private ArrayAdapter<Message> mAdapter;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mAdapter = createListAdapter();

		setSwipeItemContextMenuAdapter();

		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		getSupportActionBar().setListNavigationCallbacks(new SelectionAdapter(), this);
		getSupportActionBar().setDisplayShowTitleEnabled(false);
	}
	
	/** Generate a sample set of messages to display in the demo */
	@Override
	protected ArrayList<Message> getItems() {
		ArrayList<Message> items = new ArrayList<Message>(SAMPLE_COUNT);
		Message.Flag[] flags = Message.Flag.values();
		Random random = new Random(0L);
		long mask = 0L;
		mask = 1L << (flags.length - 1);
		mask |= mask - 1L;
		for (int i = 0; i < SAMPLE_COUNT; i++) {
			Message message = new Message();
			long bits = random.nextLong() & 0x7fffffffffffffffL;
			message.setSubject(SAMPLE_SUBJECTS[(int) (bits % SAMPLE_SUBJECTS.length)]);
			message.setBody(SAMPLE_BODIES[(int) (bits % SAMPLE_BODIES.length)]);
			bits &= mask;
			while (bits != 0L) {
				int bit = Long.numberOfTrailingZeros(bits);
				bits ^= 1L << bit;
				message.setFlag(flags[bit]);
			}
			items.add( message );
		}
		return items;
	}
	
	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		// TODO
		return false;
	}
	
	@Override
	protected String rowToString(Message message) {
		return message.getSubject();
	}
	
	private void setSwipeItemContextMenuAdapter() {
		ContextualDialogAdapter adapter = new ContextualDialogAdapter(mAdapter, R.layout.dialog_row, R.id.dialog_row_confirmbutton, R.id.dialog_row_cancelbutton);
		adapter.setAbsListView(getListView());
		getListView().setAdapter(adapter);
		adapter.setConfirmItemCallback( new ConfirmItemCallback() {
			@Override
			public void confirmItem(int position) {
				mAdapter.remove(position);
				mAdapter.notifyDataSetChanged();
			}
		} );
		//adapter.setCancelItemCallback( this );
	}
	
	
	/** Each list item corresponds to an instance of Message */
	public static class Message {

		public static enum Flag {
			READ, STARRED, TRASH, SPAM
		}

		/** The subject of the message */
		private String mSubject;

		/** The body of the message */
		private String mBody;

		/** Contains set of flags for the message, initially no flags are set */
		private EnumSet<Flag> mFlags = EnumSet.noneOf(Flag.class);

		/** Get the message subject */
		public String getSubject() {
			return mSubject;
		}

		/** Set the message subject */
		public void setSubject(String subject) {
			mSubject = subject;
		}

		/** get the message body */
		public String getBody() {
			return mBody;
		}

		/** Set the message body */
		public void setBody(String body) {
			mBody = body;
		}

		/** Return true if given flag is set */
		public boolean getFlag(Flag flag) {
			return mFlags.contains(flag);
		}

		/** Set the given flag */
		public void setFlag(Flag flag) {
			mFlags.add(flag);
		}

		/** Clear the given flag */
		public void clearFlag(Flag flag) {
			mFlags.remove(flag);
		}

		/** Toggle the given flag */
		public boolean toggleFlag(Flag flag) {
			boolean result = !getFlag(flag);
			if (result) {
				setFlag(flag);
			} else {
				clearFlag(flag);
			}
			return result;
		}

	}
	
	
	
	private class SelectionAdapter extends ArrayAdapter<String> {

		public SelectionAdapter() {
			addAll( "Swipe to context menu", "Long-click to context menu" );
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TextView tv = (TextView) convertView;
			if (tv == null) {
				LayoutInflater inflater = LayoutInflater.from(ItemContextMenuActivity.this);
				tv = (TextView) inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
			}
			tv.setText(getItem(position));
			return tv;
		}
	}


}
