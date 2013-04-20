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
package eu.koch.versent.base;

import java.lang.reflect.ParameterizedType;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.SingularAttribute;

public abstract class AbstractBaseDao<E extends SimpleEntity> {

  protected final Class<E> entityClass;

  @SuppressWarnings("unchecked")
  public AbstractBaseDao() {
    this.entityClass = (Class<E>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
  }

  public AbstractBaseDao(Class<E> entityClass) {
    this.entityClass = entityClass;
  }

  abstract protected EntityManager getEntityManager();

  // helper methods for result retrieval

  protected E getSingleResult(final CriteriaQuery<E> query) {
    return this.<E> getTypedSingleResult(query);
  }

  protected <T> T getTypedSingleResult(final CriteriaQuery<T> query) {
    try {
      return getEntityManager().createQuery(query).getSingleResult();
    } catch (NoResultException e) {
      return null;
    }
  }

  protected List<E> getResultList(final CriteriaQuery<E> query) {
    return getEntityManager().createQuery(query).getResultList();
  }

  protected List<E> getResultList(final CriteriaQuery<E> query, int maxresults, int firstresult) {
    return getEntityManager().createQuery(query).setMaxResults(maxresults).setFirstResult(firstresult)
        .getResultList();
  }

  // base helper for query construction

  protected CriteriaBuilder getCriteriaBuilder() {
    return getEntityManager().getCriteriaBuilder();
  }

  /**
   * get attribute by name
   */
  @SuppressWarnings("rawtypes")
  protected SingularAttribute getAttr(String attrName) {
    return getEntityManager().getMetamodel().entity(entityClass).getSingularAttribute(attrName);
  }

  @SuppressWarnings("unchecked")
  protected Predicate getPredicateEqual(Root<E> root, String attrName, Object attrValue) {
    return getCriteriaBuilder().equal(root.get(getAttr(attrName)), attrValue);
  }

  // standard access methods

  public E find(final long id) {
    return getEntityManager().find(entityClass, id);
  }
}
