package com.peterlaurence.trekme.ui.maplist;

import android.content.Context;
import android.graphics.Color;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.peterlaurence.trekme.R;
import com.peterlaurence.trekme.core.map.Map;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Adapter to provide access to the data set (here a list of {@link Map}).
 * <p/>
 *
 * @author peterLaurence on 26/12/15.
 */
public class MapAdapter extends RecyclerView.Adapter<MapAdapter.MapViewHolder> {
    private List<Map> maps;
    private MapSelectionListener mMapSelectionListener;
    private MapSettingsListener mMapSettingsListener;
    private MapDeleteListener mMapDeleteListener;

    private int selectedMapIndex = -1;
    private int previousSelectedMapIndex = -1;

    private int mColorAccent;
    private int mColorWhiteText;
    private int mColorBlackText;

    MapAdapter(@Nullable List<Map> maps, MapSelectionListener mapSelectionListener,
               MapSettingsListener mapSettingsListener, MapDeleteListener mapDeleteListener,
               int accentColor, int whiteTextColor,
               int blackTextColor) {
        this.maps = maps;
        mMapSelectionListener = mapSelectionListener;
        mMapSettingsListener = mapSettingsListener;
        mMapDeleteListener = mapDeleteListener;

        mColorAccent = accentColor;
        mColorWhiteText = whiteTextColor;
        mColorBlackText = blackTextColor;
    }

    void onMapListUpdate(List<Map> mapList) {
        maps = mapList;
        notifyDataSetChanged();
    }

    /**
     * Simple implementation of a toggle selection. When an item is clicked, we change its
     * background and we remember his index. When another item is clicked, the background of the
     * first item is set to its original state.
     *
     * @param position index of the selected view
     */
    private void updateSelectionColor(int position) {
        selectedMapIndex = position;
        notifyItemChanged(position);
        if (previousSelectedMapIndex != -1) {
            notifyItemChanged(previousSelectedMapIndex);
        }
        previousSelectedMapIndex = position;
    }

    @Override
    public MapViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context ctx = parent.getContext();
        View v = LayoutInflater.from(ctx).inflate(R.layout.map_row, parent, false);

        return new MapViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MapViewHolder holder, int position) {
        final Map map = maps.get(position);
        holder.mapImage.setImageBitmap(map.getImage());
        holder.mapName.setText(map.getName());
        holder.calibrationStatus.setText(map.getDescription());

        if (holder.getLayoutPosition() == selectedMapIndex) {
            holder.cardView.setCardBackgroundColor(mColorAccent);
            holder.mapName.setTextColor(mColorWhiteText);
            holder.editButton.setTextColor(mColorWhiteText);
            holder.deleteButton.setColorFilter(mColorWhiteText);
            holder.calibrationStatus.setTextColor(mColorWhiteText);
        } else {
            holder.cardView.setCardBackgroundColor(Color.WHITE);
            holder.mapName.setTextColor(mColorBlackText);
            holder.editButton.setTextColor(mColorAccent);
            holder.deleteButton.setColorFilter(mColorAccent);
        }
        switch (map.getCalibrationStatus()) {
            case OK:
                holder.calibrationStatus.setText(R.string.calibration_status_ok);
                break;
            case NONE:
                holder.calibrationStatus.setText(R.string.calibration_status_none);
                break;
            case ERROR:
                holder.calibrationStatus.setText(R.string.calibration_status_error);
                break;
        }

        /* Set click listeners */
        holder.itemView.setOnClickListener(new MapViewHolderClickListener(holder, this));
        holder.editButton.setOnClickListener(new SettingsButtonClickListener(holder, this));
        holder.deleteButton.setOnClickListener(new DeleteButtonClickListener(holder, this));
    }

    @Override
    public int getItemCount() {
        return maps == null ? 0 : maps.size();
    }

    /**
     * When an item gets selected, the {@link MapSelectionListener} is called with the corresponding
     * {@link Map}.
     */
    public interface MapSelectionListener {
        void onMapSelected(Map m);
    }

    /**
     * When the settings button of an item is clicked, the {@link MapSettingsListener} is called
     * with the corresponding {@link Map}.
     */
    public interface MapSettingsListener {
        void onMapSettings(Map m);
    }

    /**
     * When the deletion of a {@link Map} is confirmed, the {@link MapDeleteListener} is called with the
     * corresponding {@link Map}.
     */
    public interface MapDeleteListener {
        void onMapDelete(Map map);
    }

    /**
     * The view for each {@link Map}
     */
    public static class MapViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView mapName;
        TextView calibrationStatus;
        ImageView mapImage;
        Button editButton;
        ImageButton deleteButton;

        public MapViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cv);
            mapName = itemView.findViewById(R.id.map_name);
            calibrationStatus = itemView.findViewById(R.id.map_calibration_status);
            mapImage = itemView.findViewById(R.id.map_preview_image);
            editButton = itemView.findViewById(R.id.map_manage_btn);
            deleteButton = itemView.findViewById(R.id.map_delete_btn);
        }
    }

    /**
     * The generic click listener for a button of a {@link MapViewHolder}. <br>
     * It has a reference to the {@link MapAdapter} as it needs to access the {@link Map} container.
     * <p>
     */
    private static abstract class ButtonClickListener implements View.OnClickListener {
        WeakReference<MapViewHolder> mMapViewHolderWeakReference;
        WeakReference<MapAdapter> mMapAdapterWeakReference;

        ButtonClickListener(MapViewHolder mapViewHolder, MapAdapter mapAdapter) {
            mMapViewHolderWeakReference = new WeakReference<>(mapViewHolder);
            mMapAdapterWeakReference = new WeakReference<>(mapAdapter);
        }

        @Override
        public void onClick(View v) {
            if (mMapAdapterWeakReference != null && mMapViewHolderWeakReference != null) {
                MapAdapter mapAdapter = mMapAdapterWeakReference.get();
                MapViewHolder mapViewHolder = mMapViewHolderWeakReference.get();

                if (mapViewHolder != null && mapAdapter != null) {
                    Map map = mapAdapter.maps.get(mapViewHolder.getAdapterPosition());
                    if (mapAdapter.mMapSettingsListener != null) {
                        clickAction(mapAdapter, map);
                    }
                }
            }
        }

        public abstract void clickAction(MapAdapter mapAdapter, Map map);
    }

    /**
     * The click listener for the settings button of a {@link MapViewHolder}
     */
    private static class SettingsButtonClickListener extends ButtonClickListener {
        SettingsButtonClickListener(MapViewHolder mapViewHolder, MapAdapter mapAdapter) {
            super(mapViewHolder, mapAdapter);
        }

        @Override
        public void clickAction(MapAdapter mapAdapter, Map map) {
            mapAdapter.mMapSettingsListener.onMapSettings(map);
        }
    }

    /**
     * The click listener for the delete button of a {@link MapViewHolder}
     */
    private static class DeleteButtonClickListener extends ButtonClickListener {
        DeleteButtonClickListener(MapViewHolder mapViewHolder, MapAdapter mapAdapter) {
            super(mapViewHolder, mapAdapter);
        }

        @Override
        public void clickAction(MapAdapter mapAdapter, Map map) {
            mapAdapter.mMapDeleteListener.onMapDelete(map);
        }
    }

    /**
     * The click listener for a {@link MapViewHolder}.
     * It has a reference to the {@link MapAdapter} as it needs to access the {@link Map} container
     * and call some methods.
     */
    private static class MapViewHolderClickListener implements View.OnClickListener {
        WeakReference<MapViewHolder> mMapViewHolderWeakReference;
        WeakReference<MapAdapter> mMapAdapterWeakReference;

        MapViewHolderClickListener(MapViewHolder mapViewHolder, MapAdapter mapAdapter) {
            mMapViewHolderWeakReference = new WeakReference<>(mapViewHolder);
            mMapAdapterWeakReference = new WeakReference<>(mapAdapter);
        }

        @Override
        public void onClick(View v) {
            if (mMapAdapterWeakReference != null && mMapViewHolderWeakReference != null) {
                MapAdapter mapAdapter = mMapAdapterWeakReference.get();
                MapViewHolder mapViewHolder = mMapViewHolderWeakReference.get();
                if (mapViewHolder != null && mapAdapter != null) {
                    int position = mapViewHolder.getAdapterPosition();

                    // Toggle background color
                    mapAdapter.updateSelectionColor(position);

                    // Call the listener for Map selection
                    Map map = mapAdapter.maps.get(position);
                    mapAdapter.mMapSelectionListener.onMapSelected(map);
                }
            }
        }
    }
}
