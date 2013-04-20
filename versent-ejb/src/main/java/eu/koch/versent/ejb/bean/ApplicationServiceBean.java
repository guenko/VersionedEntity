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
package eu.koch.versent.ejb.bean;

import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import eu.koch.versent.appl.intf.ApplicationService;
import eu.koch.versent.ejb.dao.SimpleUserDao;
import eu.koch.versent.ejb.dao.VersionedUserDao;
import eu.koch.versent.ejb.entity.SimpleUser;
import eu.koch.versent.ejb.entity.VersionedUser;
import eu.koch.versent.ejb.logging.LogHelper;
import org.apache.log4j.Logger;

@Stateless
public class ApplicationServiceBean implements ApplicationService {

  private static Logger log = Logger.getLogger(ApplicationServiceBean.class);

  @PersistenceContext
  private EntityManager entityManager;

  private SimpleUserDao simpleUserDao;
  private VersionedUserDao versionedUserDao;

  @PostConstruct
  void init() {
    simpleUserDao = new SimpleUserDao(entityManager);
    versionedUserDao = new VersionedUserDao(entityManager);
  }

  private static void listAllSimpleUsers(SimpleUserDao userDao) {
    List<SimpleUser> userList = userDao.findAll();
    log.info("findAll: " + userList.size());
    LogHelper.listSimpleUsers(userList);
  }

  private static void listAllVersionedUsers(VersionedUserDao userDao) {
    List<VersionedUser> userList = userDao.findAll();
    log.info("findAll: " + userList.size());
    LogHelper.listVersionedUsers(userList);
  }

  private static void listAllVersionedUsersVersions(VersionedUserDao userDao) {
    List<VersionedUser> userList = userDao.findAllVersions();
    log.info("findAllVersions: " + userList.size());
    LogHelper.listVersionedUsers(userList);
  }

  public void listSimple() {
    log.info("--- listSimple ---");
    listAllSimpleUsers(simpleUserDao);
  }

  public void listVersioned() {
    log.info("--- listVersioned ---");
    listAllVersionedUsers(versionedUserDao);
    listAllVersionedUsersVersions(versionedUserDao);
  }
}
