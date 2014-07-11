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

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

@SuppressLint("ViewConstructor")
public class SwipeDialogView extends FrameLayout {

	private View mDialogView;
	private View mContentView;
	
	private long mItemId;

	public SwipeDialogView(Context context, int undoLayoutResId) {
		super(context);
		initDialog(undoLayoutResId);
	}

	private void initDialog(int undoLayoutResId) {
		mDialogView = View.inflate(getContext(), undoLayoutResId, null);
		addView(mDialogView);
	}

	public void updateContentView(View contentView) {
		if (mContentView == null) {
			addView(contentView);
		}
		mContentView = contentView;
	}

	public View getContentView() {
		return mContentView;
	}

	public void setItemId(long itemId) {
		this.mItemId = itemId;
	}

	public long getItemId() {
		return mItemId;
	}

	public boolean isContentDisplayed() {
		return mContentView.getVisibility() == View.VISIBLE;
	}

	public void displayDialog() {
		ViewGroup.LayoutParams lp = mDialogView.getLayoutParams();
		lp.height = mContentView.getHeight();
		mDialogView.setLayoutParams( lp );
		mContentView.setVisibility(View.GONE);
		mDialogView.setVisibility(View.VISIBLE);
	}

	public void displayContentView() {
		mContentView.setVisibility(View.VISIBLE);
		mDialogView.setVisibility(View.GONE);
	}
}
