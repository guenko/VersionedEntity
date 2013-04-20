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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.apache.log4j.Logger;
import eu.koch.versent.base.intf.VersionedDaoIntf;

public abstract class AbstractVersionedDao<E extends VersionedEntity> extends AbstractBaseDao<E> implements
    VersionedDaoIntf<E> {

  private static final Logger log = Logger.getLogger(AbstractVersionedDao.class);

  // the marker indicating a valid period lasting forever
  public static final Date END_OF_TIME = TimeHelper.END_OF_TIME;

  public AbstractVersionedDao() {
  }

  public AbstractVersionedDao(Class<E> entityClass) {
    super(entityClass);
  }

  // helper for query construction

  /**
   * Construct a validFromValidTo predicate related to the effectiveTime: validFrom >= effectiveTime AND validTo <
   * effectiveTime
   */
  protected Predicate getValidFromValidToPredicate(Root<E> root, Date effectiveTime) {
    effectiveTime = TimeHelper.checkForEndOfTime(effectiveTime);

    @SuppressWarnings("unchecked")
    Predicate predicate1 = getCriteriaBuilder().lessThanOrEqualTo(
        root.get(getAttr(VersionedEntity.ATTR_VALID_FROM)), effectiveTime);

    @SuppressWarnings("unchecked")
    Predicate predicate2 = getCriteriaBuilder().greaterThan(root.get(getAttr(VersionedEntity.ATTR_VALID_TO)),
        effectiveTime);

    return getCriteriaBuilder().and(predicate1, predicate2);
  }

  /**
   * add predicate (WHERE clause) and the valid-to/valid-from clause to the query
   */
  protected CriteriaQuery<E> addPredicateVersioned(CriteriaQuery<E> query, Root<E> root, Predicate predicate,
      Date effectiveTime) {
    // get validFromValidTo predicate related to effectiveTime
    Predicate predicateValidFromValidTo = getValidFromValidToPredicate(root, effectiveTime);
    if (predicate == null) {
      return query.where(predicateValidFromValidTo);
    } else {
      return query.where(predicate, predicateValidFromValidTo);
    }
  }

  /**
   * construct a query for the given predicate (WHERE clause) and given orders (ORDER BY clauses)
   */
  protected CriteriaQuery<E> buildQueryVersioned(CriteriaQuery<E> query, Root<E> root, Predicate predicate,
      Date effectiveTime, Order... order) {
    // add select clause
    query = query.select(root);
    // add predicate if exits and order clauses
    return addPredicateVersioned(query, root, predicate, effectiveTime).orderBy(order);
  }

  /**
   * short hand to construct a query for the given predicate without a ORDER BY clause
   */
  protected CriteriaQuery<E> buildQueryVersioned(CriteriaQuery<E> query, Root<E> root, Predicate predicate,
      Date effectiveTime) {
    return buildQueryVersioned(query, root, predicate, effectiveTime, (Order[]) null);
  }

  // query construction

  /**
   * build query with one predicate (WHERE clause) comparing the given attribute with the given value
   */
  protected CriteriaQuery<E> getAttributeEqualsQueryVersioned(String attrName, Object attrValue, Date effectiveTime) {
    CriteriaQuery<E> query = getCriteriaBuilder().createQuery(entityClass);
    Root<E> root = query.from(entityClass);

    Predicate predicate = getPredicateEqual(root, attrName, attrValue);
    return buildQueryVersioned(query, root, predicate, effectiveTime);
  }

  // data access CRUD interface methods

  @Override
  public Date insert(final E instance) {
    return insert(instance, null);
  }

  @Override
  public Date insert(final E instance, Date effectiveTime) {
    Date now = TimeHelper.getNow();
    effectiveTime = TimeHelper.contrainUpdateTime(effectiveTime, now);

    instance.setValidFrom(effectiveTime);
    instance.setValidTo(END_OF_TIME);
    instance.setChainId(0L);
    getEntityManager().persist(instance);
    instance.setChainId(instance.getId());
    getEntityManager().merge(instance);
    return effectiveTime;
  }

  @Override
  public Date update(final E instance) {
    return update(instance, null);
  }

  @Override
  public Date update(final E instance, Date effectiveTime) {
    Date now = TimeHelper.getNow();
    effectiveTime = TimeHelper.contrainUpdateTime(effectiveTime, now);

    // log.info("instance.getValidTo(): " + TimeHelper.getUtcDateTimeStrMilli(instance.getValidTo()) +
    // " MAX_DATE: "
    // + TimeHelper.getUtcDateTimeStrMilli(MAX_DATE));
    if (!TimeHelper.databaseEqual(instance.getValidTo(), END_OF_TIME)) {
      throw new java.lang.IllegalArgumentException(
          "only updates of either the current or if exists, the future record, are allowed");
    }

    // is this a future record?
    if (TimeHelper.isFuture(instance, now)) {

      if (TimeHelper.databaseEqual(instance.getValidFrom(), effectiveTime)) {
        log.info("update future record without valid-from change");
      } else {
        log.info("update future and current record because of valid-from change");
        // valid-from has changed, need to update the previous record too
        E previousInstance = getPreviousVersion(instance);
        previousInstance.setValidTo(effectiveTime);
        getEntityManager().merge(previousInstance);

        // set the new valid-from at the future record
        instance.setValidFrom(effectiveTime);
      }
      getEntityManager().merge(instance);

      // is this a current record?
    } else if (TimeHelper.isCurrent(instance, now)) {

      log.info("create new current record and update former current records valid-to");
      // remember this id
      Long formerId = instance.getId();
      // detach this instance as we do not want to merge (update the database row)
      getEntityManager().detach(instance);

      // rather insert as new row to the database, valid from the effective point in time on
      instance.setValidFrom(effectiveTime);
      instance.setValidTo(END_OF_TIME);
      instance.setId(null);
      getEntityManager().persist(instance);

      // reload old instance and update the valid-to attribute (instance is not valid any longer)
      E formerInstance = getEntityManager().find(entityClass, formerId);
      // Certain code will not work if valid-from equals to valid-to
      if (TimeHelper.databaseEqual(formerInstance.getValidFrom(), effectiveTime)) {
        throw new java.lang.IllegalStateException("too fast update within database time precision");
      }
      // the former row has to be the last row in the version chain
      if (!TimeHelper.databaseEqual(formerInstance.getValidTo(), END_OF_TIME)) {
        throw new java.lang.IllegalArgumentException(
            "change of current record not allowed as future record exists");
      }
      formerInstance.setValidTo(effectiveTime);

      getEntityManager().merge(formerInstance);
    } else {
      throw new java.lang.IllegalArgumentException("is neither a current nor a future record");
    }

    return effectiveTime;
  }

  @Override
  public Date remove(final E instance) {
    return remove(instance, null);
  }

  @Override
  public Date remove(final E instance, Date effectiveTime) {
    Date now = TimeHelper.getNow();
    effectiveTime = TimeHelper.contrainUpdateTime(effectiveTime, now);

    E remove = find(instance.getId());

    // is this a future record?
    if (TimeHelper.isFuture(remove, now)) {
      log.info("remove future record and update current record");
      // update valid-to of current record to infinitive
      E previousInstance = getPreviousVersion(remove);
      previousInstance.setValidTo(END_OF_TIME);
      getEntityManager().merge(previousInstance);
      // remove future record
      getEntityManager().remove(remove);

      // is this a current record?
    } else if (TimeHelper.isCurrent(remove, now)) {
      log.info("update valid-to of current record ");
      if (remove.getValidTo() != END_OF_TIME) {
        // check if this is the last record in the chain
        if (getNextVersion(remove) != null) {
          throw new java.lang.IllegalArgumentException(
              "removal of current record is not allowed at exiting future record");
        }
      }
      if (TimeHelper.databaseEqual(remove.getValidFrom(), effectiveTime)) {
        throw new java.lang.IllegalStateException("too fast removal within database time precision");
      }

      // do not remove, rather update the valid-to, so instance is not valid after the given point in time
      remove.setValidTo(effectiveTime);
      getEntityManager().merge(remove);
    } else {
      throw new java.lang.IllegalArgumentException("is neither a current nor a future record");
    }
    return effectiveTime;
  }

  @Override
  public E getPreviousVersion(E entity) {
    CriteriaQuery<E> query = getCriteriaBuilder().createQuery(entityClass);
    Root<E> root = query.from(entityClass);

    Predicate predicate1 = getPredicateEqual(root, VersionedEntity.ATTR_CHAIN_ID, entity.getChainId());
    Predicate predicate2 = getPredicateEqual(root, VersionedEntity.ATTR_VALID_TO, entity.getValidFrom());

    // need non-versioned access
    return getSingleResult(AbstractSimpleDao.buildQuery(query, root,
        getCriteriaBuilder().and(predicate1, predicate2)));
  }

  @Override
  public E getNextVersion(E entity) {
    CriteriaQuery<E> query = getCriteriaBuilder().createQuery(entityClass);
    Root<E> root = query.from(entityClass);

    Predicate predicate1 = getPredicateEqual(root, VersionedEntity.ATTR_CHAIN_ID, entity.getChainId());
    Predicate predicate2 = getPredicateEqual(root, VersionedEntity.ATTR_VALID_FROM, entity.getValidTo());

    // need non-versioned access
    return getSingleResult(AbstractSimpleDao.buildQuery(query, root,
        getCriteriaBuilder().and(predicate1, predicate2)));
  }

  @Override
  public List<E> getVersions(E entity) {
    CriteriaQuery<E> query = getCriteriaBuilder().createQuery(entityClass);
    Root<E> root = query.from(entityClass);

    Predicate predicate = getPredicateEqual(root, VersionedEntity.ATTR_CHAIN_ID, entity.getChainId());
    @SuppressWarnings("unchecked")
    Order order = getCriteriaBuilder().asc(root.get(getAttr(VersionedEntity.ATTR_VALID_FROM)));

    // need non-versioned access
    return getResultList(AbstractSimpleDao.buildQuery(query, root, predicate, order));
  }

  @Override
  public E getEffectiveVersion(E entity) {
    return getEffectiveVersion(entity, null);
  }

  @Override
  public E getEffectiveVersion(E entity, Date effectiveTime) {
    effectiveTime = TimeHelper.getNowIfNull(effectiveTime);

    CriteriaQuery<E> query = getCriteriaBuilder().createQuery(entityClass);
    Root<E> root = query.from(entityClass);

    Predicate predicate1 = getPredicateEqual(root, VersionedEntity.ATTR_CHAIN_ID, entity.getChainId());
    return getTypedSingleResult(buildQueryVersioned(query, root, predicate1, effectiveTime));
  }

  @Override
  public List<E> getEffectiveVersion(List<E> entityList) {
    return getEffectiveVersion(entityList, null);
  }

  @Override
  public List<E> getEffectiveVersion(List<E> entityList, Date effectiveTime) {
    effectiveTime = TimeHelper.getNowIfNull(effectiveTime);

    List<E> resultList = new ArrayList<E>();
    for (E entity : entityList) {
      resultList.add(getEffectiveVersion(entity, effectiveTime));
    }
    return resultList;
  }

  @Override
  public List<E> findAll() {
    return findAll(null);
  }

  @Override
  public List<E> findAll(Date effectiveTime) {
    effectiveTime = TimeHelper.getNowIfNull(effectiveTime);

    CriteriaQuery<E> query = getCriteriaBuilder().createQuery(entityClass);
    Root<E> root = query.from(entityClass);
    return getResultList(buildQueryVersioned(query, root, null, effectiveTime));
  }

  @Override
  public List<E> findAllVersions() {
    CriteriaQuery<E> query = getCriteriaBuilder().createQuery(entityClass);
    Root<E> root = query.from(entityClass);

    @SuppressWarnings("unchecked")
    Order order1 = getCriteriaBuilder().asc(root.get(getAttr(VersionedEntity.ATTR_CHAIN_ID)));
    @SuppressWarnings("unchecked")
    Order order2 = getCriteriaBuilder().asc(root.get(getAttr(VersionedEntity.ATTR_VALID_FROM)));

    // need non-versioned access
    return getResultList(AbstractSimpleDao.buildQuery(query, root, null, order1, order2));
  }
}
