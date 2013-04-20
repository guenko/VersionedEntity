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
package eu.koch.versent.ejb.dao;

import java.util.Date;
import javax.persistence.EntityManager;
import eu.koch.versent.base.TimeHelper;
import eu.koch.versent.base.VersionedDao;
import eu.koch.versent.ejb.entity.VersionedDepartment;
import eu.koch.versent.ejb.entity.VersionedUser;

public class VersionedUserDao extends VersionedDao<VersionedUser> {

  protected VersionedDepartmentDao versionedDepartmentDao;

  public VersionedUserDao() {
    super(VersionedUser.class);
  }

  public VersionedUserDao(EntityManager entityManager) {
    this();
    init(entityManager);
  }

  @Override
  public void init(EntityManager entityManager) {
    super.init(entityManager);
    versionedDepartmentDao = new VersionedDepartmentDao(entityManager);
  }

  public VersionedUser findByUsername(final String username, Date effectiveTime) {
    effectiveTime = TimeHelper.getNowIfNull(effectiveTime);
    return getSingleResult(getAttributeEqualsQueryVersioned("username", username, effectiveTime));
  }

  public VersionedDepartment getDepartment(VersionedUser user, Date effectiveTime) {
    effectiveTime = TimeHelper.getNowIfNull(effectiveTime);
    return versionedDepartmentDao.getEffectiveVersion(user.getDepartment(), effectiveTime);
  }
}
