/*
 * Copyright (c) 2013, guenkogit@gmail.com All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 * 
 * - Redistributions of source code must retain the above copyright notice, this list of conditions and the
 * following disclaimer.
 * 
 * - Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * following disclaimer in the documentation and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
 * DAMAGE.
 */
package eu.koch.versent.web.test;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import eu.koch.versent.base.SimpleEntity;
import eu.koch.versent.base.TimeHelper;
import eu.koch.versent.base.VersionedEntity;

public class TestHelper {

  public static Date getNow() {
    return new Date();
  }

  public static Date getTimeTickBefore(Date pointInTime) {
    return new Date(pointInTime.getTime() - TimeHelper.databaseTimePrecision);
  }

  public static void waitForNextTimeTick(Date pointInTime) {
    try {
      Thread.sleep(TimeHelper.getDatabasePrecision(pointInTime).getTime() + TimeHelper.databaseTimePrecision
          - pointInTime.getTime());
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  static class SimpleEntityComparator implements Comparator<SimpleEntity> {
    @Override
    public int compare(SimpleEntity item1, SimpleEntity item2) {
      return Long.signum(item1.getId() - item2.getId());
    }
  }

  static class VersionedEntityComparator implements Comparator<VersionedEntity> {
    @Override
    public int compare(VersionedEntity item1, VersionedEntity item2) {
      Long diff1 = item1.getChainId() - item2.getChainId();
      if (diff1 != 0) {
        return Long.signum(diff1);
      } else {
        return Long.signum(item1.getId() - item2.getId());
      }
    }
  }

  public static <E extends VersionedEntity> void sortVersioned(List<E> list) {
    Collections.sort(list, new VersionedEntityComparator());
  }

  public static <E extends SimpleEntity> void sortSimple(List<E> list) {
    Collections.sort(list, new SimpleEntityComparator());
  }
}
