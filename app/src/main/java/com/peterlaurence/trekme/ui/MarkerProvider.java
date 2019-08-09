package com.peterlaurence.trekme.ui;

import androidx.annotation.Nullable;

import com.peterlaurence.trekme.core.map.gson.MarkerGson;

/**
 * Any object able to give the current marker requested for editing, and can be notified of a change.
 *
 * @author peterLaurence on 29/04/17.
 */
public interface MarkerProvider {
    @Nullable
    MarkerGson.Marker getCurrentMarker();

    void currentMarkerEdited();
}
