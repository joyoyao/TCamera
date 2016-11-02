package com.abcew.camera.configuration;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import com.abcew.camera.ui.adapter.DataSourceListAdapter;

/**
 * Created by laputan on 16/10/31.
 */
public interface DataSourceInterface<DATA> {

    boolean isDirty();
    void setDirtyFlag(boolean isDirty);

    /**
     * Get the Name
     * @return localized name
     * {@inheritDoc}
     */
    @Nullable
    String getName();

    /**
     * Get the layout res id of the list item view.
     * @return Layout resource.
     */
    @LayoutRes
    int getLayout();

    /**
     * Get the vertical layout res id of the list item view.
     * @return Vertical layout resource.
     */
    @LayoutRes int getVerticalLayout();

    /**
     * Create a ViewHolder to Display in List
     * @param view view inflated with #getLayout
     * @return a new ViewHolder
     */
    @NonNull
    DataSourceListAdapter.DataSourceViewHolder<DATA> createViewHolder(View view, boolean useVerticalLayout);

    /**
     * Bind this Config to ViewHolder.
     * @param view List item view, inflated with #getLayout
     * @param data your custom data loaded by #asyncDataLoad
     */
    //void bind(DataSourceListAdapter.DataSourceViewHolder view, DATA data, boolean selected);

    /**
     * Load custom binding data Synchronous.
     * @return return custom bind data.
     */
    @Nullable
    DATA generateBindData();

    /**
     * Load custom binding data asynchronous.
     * @return return custom bind data.
     */
    @Nullable
    DATA generateBindDataAsync();

    boolean isSelectable();
}