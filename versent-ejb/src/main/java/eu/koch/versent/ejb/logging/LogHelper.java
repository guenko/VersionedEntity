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
package eu.koch.versent.ejb.logging;

import java.util.List;
import eu.koch.versent.base.TimeHelper;
import eu.koch.versent.base.VersionedEntity;
import eu.koch.versent.ejb.entity.SimpleUser;
import eu.koch.versent.ejb.entity.VersionedUser;
import org.apache.log4j.Logger;

public class LogHelper {
  private static Logger log = Logger.getLogger(LogHelper.class);

  public static void listSimpleUser(SimpleUser user) {
    if (user == null) {
      log.info("user = null");
    } else {
      log.info(String.format("%03d %s %s %s %s", user.getId(), user.getFirstname(), user.getSurname(),
          user.getUsername(), user.getDepartment().getName()));
    }
  }

  public static void listSimpleUsers(List<SimpleUser> userList) {
    for (SimpleUser user : userList) {
      listSimpleUser(user);
    }
  }

  public static String getVersionedAttrStr(VersionedEntity entity) {
    return String.format("%03d %03d %s %s", entity.getChainId(), entity.getId(),
        TimeHelper.getUtcDateTimeStrMilli(entity.getValidFrom()),
        TimeHelper.getUtcDateTimeStrMilli(entity.getValidTo()));
  }

  public static void listVersionedUser(VersionedUser user) {
    if (user == null) {
      log.info("user = null");
    } else {
      log.info(String.format("%s %s %s %s %s", getVersionedAttrStr(user), user.getFirstname(), user.getSurname(),
          user.getUsername(), user.getDepartment().getName()));
    }
  }

  public static void listVersionedUsers(List<VersionedUser> userList) {
    for (VersionedUser user : userList) {
      listVersionedUser(user);
    }
  }
}
