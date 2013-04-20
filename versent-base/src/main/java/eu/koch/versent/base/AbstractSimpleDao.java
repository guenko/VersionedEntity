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

import java.util.List;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import eu.koch.versent.base.intf.SimpleDaoIntf;

public abstract class AbstractSimpleDao<E extends SimpleEntity> extends AbstractBaseDao<E> implements
    SimpleDaoIntf<E> {

  public AbstractSimpleDao() {
  }

  public AbstractSimpleDao(Class<E> entityClass) {
    super(entityClass);
  }

  // static helper for query construction for any entity types

  /**
   * add predicate (WHERE clause) to the query
   */
  private static <T> CriteriaQuery<T> addPredicate(CriteriaQuery<T> query, Root<T> root, Predicate predicate) {
    if (predicate != null) {
      query = query.where(predicate);
    }
    return query;
  }

  /**
   * construct a query for the given predicate (WHERE clause) and given orders (ORDER BY clauses)
   */
  protected static <T> CriteriaQuery<T> buildQuery(CriteriaQuery<T> query, Root<T> root, Predicate predicate,
      Order... order) {
    // add select clause
    query = query.select(root);
    // add predicate if exits and order clauses
    return addPredicate(query, root, predicate).orderBy(order);
  }

  /**
   * short hand to construct a query for the given predicate without a ORDER BY clause
   */
  protected static <T> CriteriaQuery<T> buildQuery(CriteriaQuery<T> query, Root<T> root, Predicate predicate) {
    return buildQuery(query, root, predicate, (Order[]) null);
  }

  // query construction

  /**
   * build query with one predicate (WHERE clause) comparing the given attribute with the given value
   */
  protected CriteriaQuery<E> getAttributeEqualsQuery(String attrName, Object attrValue) {
    CriteriaQuery<E> query = getCriteriaBuilder().createQuery(entityClass);
    Root<E> root = query.from(entityClass);

    Predicate predicate = getPredicateEqual(root, attrName, attrValue);
    return buildQuery(query, root, predicate);
  }

  // data access CRUD interface methods

  @Override
  public void insert(final E instance) {
    getEntityManager().persist(instance);
  }

  @Override
  public void update(final E instance) {
    getEntityManager().merge(instance);
    // entityManager.flush();
  }

  @Override
  public void remove(final E instance) {
    boolean contains = getEntityManager().contains(instance);
    E remove = instance;
    if (!contains) {
      remove = find(instance.getId());
    }
    getEntityManager().remove(remove);
  }

  @Override
  public List<E> findAll() {
    CriteriaQuery<E> query = getCriteriaBuilder().createQuery(entityClass);
    query.from(entityClass);
    return getResultList(query);
  }
}
