/*
 * Copyright 2018 LinkedIn Corp. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package com.github.ambry.store;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.ambry.utils.SystemTime;
import com.github.ambry.utils.TestUtils;
import com.github.ambry.utils.Utils;
import org.junit.Test;

import static org.junit.Assert.*;


/**
 * Tests for {@link MessageInfo}
 */
public class MessageInfoTest {

  /**
   * Tests all constructors and getters
   */
  @Test
  public void basicAllConstructorsTest() {
    short[] accountIds = {100, 101, 102, 103};
    short[] containerIds = {10, 11, 12, 13};
    boolean[] isDeletedVals = {false, true, false, true};
    boolean[] isTtlUpdatedVals = {true, false, false, true};
    boolean[] isUndeletedVals = {true, false};
    short[] lifeVersionVals = {(short) 0, (short) 1, (short) 2, (short) 3};
    Long[] crcs = {null, 100L, Long.MIN_VALUE, Long.MAX_VALUE};
    StoreKey[] keys = {new MockId(TestUtils.getRandomString(10), accountIds[0], containerIds[0]),
        new MockId(TestUtils.getRandomString(10), accountIds[1], containerIds[1]),
        new MockId(TestUtils.getRandomString(10), accountIds[2], containerIds[2]),
        new MockId(TestUtils.getRandomString(10), accountIds[3], containerIds[3])};
    long[] blobSizes = {1024, 2048, 4096, 8192};
    long[] times = {SystemTime.getInstance().milliseconds() + 100, SystemTime.getInstance().milliseconds() - 1,
        SystemTime.getInstance().milliseconds() + 300, Utils.Infinite_Time};

    for (int j = 0; j < isUndeletedVals.length; j++) {
      for (int i = 0; i < keys.length; i++) {
        MessageInfo info =
            new MessageInfo(keys[i], blobSizes[i], isDeletedVals[i], isTtlUpdatedVals[i], times[i], crcs[i],
                accountIds[i], containerIds[i], times[i]);
        checkGetters(info, keys[i], blobSizes[i], isDeletedVals[i], isTtlUpdatedVals[i], false, times[i], crcs[i],
            accountIds[i], containerIds[i], times[i], (short) 0);

        info = new MessageInfo(keys[i], blobSizes[i], accountIds[i], containerIds[i], times[i]);
        checkGetters(info, keys[i], blobSizes[i], false, false, false, Utils.Infinite_Time, null, accountIds[i],
            containerIds[i], times[i], (short) 0);

        info = new MessageInfo(keys[i], blobSizes[i], isDeletedVals[i], isTtlUpdatedVals[i], times[i], accountIds[i],
            containerIds[i], times[i]);
        checkGetters(info, keys[i], blobSizes[i], isDeletedVals[i], isTtlUpdatedVals[i], false, times[i], null,
            accountIds[i], containerIds[i], times[i], (short) 0);

        info = new MessageInfo(keys[i], blobSizes[i], isDeletedVals[i], isTtlUpdatedVals[i], accountIds[i],
            containerIds[i], times[i]);
        checkGetters(info, keys[i], blobSizes[i], isDeletedVals[i], isTtlUpdatedVals[i], false, Utils.Infinite_Time,
            null, accountIds[i], containerIds[i], times[i], (short) 0);

        info = new MessageInfo(keys[i], blobSizes[i], times[i], accountIds[i], containerIds[i], times[i]);
        checkGetters(info, keys[i], blobSizes[i], false, false, false, times[i], null, accountIds[i], containerIds[i],
            times[i], (short) 0);

        info =
            new MessageInfo(keys[i], blobSizes[i], isDeletedVals[i], isTtlUpdatedVals[i], isUndeletedVals[j], times[i],
                crcs[i], accountIds[i], containerIds[i], times[i], lifeVersionVals[i]);
        checkGetters(info, keys[i], blobSizes[i], isDeletedVals[i], isTtlUpdatedVals[i], isUndeletedVals[j], times[i],
            crcs[i], accountIds[i], containerIds[i], times[i], lifeVersionVals[i]);
      }
    }
  }

  /**
   * Tests for MessageInfo.Builder.
   */
  @Test
  public void testBuilder() {
    short accountId = 100, containerId = 1000;
    StoreKey key = new MockId(TestUtils.getRandomString(10), accountId, containerId);
    long size = 12345;
    long expirationTime = SystemTime.getInstance().milliseconds() - 1;
    long operationTime = SystemTime.getInstance().milliseconds() + 300;
    Long crc = 1000L;
    short lifeVersion = 5;
    MessageInfo origin =
        new MessageInfo(key, size, false, false, false, expirationTime, crc, accountId, containerId, operationTime,
            lifeVersion);

    MessageInfo.Builder builder = new MessageInfo.Builder(origin);
    MessageInfo info = builder.build();
    checkGetters(info, origin.getStoreKey(), origin.getSize(), origin.isDeleted(), origin.isTtlUpdated(),
        origin.isUndeleted(), origin.getExpirationTimeInMs(), origin.getCrc(), origin.getAccountId(),
        origin.getContainerId(), origin.getOperationTimeMs(), origin.getLifeVersion());

    StoreKey newKey = new MockId(TestUtils.getRandomString(10), accountId, containerId);
    info = builder.storeKey(newKey).build();
    checkGetters(info, newKey, origin.getSize(), origin.isDeleted(), origin.isTtlUpdated(), origin.isUndeleted(),
        origin.getExpirationTimeInMs(), origin.getCrc(), origin.getAccountId(), origin.getContainerId(),
        origin.getOperationTimeMs(), origin.getLifeVersion());

    long newSize = size + 1000L;
    info = builder.size(newSize).build();
    checkGetters(info, newKey, newSize, origin.isDeleted(), origin.isTtlUpdated(), origin.isUndeleted(),
        origin.getExpirationTimeInMs(), origin.getCrc(), origin.getAccountId(), origin.getContainerId(),
        origin.getOperationTimeMs(), origin.getLifeVersion());

    info = builder.isDeleted(true).build();
    checkGetters(info, newKey, newSize, true, origin.isTtlUpdated(), origin.isUndeleted(),
        origin.getExpirationTimeInMs(), origin.getCrc(), origin.getAccountId(), origin.getContainerId(),
        origin.getOperationTimeMs(), origin.getLifeVersion());

    info = builder.isTtlUpdated(true).build();
    checkGetters(info, newKey, newSize, true, true, origin.isUndeleted(), origin.getExpirationTimeInMs(),
        origin.getCrc(), origin.getAccountId(), origin.getContainerId(), origin.getOperationTimeMs(),
        origin.getLifeVersion());

    info = builder.isUndeleted(true).build();
    checkGetters(info, newKey, newSize, true, true, true, origin.getExpirationTimeInMs(), origin.getCrc(),
        origin.getAccountId(), origin.getContainerId(), origin.getOperationTimeMs(), origin.getLifeVersion());

    long newExpirationTime = expirationTime + 1000L;
    info = builder.expirationTimeInMs(newExpirationTime).build();
    checkGetters(info, newKey, newSize, true, true, true, newExpirationTime, origin.getCrc(), origin.getAccountId(),
        origin.getContainerId(), origin.getOperationTimeMs(), origin.getLifeVersion());

    Long newCrc = null;
    info = builder.crc(newCrc).build();
    checkGetters(info, newKey, newSize, true, true, true, newExpirationTime, newCrc, origin.getAccountId(),
        origin.getContainerId(), origin.getOperationTimeMs(), origin.getLifeVersion());

    short newAccountId = 101;
    info = builder.accountId(newAccountId).build();
    checkGetters(info, newKey, newSize, true, true, true, newExpirationTime, newCrc, newAccountId,
        origin.getContainerId(), origin.getOperationTimeMs(), origin.getLifeVersion());

    short newContainerId = 10001;
    info = builder.containerId(newContainerId).build();
    checkGetters(info, newKey, newSize, true, true, true, newExpirationTime, newCrc, newAccountId, newContainerId,
        origin.getOperationTimeMs(), origin.getLifeVersion());

    long newOperationTime = operationTime - 1000L;
    info = builder.operationTimeMs(newOperationTime).build();
    checkGetters(info, newKey, newSize, true, true, true, newExpirationTime, newCrc, newAccountId, newContainerId,
        newOperationTime, origin.getLifeVersion());

    short newLifeVersion = (short) (lifeVersion + 1);
    info = builder.lifeVersion(newLifeVersion).build();
    checkGetters(info, newKey, newSize, true, true, true, newExpirationTime, newCrc, newAccountId, newContainerId,
        newOperationTime, newLifeVersion);

    info = new MessageInfo.Builder(newKey, newSize, newAccountId, newContainerId, newOperationTime).build();
    checkGetters(info, newKey, newSize, false, false, false, Utils.Infinite_Time, null, newAccountId, newContainerId,
        newOperationTime, (short) 0);
  }

  /**
   * Test for serialize and deserialize {@link MessageInfo} using jackson library.
   * @throws Exception
   */
  @Test
  public void testJsonSerialization() throws Exception {
    ObjectMapper objectMapper = new ObjectMapper();
    StoreKeyJacksonConfig.setupObjectMapper(objectMapper, new MockIdFactory());
    short accountId = 100, containerId = 1000;
    StoreKey key = new MockId(TestUtils.getRandomString(10), accountId, containerId);
    long size = 12345;
    long expirationTime = SystemTime.getInstance().milliseconds() - 1;
    long operationTime = SystemTime.getInstance().milliseconds() + 300;
    Long crc = 1000L;
    short lifeVersion = 5;
    MessageInfo origin =
        new MessageInfo(key, size, false, false, false, expirationTime, crc, accountId, containerId, operationTime,
            lifeVersion);

    String jsonString = objectMapper.writeValueAsString(origin);
    MessageInfo deserialized = objectMapper.readValue(jsonString, MessageInfo.class);

    checkGetters(deserialized, origin.getStoreKey(), origin.getSize(), origin.isDeleted(), origin.isTtlUpdated(),
        origin.isUndeleted(), origin.getExpirationTimeInMs(), origin.getCrc(), origin.getAccountId(),
        origin.getContainerId(), origin.getOperationTimeMs(), origin.getLifeVersion());

    origin = new MessageInfo(key, size, true, true, true, expirationTime, crc, accountId, containerId, operationTime,
        lifeVersion);

    jsonString = objectMapper.writeValueAsString(origin);
    deserialized = objectMapper.readValue(jsonString, MessageInfo.class);

    checkGetters(deserialized, origin.getStoreKey(), origin.getSize(), origin.isDeleted(), origin.isTtlUpdated(),
        origin.isUndeleted(), origin.getExpirationTimeInMs(), origin.getCrc(), origin.getAccountId(),
        origin.getContainerId(), origin.getOperationTimeMs(), origin.getLifeVersion());
  }

  /**
   * Checks getters of {@code info}
   * @param info the {@link MessageInfo} whose props need to checked
   * @param key the expected {@link StoreKey} in {@code info}.
   * @param size the expected {@link StoreKey} in {@code info}.
   * @param isDeleted the expected deleted state in {@code info}.
   * @param isTtlUpdated the expected ttl updated state in {@code info}.
   * @param isUndeleted the expected undeleted state in {@code info}.
   * @param expiresAtMs the expected expiry time (ms) in {@code info}.
   * @param crc the expected crc in {@code info}.
   * @param accountId the expected account id in {@code info}.
   * @param containerId the expected container id in {@code info}.
   * @param operationTimeMs the expected operation time (ms) in {@code info}.
   * @param lifeVersion the expected lifeVersion in {@code info}.
   */
  public static void checkGetters(MessageInfo info, StoreKey key, long size, boolean isDeleted, boolean isTtlUpdated,
      boolean isUndeleted, long expiresAtMs, Long crc, short accountId, short containerId, long operationTimeMs,
      short lifeVersion) {
    assertEquals("Key not as expected", key, info.getStoreKey());
    assertEquals("Size not as expected", size, info.getSize());
    assertEquals("Expiration time not as expected", expiresAtMs, info.getExpirationTimeInMs());
    // this works assuming that the test takes < 100 ms to execute
    assertEquals("isExpired not as expected",
        expiresAtMs != Utils.Infinite_Time && expiresAtMs < System.currentTimeMillis(), info.isExpired());
    assertEquals("isDeleted not as expected", isDeleted, info.isDeleted());
    assertEquals("isTtlUpdated not as expected", isTtlUpdated, info.isTtlUpdated());
    assertEquals("isUndeleted not as expected", isUndeleted, info.isUndeleted());
    assertEquals("Crc not as expected", crc, info.getCrc());
    assertEquals("Account ID not as expected", accountId, info.getAccountId());
    assertEquals("Container ID not as expected", containerId, info.getContainerId());
    assertEquals("Op time not as expected", operationTimeMs, info.getOperationTimeMs());
    assertEquals("lifeVersion not as expected", lifeVersion, info.getLifeVersion());
  }
}
