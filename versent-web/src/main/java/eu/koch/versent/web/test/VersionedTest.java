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

import java.util.Date;
import java.util.List;
import junit.framework.TestCase;
import eu.koch.versent.base.TimeHelper;
import eu.koch.versent.ejb.entity.VersionedDepartment;
import eu.koch.versent.ejb.entity.VersionedUser;
import eu.koch.versent.ejb.intf.VersionedDepartmentService;
import eu.koch.versent.ejb.intf.VersionedUserService;
import eu.koch.versent.ejb.logging.LogHelper;
import org.apache.log4j.Logger;

public class VersionedTest extends TestCase {

  private static Logger log = Logger.getLogger(VersionedTest.class);

  private static VersionedUserService userService;
  private static VersionedDepartmentService departmentService;
  private static boolean testOk = false;

  public static boolean getTestOk() {
    return testOk;
  }

  public static void setServices(VersionedUserService userService, VersionedDepartmentService departmentService) {
    VersionedTest.userService = userService;
    VersionedTest.departmentService = departmentService;
  }

  private List<VersionedUser> listAllVersionedUsers() {
    List<VersionedUser> userList = userService.findAll();
    TestHelper.sortVersioned(userList);
    log.info("findAll: " + userList.size());
    LogHelper.listVersionedUsers(userList);
    return userList;
  }

  private List<VersionedUser> listAllVersionedUsersVersions() {
    List<VersionedUser> userList = userService.findAllVersions();
    TestHelper.sortVersioned(userList);
    log.info("findAllVersions: " + userList.size());
    LogHelper.listVersionedUsers(userList);
    return userList;
  }

  private void checkVersionedUser(VersionedUser user, String firstname, String surname, String username,
      Date validFrom, Date validTo) {
    assertNotNull(user);
    assertEquals(username, user.getUsername());
    assertEquals(firstname, user.getFirstname());
    assertEquals(surname, user.getSurname());
    assertTrue(TimeHelper.databaseEqual(validFrom, user.getValidFrom()));
    if (validTo.equals(END_OF_TIME)) {
      assertTrue(END_OF_TIME.equals(user.getValidTo()));
    } else {
      assertTrue(TimeHelper.databaseEqual(validTo, user.getValidTo()));
    }
  }

  private void checkVersionedUser(VersionedUser user, Long chainId, String firstname, String surname,
      String username, Date validFrom, Date validTo) {
    assertNotNull(user);
    assertEquals(chainId, user.getChainId());
    checkVersionedUser(user, firstname, surname, username, validFrom, validTo);
  }

  private static Date END_OF_TIME = TimeHelper.END_OF_TIME;
  private static Date insertTimeT;
  private static Date insertTimeA;
  private static Date insertTimeC;
  private static Date updateTimeT1;
  private static Date updateTimeT2;
  private static Date removeTimeA;

  private static Long chainIdT;
  private static Long chainIdA;
  private static Long chainIdC;

  private VersionedDepartment dep1;

  private void runInsert() {
    log.info("--- runInsert ---");

    Date now = TestHelper.getNow();
    dep1 = new VersionedDepartment("department1");
    departmentService.insert(dep1, now);
    log.info("- insert t -");
    insertTimeT = userService.insert(new VersionedUser("T0", "Koch", "t.koch", dep1), now);
    log.info("insertTimeT: " + TimeHelper.getUtcDateTimeStrMilli(insertTimeT));
    log.info("- insert c -");
    insertTimeC = userService.insert(new VersionedUser("C", "Koch", "c.koch", dep1), now);
    log.info("insertTimeC: " + TimeHelper.getUtcDateTimeStrMilli(insertTimeC));

    List<VersionedUser> listAll = listAllVersionedUsers();
    assertEquals(2, listAll.size());
    checkVersionedUser(listAll.get(0), "T0", "Koch", "t.koch", insertTimeT, END_OF_TIME);
    checkVersionedUser(listAll.get(1), "C", "Koch", "c.koch", insertTimeC, END_OF_TIME);
    chainIdT = listAll.get(0).getChainId();
    chainIdC = listAll.get(1).getChainId();
    assertNotSame(chainIdT, chainIdC);

    listAll = listAllVersionedUsersVersions();
    assertEquals(2, listAll.size());
    checkVersionedUser(listAll.get(0), chainIdT, "T0", "Koch", "t.koch", insertTimeT, END_OF_TIME);
    checkVersionedUser(listAll.get(1), chainIdC, "C", "Koch", "c.koch", insertTimeC, END_OF_TIME);

    // valid period starts at insertion time
    VersionedUser userT0 = userService.findByUsername("t.koch", insertTimeT);
    assertNotNull(userT0);
    checkVersionedUser(userT0, "T0", "Koch", "t.koch", insertTimeT, END_OF_TIME);
    // record is not valid before insertion time
    userT0 = userService.findByUsername("t.koch", TestHelper.getTimeTickBefore(insertTimeT));
    assertNull(userT0);

    TestHelper.waitForNextTimeTick(insertTimeT);
  }

  private void runUpdateCurrent1() {
    log.info("--- runUpdateCurrent1 ---");

    log.info("- update t0 -");
    VersionedUser userT0 = userService.findByUsername("t.koch", null);
    userT0.setFirstname("T1");
    userT0.setUsername("t.koch");
    updateTimeT1 = userService.update(userT0);

    List<VersionedUser> listAll = listAllVersionedUsers();
    assertEquals(2, listAll.size());
    checkVersionedUser(listAll.get(0), chainIdT, "T1", "Koch", "t.koch", updateTimeT1, END_OF_TIME);
    checkVersionedUser(listAll.get(1), chainIdC, "C", "Koch", "c.koch", insertTimeC, END_OF_TIME);

    listAll = listAllVersionedUsersVersions();
    assertEquals(3, listAll.size());
    checkVersionedUser(listAll.get(0), chainIdT, "T0", "Koch", "t.koch", insertTimeT, updateTimeT1);
    checkVersionedUser(listAll.get(1), chainIdT, "T1", "Koch", "t.koch", updateTimeT1, END_OF_TIME);
    checkVersionedUser(listAll.get(2), chainIdC, "C", "Koch", "c.koch", insertTimeC, END_OF_TIME);

    TestHelper.waitForNextTimeTick(updateTimeT1);
  }

  private void runUpdateCurrent2() {
    log.info("--- runUpdateCurrent2 ---");

    log.info("- update t1 -");
    Date now = TestHelper.getNow();
    VersionedUser userT1 = userService.findByUsername("t.koch", now);
    userT1.setFirstname("T2");
    userT1.setUsername("t.koch");
    updateTimeT2 = userService.update(userT1, now);

    List<VersionedUser> listAll = listAllVersionedUsers();
    assertEquals(2, listAll.size());
    checkVersionedUser(listAll.get(0), chainIdT, "T2", "Koch", "t.koch", updateTimeT2, END_OF_TIME);
    checkVersionedUser(listAll.get(1), chainIdC, "C", "Koch", "c.koch", insertTimeC, END_OF_TIME);

    listAll = listAllVersionedUsersVersions();
    assertEquals(4, listAll.size());
    checkVersionedUser(listAll.get(0), chainIdT, "T0", "Koch", "t.koch", insertTimeT, updateTimeT1);
    checkVersionedUser(listAll.get(1), chainIdT, "T1", "Koch", "t.koch", updateTimeT1, updateTimeT2);
    checkVersionedUser(listAll.get(2), chainIdT, "T2", "Koch", "t.koch", updateTimeT2, END_OF_TIME);
    checkVersionedUser(listAll.get(3), chainIdC, "C", "Koch", "c.koch", insertTimeC, END_OF_TIME);

    userT1 = userService.findByUsername("t.koch", updateTimeT1);
    checkVersionedUser(userT1, chainIdT, "T1", "Koch", "t.koch", updateTimeT1, updateTimeT2);
    VersionedUser userT2 = userService.findByUsername("t.koch", updateTimeT2);
    checkVersionedUser(userT2, chainIdT, "T2", "Koch", "t.koch", updateTimeT2, END_OF_TIME);
  }

  private void runNextPreviousVersions() {
    log.info("--- runVersionedNextPrevious ---");

    log.info("- find t2 -");
    VersionedUser userT2 = userService.findByUsername("t.koch", null);
    LogHelper.listVersionedUser(userT2);
    checkVersionedUser(userT2, chainIdT, "T2", "Koch", "t.koch", updateTimeT2, END_OF_TIME);

    log.info("- getVersion -");
    VersionedUser userCurrent = userService.getEffectiveVersion(userT2);
    checkVersionedUser(userCurrent, chainIdT, "T2", "Koch", "t.koch", updateTimeT2, END_OF_TIME);

    log.info("- getPreviousVersion -");
    VersionedUser userT1 = userService.getPreviousVersion(userT2);
    LogHelper.listVersionedUser(userT1);
    checkVersionedUser(userT1, chainIdT, "T1", "Koch", "t.koch", updateTimeT1, updateTimeT2);

    log.info("- getPreviousVersion -");
    VersionedUser userT0 = userService.getPreviousVersion(userT1);
    LogHelper.listVersionedUser(userT0);
    checkVersionedUser(userT0, chainIdT, "T0", "Koch", "t.koch", insertTimeT, updateTimeT1);

    log.info("- getVersion -");
    userCurrent = userService.getEffectiveVersion(userT2);
    checkVersionedUser(userCurrent, chainIdT, "T2", "Koch", "t.koch", updateTimeT2, END_OF_TIME);

    log.info("- getPreviousVersion -");
    assertEquals(null, userService.getPreviousVersion(userT0));

    log.info("- getNextVersion -");
    userT1 = userService.getNextVersion(userT0);
    LogHelper.listVersionedUser(userT1);
    checkVersionedUser(userT1, chainIdT, "T1", "Koch", "t.koch", updateTimeT1, updateTimeT2);

    log.info("- getNextVersion -");
    userT2 = userService.getNextVersion(userT1);
    LogHelper.listVersionedUser(userT2);
    checkVersionedUser(userT2, chainIdT, "T2", "Koch", "t.koch", updateTimeT2, END_OF_TIME);

    log.info("- getNextVersion -");
    assertEquals(null, userService.getNextVersion(userT2));

    List<VersionedUser> listVersions = userService.getVersions(userT0);
    LogHelper.listVersionedUsers(listVersions);
    assertEquals(3, listVersions.size());
    checkVersionedUser(listVersions.get(0), chainIdT, "T0", "Koch", "t.koch", insertTimeT, updateTimeT1);
    checkVersionedUser(listVersions.get(1), chainIdT, "T1", "Koch", "t.koch", updateTimeT1, updateTimeT2);
    checkVersionedUser(listVersions.get(2), chainIdT, "T2", "Koch", "t.koch", updateTimeT2, END_OF_TIME);
  }

  private void runUserToDepartment() {
    log.info("--- runUserToDepartment ---");

    Date now = TestHelper.getNow();
    VersionedUser user = userService.findByUsername("t.koch", now);
    VersionedDepartment department = userService.getDepartment(user, now);
    log.info(department.getId() + " " + department.getName());
  }

  private void runCreateFuture() {
    log.info("--- runCreateFuture ---");

    log.info("- get c -");
    VersionedUser userC = userService.findByUsername("c.koch", null);

    log.info("- update c -"); // update record with future valid-from time:
                              // creates future record
    userC.setFirstname("C1");
    Date now = TimeHelper.getNow();
    Date effectiveTime = new Date(now.getTime() + 3000);
    Date updateTimeC1 = userService.update(userC, effectiveTime);
    log.info("updateTimeC1: " + TimeHelper.getUtcDateTimeStrMilli(updateTimeC1));
    assertEquals(effectiveTime, updateTimeC1);
    assertEquals(5, userService.findAllVersions().size());

    log.info("- get c -");
    userC = userService.findByUsername("c.koch", now);
    LogHelper.listVersionedUser(userC);
    checkVersionedUser(userC, chainIdC, "C", "Koch", "c.koch", insertTimeC, updateTimeC1);
    assertEquals(chainIdC, userC.getChainId());

    log.info("- get c1 -");
    VersionedUser userC1 = userService.findByUsername("c.koch", updateTimeC1);
    LogHelper.listVersionedUser(userC1);
    checkVersionedUser(userC1, chainIdC, "C1", "Koch", "c.koch", updateTimeC1, END_OF_TIME);
    assertEquals(chainIdC, userC1.getChainId());

    List<VersionedUser> listVersions = userService.getVersions(userC1);
    LogHelper.listVersionedUsers(listVersions);
    assertEquals(2, listVersions.size());
    checkVersionedUser(listVersions.get(0), chainIdC, "C", "Koch", "c.koch", insertTimeC, updateTimeC1);
    checkVersionedUser(listVersions.get(1), chainIdC, "C1", "Koch", "c.koch", updateTimeC1, END_OF_TIME);
  }

  private void runUpdateFuture() {
    log.info("--- runUpdateFuture ---");

    log.info("- get c1 -");
    VersionedUser userC1 = userService.findByUsername("c.koch", TimeHelper.END_OF_TIME);
    assertNotNull(userC1);

    log.info("- update c1 -"); // update future record with change of effective
                               // time (change of valid-from
                               // time)
    userC1.setFirstname("C2");
    Date effectiveTime = new Date(userC1.getValidFrom().getTime() + 2000);
    Date updateTimeC2 = userService.update(userC1, effectiveTime);
    log.info("updateTimeC2: " + TimeHelper.getUtcDateTimeStrMilli(updateTimeC2));
    assertEquals(effectiveTime, updateTimeC2);
    assertEquals(5, userService.findAllVersions().size());

    log.info("- get c -");
    VersionedUser userC = userService.findByUsername("c.koch", null);
    LogHelper.listVersionedUser(userC);
    checkVersionedUser(userC, chainIdC, "C", "Koch", "c.koch", insertTimeC, updateTimeC2);
    assertEquals(chainIdC, userC.getChainId());

    log.info("- get c1 -");
    VersionedUser userC2 = userService.findByUsername("c.koch", updateTimeC2);
    LogHelper.listVersionedUser(userC2);
    checkVersionedUser(userC2, chainIdC, "C2", "Koch", "c.koch", updateTimeC2, END_OF_TIME);
    assertEquals(chainIdC, userC2.getChainId());

    log.info("- update c2 -"); // update future record without time change
    userC2.setFirstname("C3");
    Date updateTimeC3 = userService.update(userC2, effectiveTime);
    assertEquals(updateTimeC3, updateTimeC2);
    assertEquals(5, userService.findAllVersions().size());

    log.info("- get c -");
    userC = userService.findByUsername("c.koch", null);
    LogHelper.listVersionedUser(userC);
    checkVersionedUser(userC, chainIdC, "C", "Koch", "c.koch", insertTimeC, updateTimeC2);
    assertEquals(chainIdC, userC.getChainId());

    log.info("- get c3 -");
    VersionedUser userC3 = userService.findByUsername("c.koch", updateTimeC2);
    LogHelper.listVersionedUser(userC2);
    checkVersionedUser(userC3, chainIdC, "C3", "Koch", "c.koch", updateTimeC2, END_OF_TIME);
    assertEquals(chainIdC, userC3.getChainId());

    List<VersionedUser> listVersions = userService.getVersions(userC);
    LogHelper.listVersionedUsers(listVersions);
    assertEquals(2, listVersions.size());
    checkVersionedUser(listVersions.get(0), chainIdC, "C", "Koch", "c.koch", insertTimeC, updateTimeC2);
    checkVersionedUser(listVersions.get(1), chainIdC, "C3", "Koch", "c.koch", updateTimeC2, END_OF_TIME);
  }

  private void runRemoveFuture() {
    log.info("--- runRemoveFuture ---");
    log.info("- get c3 -");
    VersionedUser userC3 = userService.findByUsername("c.koch", TimeHelper.END_OF_TIME);
    assertNotNull(userC3);
    userService.remove(userC3);

    List<VersionedUser> listAll = listAllVersionedUsersVersions();
    assertEquals(4, listAll.size());
    checkVersionedUser(listAll.get(0), chainIdT, "T0", "Koch", "t.koch", insertTimeT, updateTimeT1);
    checkVersionedUser(listAll.get(1), chainIdT, "T1", "Koch", "t.koch", updateTimeT1, updateTimeT2);
    checkVersionedUser(listAll.get(2), chainIdT, "T2", "Koch", "t.koch", updateTimeT2, END_OF_TIME);
    checkVersionedUser(listAll.get(3), chainIdC, "C", "Koch", "c.koch", insertTimeC, END_OF_TIME);

    log.info("- get c -");
    VersionedUser userC = userService.findByUsername("c.koch", null);
    checkVersionedUser(userC, chainIdC, "C", "Koch", "c.koch", insertTimeC, END_OF_TIME);
    assertNull(userService.getPreviousVersion(userC));
    assertNull(userService.getNextVersion(userC));
  }

  private void runRemoveInFutureAndNow() {
    log.info("--- runRemoveCurrent ---");

    log.info("- insert a -");
    insertTimeA = userService.insert(new VersionedUser("A", "Koch", "a.koch", dep1));
    log.info("insertTimeA: " + TimeHelper.getUtcDateTimeStrMilli(insertTimeA));

    List<VersionedUser> listAll = listAllVersionedUsers();
    assertEquals(3, listAll.size());
    checkVersionedUser(listAll.get(0), chainIdT, "T2", "Koch", "t.koch", updateTimeT2, END_OF_TIME);
    checkVersionedUser(listAll.get(1), chainIdC, "C", "Koch", "c.koch", insertTimeC, END_OF_TIME);
    checkVersionedUser(listAll.get(2), "A", "Koch", "a.koch", insertTimeA, END_OF_TIME);
    chainIdA = listAll.get(2).getChainId();
    assertNotSame(chainIdA, chainIdT);
    assertNotSame(chainIdA, chainIdC);

    listAll = listAllVersionedUsersVersions();
    assertEquals(5, listAll.size());
    checkVersionedUser(listAll.get(0), chainIdT, "T0", "Koch", "t.koch", insertTimeT, updateTimeT1);
    checkVersionedUser(listAll.get(1), chainIdT, "T1", "Koch", "t.koch", updateTimeT1, updateTimeT2);
    checkVersionedUser(listAll.get(2), chainIdT, "T2", "Koch", "t.koch", updateTimeT2, END_OF_TIME);
    checkVersionedUser(listAll.get(3), chainIdC, "C", "Koch", "c.koch", insertTimeC, END_OF_TIME);
    checkVersionedUser(listAll.get(4), chainIdA, "A", "Koch", "a.koch", insertTimeA, END_OF_TIME);

    log.info("- remove a - at effective time in future");
    Date nowReference = TimeHelper.getNow();
    VersionedUser userA = userService.findByUsername("a.koch", nowReference);
    assertNotNull(userA);

    Date removalEffectiveTime = new Date(nowReference.getTime() + 3000);
    removeTimeA = userService.remove(userA, removalEffectiveTime);

    listAll = listAllVersionedUsers();
    assertEquals(3, listAll.size());
    checkVersionedUser(listAll.get(0), chainIdT, "T2", "Koch", "t.koch", updateTimeT2, END_OF_TIME);
    checkVersionedUser(listAll.get(1), chainIdC, "C", "Koch", "c.koch", insertTimeC, END_OF_TIME);
    checkVersionedUser(listAll.get(2), chainIdA, "A", "Koch", "a.koch", insertTimeA, removeTimeA);

    listAll = listAllVersionedUsersVersions();
    assertEquals(5, listAll.size());
    checkVersionedUser(listAll.get(0), chainIdT, "T0", "Koch", "t.koch", insertTimeT, updateTimeT1);
    checkVersionedUser(listAll.get(1), chainIdT, "T1", "Koch", "t.koch", updateTimeT1, updateTimeT2);
    checkVersionedUser(listAll.get(2), chainIdT, "T2", "Koch", "t.koch", updateTimeT2, END_OF_TIME);
    checkVersionedUser(listAll.get(3), chainIdC, "C", "Koch", "c.koch", insertTimeC, END_OF_TIME);
    checkVersionedUser(listAll.get(4), chainIdA, "A", "Koch", "a.koch", insertTimeA, removeTimeA);

    // retrieval exactly at removal time: records is treated as removed
    userA = userService.findByUsername("a.koch", removeTimeA);
    assertNull(userA);
    // retrieval one tick before removal time: records is treated as existing
    userA = userService.findByUsername("a.koch", TestHelper.getTimeTickBefore(removeTimeA));
    assertNotNull(userA);

    log.info("- undo remove a - set effective time to end-of-time");
    removeTimeA = userService.remove(userA, TimeHelper.END_OF_TIME);
    // user A is now valid for all the time
    userA = userService.findByUsername("a.koch", TimeHelper.END_OF_TIME);
    LogHelper.listVersionedUser(userA);
    checkVersionedUser(userA, chainIdA, "A", "Koch", "a.koch", insertTimeA, END_OF_TIME);
    assertEquals(3, listAllVersionedUsers().size());
    assertEquals(5, listAllVersionedUsersVersions().size());

    log.info("- remove a - effective at now ");
    TestHelper.waitForNextTimeTick(insertTimeA);
    removeTimeA = userService.remove(userA);

    listAll = listAllVersionedUsers();
    assertEquals(2, listAll.size());
    checkVersionedUser(listAll.get(0), chainIdT, "T2", "Koch", "t.koch", updateTimeT2, END_OF_TIME);
    checkVersionedUser(listAll.get(1), chainIdC, "C", "Koch", "c.koch", insertTimeC, END_OF_TIME);

    listAll = listAllVersionedUsersVersions();
    assertEquals(5, listAll.size());
    checkVersionedUser(listAll.get(0), chainIdT, "T0", "Koch", "t.koch", insertTimeT, updateTimeT1);
    checkVersionedUser(listAll.get(1), chainIdT, "T1", "Koch", "t.koch", updateTimeT1, updateTimeT2);
    checkVersionedUser(listAll.get(2), chainIdT, "T2", "Koch", "t.koch", updateTimeT2, END_OF_TIME);
    checkVersionedUser(listAll.get(3), chainIdC, "C", "Koch", "c.koch", insertTimeC, END_OF_TIME);
    checkVersionedUser(listAll.get(4), chainIdA, "A", "Koch", "a.koch", insertTimeA, removeTimeA);

    log.info("- get historical version a -");
    // retrieval exactly at removal time: records is treated as removed
    userA = userService.findByUsername("a.koch", removeTimeA);
    assertNull(userA);
    // retrieval one tick before removal time: records is treated as existing
    userA = userService.findByUsername("a.koch", TestHelper.getTimeTickBefore(removeTimeA));
    assertNotNull(userA);
    checkVersionedUser(userA, chainIdA, "A", "Koch", "a.koch", insertTimeA, removeTimeA);

    log.info("- list versions a -");
    List<VersionedUser> listVersions = userService.getVersions(userA);
    LogHelper.listVersionedUsers(listVersions);
    assertEquals(1, listVersions.size());
    checkVersionedUser(listVersions.get(0), chainIdA, "A", "Koch", "a.koch", insertTimeA, removeTimeA);

    log.info("- get current version a -");
    userA = userService.getEffectiveVersion(userA);
    LogHelper.listVersionedUser(userA);
    assertNull(userA);
  }

  public void testAll() {
    log.info("userService: " + userService);
    log.info("departmentService: " + departmentService);

    runInsert();
    runUpdateCurrent1();
    runUpdateCurrent2();
    runNextPreviousVersions();
    runUserToDepartment();
    runCreateFuture();
    runUpdateFuture();
    runRemoveFuture();
    runRemoveInFutureAndNow();
    testOk = true;
  }
}
