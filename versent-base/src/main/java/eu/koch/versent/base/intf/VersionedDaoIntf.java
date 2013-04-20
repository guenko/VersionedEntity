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
package eu.koch.versent.base.intf;

import java.util.Date;
import java.util.List;
import eu.koch.versent.base.VersionedEntity;

public interface VersionedDaoIntf<E extends VersionedEntity> {

  public Date insert(final E instance);

  public Date update(final E instance);

  public Date remove(final E instance);

  public Date insert(final E instance, Date effectiveTime);

  public Date update(final E instance, Date effectiveTime);

  public Date remove(final E instance, Date effectiveTime);

  public List<E> getVersions(E entity);

  public E getNextVersion(E entity);

  public E getPreviousVersion(E entity);

  public E getEffectiveVersion(E entity);

  public E getEffectiveVersion(E entity, Date effectiveTime);

  public List<E> getEffectiveVersion(List<E> entityList);

  public List<E> getEffectiveVersion(List<E> entityList, Date effectiveTime);

  public E find(final long id);

  public List<E> findAll();

  public List<E> findAll(Date effectiveTime);

  public List<E> findAllVersions();
}