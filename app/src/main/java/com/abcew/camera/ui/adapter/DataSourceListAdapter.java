package com.abcew.camera.ui.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.abcew.camera.R;
import com.abcew.camera.configuration.DataSourceInterface;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by laputan on 16/10/31.
 */
public class DataSourceListAdapter extends RecyclerView.Adapter<DataSourceListAdapter.MultiViewHolder> {
    public final LoaderList loaderList = new LoaderList();
    private final List<DataSourceInterface> dataList = new ArrayList<>();
    private final Context context;
    protected OnItemClickListener onItemClickListener;
    private boolean useVerticalLayout = false;
    private int selectedPosition = -1;

    public DataSourceListAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public MultiViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new MultiViewHolder(new MultiView(context));
    }

    @Override
    public void onBindViewHolder(@NonNull MultiViewHolder holder, int position) {
        onBindViewHolder(holder, position, null);
    }

    @Override
    public void onBindViewHolder(@NonNull MultiViewHolder holder, int position, @Nullable List<Object> payloads) {
        if (payloads != null && payloads.size() > 0) {
            holder.setSelectionState(selectedPosition == position);
        } else {
            holder.bind(getEntityAt(position), selectedPosition == position);
        }
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public DataSourceInterface getEntityAt(int i) {
        return dataList.get(i);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        onItemClickListener = listener;
    }

    @SuppressWarnings("unchecked")
    public void dispatchOnItemClick(DataSourceInterface entity) {
        if (onItemClickListener != null) {
            onItemClickListener.onItemClick(entity);
        }
    }

    public void setData(@NonNull final List<? extends DataSourceInterface> data) {
        // Remove all deleted items.
        for (int i = dataList.size() - 1; i >= 0; --i) {
            if (getLocation(data, dataList.get(i)) < 0) {
                deleteEntity(i);
            }
        }

        // Add and move items.
        for (int i = 0; i < data.size(); ++i) {
            DataSourceInterface entity = data.get(i);
            int loc = getLocation(dataList, entity);
            if (loc < 0) {
                addEntity(i, entity);
            } else if (loc != i) {
                moveEntity(i, loc);
            }
        }
    }

    private int getLocation(@NonNull List<? extends DataSourceInterface> data, @NonNull DataSourceInterface entity) {
        for (int j = 0; j < data.size(); ++j) {
            DataSourceInterface newEntity = data.get(j);
            if (entity.equals(newEntity)) {
                return j;
            }
        }

        return -1;
    }

    public void deleteEntity(int i) {
        dataList.remove(i);
        notifyItemRemoved(i);
    }

    public void addEntity(int i, DataSourceInterface entity) {
        dataList.add(i, entity);
        notifyItemInserted(i);
    }

    public void moveEntity(int i, int loc) {
        move(dataList, i, loc);
        notifyItemMoved(i, loc);
    }

    private void move(@NonNull List<DataSourceInterface> data, int a, int b) {
        DataSourceInterface temp = data.remove(a);
        data.add(b, temp);
        /**TODO:
         * Process: com.photoeditorsdk.android.app, PID: 32404
         java.lang.IndexOutOfBoundsException: Invalid index 3, size is 3
         at java.util.ArrayList.throwIndexOutOfBoundsException(ArrayList.java:255)
         at java.util.ArrayList.remove(ArrayList.java:403)
         at ly.img.android.ui.adapter.DataSourceListAdapter.move(DataSourceListAdapter.java:409)
         at ly.img.android.ui.adapter.DataSourceListAdapter.moveEntity(DataSourceListAdapter.java:404)
         at ly.img.android.ui.adapter.DataSourceListAdapter.setData(DataSourceListAdapter.java:358)
         at ly.img.android.ui.panels.CropToolPanel.onAttached(CropToolPanel.java:42)
         at ly.img.android.sdk.tools.AbstractToolPanel$2.run(AbstractToolPanel.java:87)
         at android.os.Handler.handleCallback(Handler.java:739)
         at android.os.Handler.dispatchMessage(Handler.java:95)
         at android.os.Looper.loop(Looper.java:145)
         at android.app.ActivityThread.main(ActivityThread.java:6837)
         at java.lang.reflect.Method.invoke(Native Method)
         at java.lang.reflect.Method.invoke(Method.java:372)
         at com.android.internal.os.ZygoteInit$MethodAndArgsCaller.run(ZygoteInit.java:1404)
         at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:1199)
         */
    }

    public void invalidateItem(DataSourceInterface item) {
        item.setDirtyFlag(true);
        notifyItemChanged(dataList.indexOf(item));
    }

    public void setSelection(DataSourceInterface item) {
        notifyItemChanged(selectedPosition, new Object()); // Old Deselect
        selectedPosition = dataList.indexOf(item);
        notifyItemChanged(selectedPosition, new Object()); // New Select
    }

    public void setUseVerticalLayout(boolean useVerticalLayout) {
        this.useVerticalLayout = useVerticalLayout;
    }

    private interface OnSetSelectionListener {
        void dispatchSelection();
    }

    public interface OnItemClickListener<T extends DataSourceInterface> {
        void onItemClick(T entity);
    }

    public static abstract class DataSourceViewHolder<DATA> extends RecyclerView.ViewHolder {

        View.OnClickListener clickListener;
        OnSetSelectionListener selectionListener;

        public DataSourceViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        protected abstract void bind(DATA data);

        private void setOnClickListener(View.OnClickListener listener) {
            this.clickListener = listener;
        }

        private void setOnSelectionListener(OnSetSelectionListener selectionListener) {
            this.selectionListener = selectionListener;
        }

        protected void dispatchOnItemClick() {
            if (clickListener != null) {
                clickListener.onClick(itemView);
            }
        }

        public abstract void setSelectedState(boolean selected);

        public void dispatchSelection() {
            if (selectionListener != null) {
                selectionListener.dispatchSelection();
            }
        }
    }

    private static class LoaderList {

        private final Map<MultiViewHolder, DataSourceInterface> holderMap;
        private final List<DataSourceInterface> workerList;


        @NonNull
        private final Lock workerLock;

        @Nullable
        private Task task = null;

        public LoaderList() {
            workerLock = new ReentrantLock();
            holderMap = Collections.synchronizedMap(new LinkedHashMap<MultiViewHolder, DataSourceInterface>());
            workerList = Collections.synchronizedList(new ArrayList<DataSourceInterface>());
        }

        @Nullable
        public DataSourceInterface getLastValue() {
            DataSourceInterface lastKey = null;
            for (DataSourceInterface key : holderMap.values()) {
                if (!workerList.contains(key)) {
                    lastKey = key;
                }
            }
            return lastKey;
        }

        public void put(DataSourceInterface entity, MultiViewHolder holder) {
            workerLock.lock();

            holderMap.remove(holder);

            holderMap.put(holder, entity);
            workerLock.unlock();

            checkStart();
        }

        private synchronized void checkStart() {
            if (task == null && holderMap.size() > 0) {
                task = new Task();
                task.start();
            }
        }

        public void runEntryProcess(@NonNull final DataSourceInterface entity) {
            final Object data = entity.generateBindDataAsync();
            if (data != null) {
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        workerLock.lock();

                        List<MultiViewHolder> removesHolders = new ArrayList<>();

                        for (Map.Entry<MultiViewHolder, DataSourceInterface> mapEntry : holderMap.entrySet()) {
                            DataSourceInterface entry = mapEntry.getValue();
                            if (entry.equals(entity)) {
                                MultiViewHolder holder = mapEntry.getKey();
                                removesHolders.add(holder);
                                holder.bindAsync(entry, data);
                            }
                        }

                        for (MultiViewHolder holder : removesHolders) {
                            holderMap.remove(holder);
                        }

                        //noinspection StatementWithEmptyBody
                        while (workerList.remove(entity)) {
                        }

                        workerLock.unlock();
                    }
                }, 20);
            } else {
                workerList.remove(entity);
            }
        }

        private class Task extends Thread implements Runnable {
            @Override
            public synchronized void run() {
                while (holderMap.size() > 0 && !interrupted() && !isInterrupted()) {
                    workerLock.lock();

                    final DataSourceInterface entity = getLastValue();
                    if (entity != null) {
                        workerList.add(entity);
                        workerLock.unlock();
                        runEntryProcess(entity);
                    } else {
                        workerLock.unlock();
                    }
                }
                try {
                    sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                task = null;
                checkStart();
            }
        }

    }

    protected static final class MultiView extends RelativeLayout {
        @NonNull
        private final ViewGroup viewHolder;
        private final LayoutInflater inflater;
        @NonNull
        private final HashMap<Integer, View> viewTypes;

        @SuppressLint("UseSparseArrays")
        public MultiView(Context context) {
            super(context);

            viewTypes = new HashMap<>();

            inflater = LayoutInflater.from(context);
            View view = inflater.inflate(R.layout.list_item, this, true);
            viewHolder = (ViewGroup) view.findViewById(R.id.multiViewHolder);
        }

        protected synchronized View changeLayout(@LayoutRes int layout) {
            final View view;
            for (Map.Entry<Integer, View> viewType : viewTypes.entrySet()) {
                if (viewType.getKey() != layout) {
                    viewType.getValue().setVisibility(GONE);
                }
            }

            if (!viewTypes.containsKey(layout)) {
                view = inflater.inflate(layout, viewHolder, false);
                viewHolder.addView(view);
                viewTypes.put(layout, view);
            } else {
                view = viewTypes.get(layout);
            }
            view.setVisibility(VISIBLE);

            return view;
        }
    }

    protected class MultiViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, OnSetSelectionListener {

        @NonNull
        public final MultiView multiType;
        @NonNull
        private final SparseArray<DataSourceViewHolder> viewHolderTypes;
        private DataSourceInterface entity;

        MultiViewHolder(@NonNull MultiView v) {
            super(v);
            viewHolderTypes = new SparseArray<>();
            multiType = v;
        }

        public synchronized <DATA> void bind(@NonNull DataSourceInterface<DATA> entity, boolean selected) {
            DataSourceViewHolder<DATA> holder = getViewHolder(entity);

            if (!entity.equals(this.entity) || entity.isDirty()) {
                entity.setDirtyFlag(false);
                this.entity = entity;

                DATA data = entity.generateBindData();
                if (data != null) {
                    holder.bind(data);
                }
                loaderList.put(entity, this);
            }
            setSelectionState(selected);
        }

        @SuppressWarnings("unchecked")
        public synchronized <DATA> DataSourceViewHolder<DATA> getViewHolder(@NonNull DataSourceInterface<DATA> entity) {
            int layout = (useVerticalLayout) ? entity.getVerticalLayout() : entity.getLayout();
            View view = multiType.changeLayout(layout);

            DataSourceViewHolder<DATA> viewHolder = viewHolderTypes.get(layout);
            if (viewHolder == null) {
                viewHolder = entity.createViewHolder(view, useVerticalLayout);
                viewHolder.setOnClickListener(this);
                viewHolder.setOnSelectionListener(this);
                viewHolderTypes.put(layout, viewHolder);
            }

            return viewHolder;
        }

        public void setSelectionState(boolean selected) {
            selected = selected && entity.isSelectable();
            if (entity != null) {
                getViewHolder(entity).setSelectedState(selected);
                multiType.setSelected(selected);
            }
        }

        public synchronized <DATA> void bindAsync(@NonNull DataSourceInterface<DATA> entity, @Nullable DATA data) {
            if (data != null) {
                DataSourceViewHolder<DATA> holder = getViewHolder(entity);

                this.entity = entity;
                holder.bind(data);
            }
        }

        @Override
        public void onClick(View v) {
            dispatchOnItemClick(entity);
        }

        @Override
        public void dispatchSelection() {
            DataSourceListAdapter.this.setSelection(entity);
        }
    }
}