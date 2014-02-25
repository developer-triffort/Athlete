/*
 * Copyright 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.athlete.google.android.apps.mytracks.maps;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Canvas;
import android.graphics.Rect;

import com.athlete.google.android.apps.mytracks.MapOverlay.CachedLocation;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.maps.Projection;

/**
 * An interface for classes which paint the track path.
 * 
 * @author Vangelis S.
 */
public interface TrackPath {
	 public boolean updateState();

	  /**
	   * Updates the path.
	   * 
	   * @param startIndex the start index
	   * @param points the points
	   */
	  public void updatePath(GoogleMap googleMap, ArrayList<Polyline> paths, int startIndex,
	      List<CachedLocation> points);
}