/*
 * Copyright 2012 Google Inc.
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

package com.athlete.google.android.apps.mytracks.services.sensors.ant;

import com.athlete.R;


/**
 * Heart rate monitor channel configuration.
 *
 * @author Jimmy Shih
 */
public class HeartRateChannelConfiguration extends ChannelConfiguration {

  private static final int DEVICE_ID_KEY = R.string.ant_heart_rate_monitor_id_key;
  private static final byte DEVICE_TYPE = 0x78;
  private static final short MESSAGE_PERIOD = 8070;

  @Override
  public int getDeviceIdKey() {
    return DEVICE_ID_KEY;
  }

  @Override
  public byte getDeviceType() {
    return DEVICE_TYPE;
  }

  @Override
  public short getMessagPeriod() {
    return MESSAGE_PERIOD;
  }

  @Override
  public void decodeMessage(byte[] message, AntSensorValue antSensorValue) {
    antSensorValue.setHeartRate(message[10] & 0xFF);
  }
}
