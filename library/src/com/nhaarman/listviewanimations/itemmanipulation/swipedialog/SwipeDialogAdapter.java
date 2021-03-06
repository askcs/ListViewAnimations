/*
 * Copyright 2013 Frankie Sardo
 * Copyright 2013 Niek Haarman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nhaarman.listviewanimations.itemmanipulation.swipedialog;

import static com.nineoldandroids.view.ViewHelper.setAlpha;
import static com.nineoldandroids.view.ViewHelper.setTranslationX;
import static com.nineoldandroids.view.ViewPropertyAnimator.animate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import android.app.Activity;
import android.os.Bundle;

import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import com.nhaarman.listviewanimations.BaseAdapterDecorator;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.contextualundo.ContextualUndoAdapter.DeleteItemCallback;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.view.ViewHelper;

/**
 * Warning: a stable id for each item in the adapter is required. The decorated
 * adapter should not try to cast convertView to a particular view. The
 * undoLayout should have the same height as the content row.
 * <p>
 * Usage: <br>
 * * Create a new instance of this class providing the {@link BaseAdapter} to
 * wrap, the undo layout, and the undo button id, optionally a delay time
 * millis, a count down TextView res id, and a delay in milliseconds before
 * deleting the item .<br>
 * * Call {@link #setConfirmItemCallback(ConfirmItemCallback)} to be notified of
 * when items should be removed from your collection.<br>
 * * Set your {@link ListView} to this ContextualUndoAdapter, and set this
 * ContextualUndoAdapter to your ListView.<br>
 */
public class SwipeDialogAdapter extends BaseAdapterDecorator
		implements
			SwipeDialogListViewTouchListener.Callback {

	private static final int ANIMATION_DURATION = 150;
	private static final String EXTRA_ACTIVE_REMOVED_ID = "removedId";

	private final int mDialogLayoutId;
	private final int mConfirmActionId;
	private final int mCancelActionId;

	private ConfirmItemCallback mConfirmItemCallback;
	private CancelItemCallback mCancelItemCallback;

	private SwipeDialogView mCurrentDialogedView;
	private long mCurrentDialogedId;

	private Map<View, Animator> mActiveAnimators = new ConcurrentHashMap<View, Animator>();

	private SwipeDialogListViewTouchListener mContextualDialogListViewTouchListener;

	/**
	 * Create a new ContextualDialogAdapter based on given parameters.
	 * 
	 * @param baseAdapter
	 *            The {@link BaseAdapter} to wrap
	 * @param dialogLayoutId
	 *            The layout resource id to show as dialog
	 * @param confirmActionId
	 *            resource id of confirm button (within the layout)
	 * @param cancelActionId
	 *            resource id of cancel button (within the layout)
	 */
	public SwipeDialogAdapter(BaseAdapter baseAdapter, int dialogLayoutId,
                            int confirmActionId, int cancelActionId) {
		super(baseAdapter);
		mDialogLayoutId = dialogLayoutId;
		mConfirmActionId = confirmActionId;
		mCancelActionId = cancelActionId;
		mCurrentDialogedId = -1;
	}

	@Override
	public final View getView(int position, View convertView, ViewGroup parent) {
		SwipeDialogView contextualDialogView = (SwipeDialogView) convertView;
		if (contextualDialogView == null) {
			contextualDialogView = new SwipeDialogView(
					parent.getContext(), mDialogLayoutId );
			contextualDialogView.findViewById(mConfirmActionId)
					.setOnClickListener(
							new ConfirmListener(contextualDialogView));
			contextualDialogView.findViewById(mCancelActionId)
					.setOnClickListener(
							new CancelListener(contextualDialogView));
		}

		View contentView = super.getView(position,
				contextualDialogView.getContentView(), contextualDialogView);
		contextualDialogView.updateContentView(contentView);

		long itemId = getItemId(position);

		if (itemId == mCurrentDialogedId) {
			contextualDialogView.displayDialog();
			mCurrentDialogedView = contextualDialogView;
		} else {
			contextualDialogView.displayContentView();
		}

		contextualDialogView.setItemId(itemId);
		
		// contextualDialogView.requestLayout();
		
		return contextualDialogView;
	}

	@Override
	public void setAbsListView(AbsListView listView) {
		super.setAbsListView(listView);
		mContextualDialogListViewTouchListener = new SwipeDialogListViewTouchListener(
				listView, this);
		mContextualDialogListViewTouchListener
				.setIsParentHorizontalScrollContainer(isParentHorizontalScrollContainer());
		mContextualDialogListViewTouchListener.setTouchChild(getTouchChild());
		listView.setOnTouchListener(mContextualDialogListViewTouchListener);
		listView.setOnScrollListener(mContextualDialogListViewTouchListener
				.makeScrollListener());
		listView.setRecyclerListener(new RecycleViewListener());
	}

	@Override
	public void onViewSwiped(View dismissView, int dismissPosition) {
		int position = getAbsListView().getPositionForView(dismissView);
		if (getAbsListView() instanceof ListView) {
			position -= ((ListView) getAbsListView()).getHeaderViewsCount();
		}
		SwipeDialogView contextualUndoView = (SwipeDialogView) dismissView;
		if (contextualUndoView.isContentDisplayed()) {
			restoreViewPosition(contextualUndoView);
			contextualUndoView.displayDialog();
			removePreviousContextualDialogIfPresent();
			setCurrentDialogedView(contextualUndoView);
		} else {
			performActionIfNecessary();
		}
	}

	private void restoreViewPosition(View view) {
		setAlpha(view, 1f);
		setTranslationX(view, 0);
	}

	private void removePreviousContextualDialogIfPresent() {
		if (mCurrentDialogedView != null) {
			//performActionIfNecessary();
			Button cancel = (Button) mCurrentDialogedView.findViewById( mCancelActionId );
			cancel.performClick();
		}
	}

	private void setCurrentDialogedView(SwipeDialogView currentDialogedView) {
		mCurrentDialogedView = currentDialogedView;
		mCurrentDialogedId = currentDialogedView.getItemId();
	}

	private void clearCurrentDialogedView() {
		mCurrentDialogedView = null;
		mCurrentDialogedId = -1;
	}

	@Override
	public void onListScrolled() {
		//performActionIfNecessary();
		removePreviousContextualDialogIfPresent();
	}

	private void performActionIfNecessary() {
		if (mCurrentDialogedView != null
				&& mCurrentDialogedView.getParent() != null) {
			ValueAnimator animator = ValueAnimator.ofInt(
					mCurrentDialogedView.getHeight(), 1).setDuration(
					ANIMATION_DURATION);
			animator.addListener(new RemoveViewAnimatorListenerAdapter(
					mCurrentDialogedView));
			animator.addUpdateListener(new RemoveViewAnimatorUpdateListener(
					mCurrentDialogedView));
			animator.start();
			mActiveAnimators.put(mCurrentDialogedView, animator);
			clearCurrentDialogedView();
		}
	}

	/**
	 * Set the ConfirmItemCallback for this ContextualUndoAdapter.
	 */
	public void setConfirmItemCallback(ConfirmItemCallback itemCallback) {
		mConfirmItemCallback = itemCallback;
	}

	/**
	 * Set the CancelItemCallback for this ContextualUndoAdapter.
	 */
	public void setCancelItemCallback(CancelItemCallback itemCallback) {
		mCancelItemCallback = itemCallback;
	}

	/**
	 * This method should be called in your {@link Activity}'s
	 * {@link Activity#onSaveInstanceState(Bundle)} to remember dismissed
	 * statuses.
	 * 
	 * @param outState
	 *            the {@link Bundle} provided by
	 *            Activity.onSaveInstanceState(Bundle).
	 */
	public void onSaveInstanceState(Bundle outState) {
		outState.putLong(EXTRA_ACTIVE_REMOVED_ID, mCurrentDialogedId);
	}

	/**
	 * This method should be called in your
	 * {@link Activity#onRestoreInstanceState(Bundle)} to remember dismissed
	 * statuses.
	 * 
	 * @param savedInstanceState
	 */
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		mCurrentDialogedId = savedInstanceState.getLong(
				EXTRA_ACTIVE_REMOVED_ID, -1);
	}

	/**
	 * Animate the item at given position away and show the dialog {@link View}.
	 * 
	 * @param position
	 *            the position.
	 */
	public void swipeViewAtPosition(int position) {
		mCurrentDialogedId = getItemId(position);
		for (int i = 0; i < getAbsListView().getChildCount(); i++) {
			int positionForView = getAbsListView().getPositionForView(
					getAbsListView().getChildAt(i));
			if (positionForView == position) {
				swipeView(getAbsListView().getChildAt(i), positionForView);
			}
		}
	}

	private void swipeView(final View view, final int dismissPosition) {
		ObjectAnimator animator = ObjectAnimator.ofFloat(view, "x",
				view.getMeasuredWidth());
		animator.addListener(new AnimatorListenerAdapter() {

			@Override
			public void onAnimationEnd(Animator animator) {
				onViewSwiped(view, dismissPosition);
			}
		});
		animator.start();
	}

	@Override
	public void setIsParentHorizontalScrollContainer(
			boolean isParentHorizontalScrollContainer) {
		super.setIsParentHorizontalScrollContainer(isParentHorizontalScrollContainer);
		if (mContextualDialogListViewTouchListener != null) {
			mContextualDialogListViewTouchListener
					.setIsParentHorizontalScrollContainer(isParentHorizontalScrollContainer);
		}
	}

	@Override
	public void setTouchChild(int childResId) {
		super.setTouchChild(childResId);
		if (mContextualDialogListViewTouchListener != null) {
			mContextualDialogListViewTouchListener.setTouchChild(childResId);
		}
	}

	/**
	 * A callback interface which is used to notify when the user confirms.
	 */
	public interface ConfirmItemCallback {
		/**
		 * Called when the user confirms.
		 * 
		 * @param position
		 *            the position of the item.
		 */
		public void confirmItem(int position);
	}

	/**
	 * A callback interface which is used to notify when the user cancels.
	 */
	public interface CancelItemCallback {
		/**
		 * Called when the user cancels.
		 * 
		 * @param position
		 *            the position of the item.
		 */
		public void cancelItem(int position);
	}

	private class RemoveViewAnimatorListenerAdapter
			extends
				AnimatorListenerAdapter {

		private final View mDismissView;
		private final int mOriginalHeight;

		public RemoveViewAnimatorListenerAdapter(View dismissView) {
			mDismissView = dismissView;
			mOriginalHeight = dismissView.getHeight();
		}

		@Override
		public void onAnimationEnd(Animator animation) {
			mActiveAnimators.remove(mDismissView);
			restoreViewPosition(mDismissView);
			restoreViewDimension(mDismissView);
			handleCurrentItem();
		}

		private void restoreViewDimension(View view) {
			ViewGroup.LayoutParams lp;
			lp = view.getLayoutParams();
			lp.height = mOriginalHeight;
			view.setLayoutParams(lp);
		}

		private void handleCurrentItem() {
			if ( mDismissView != null && mDismissView.getParent() != null ) {
				int position = getAbsListView().getPositionForView(mDismissView);
	
				if (getAbsListView() instanceof ListView) {
					position -= ((ListView) getAbsListView()).getHeaderViewsCount();
				}
				if (mConfirmItemCallback != null) {
					mConfirmItemCallback.confirmItem(position);
			}
			}
		}
	}

	private class RemoveViewAnimatorUpdateListener
			implements
				ValueAnimator.AnimatorUpdateListener {

		private final View mDismissView;
		private final ViewGroup.LayoutParams mLayoutParams;

		public RemoveViewAnimatorUpdateListener(View dismissView) {
			mDismissView = dismissView;
			mLayoutParams = dismissView.getLayoutParams();
		}

		@Override
		public void onAnimationUpdate(ValueAnimator valueAnimator) {
			mLayoutParams.height = (Integer) valueAnimator.getAnimatedValue();
			mDismissView.setLayoutParams(mLayoutParams);
		}
	}

	private class ConfirmListener implements View.OnClickListener {

		private final SwipeDialogView mContextualDialogView;

		public ConfirmListener(SwipeDialogView contextualDialogView) {
			mContextualDialogView = contextualDialogView;
		}

		@Override
		public void onClick(View v) {
			performActionIfNecessary();
		}

	}

	private class CancelListener implements View.OnClickListener {

		private final SwipeDialogView mContextualDialogView;

		public CancelListener(SwipeDialogView contextualDialogView) {
			mContextualDialogView = contextualDialogView;
		}

		@Override
		public void onClick(View v) {
			clearCurrentDialogedView();
			mContextualDialogView.displayContentView();
			moveViewOffScreen();
			animateViewComingBack();
		}

		private void moveViewOffScreen() {
			ViewHelper.setTranslationX(mContextualDialogView,
					mContextualDialogView.getWidth());
		}

		private void animateViewComingBack() {
			animate(mContextualDialogView).translationX(0)
					.setDuration(ANIMATION_DURATION).setListener(null);
		}
	}

	private class RecycleViewListener implements AbsListView.RecyclerListener {
		@Override
		public void onMovedToScrapHeap(View view) {
			Animator animator = mActiveAnimators.get(view);
			if (animator != null) {
				animator.cancel();
			}
		}
	}
}
