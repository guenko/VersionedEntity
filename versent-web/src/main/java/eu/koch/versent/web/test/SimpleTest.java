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

import java.util.List;
import junit.framework.TestCase;
import eu.koch.versent.ejb.entity.SimpleDepartment;
import eu.koch.versent.ejb.entity.SimpleUser;
import eu.koch.versent.ejb.intf.SimpleDepartmentService;
import eu.koch.versent.ejb.intf.SimpleUserService;
import eu.koch.versent.ejb.logging.LogHelper;
import org.apache.log4j.Logger;

public class SimpleTest extends TestCase {

  private static Logger log = Logger.getLogger(SimpleTest.class);

  private static SimpleUserService userService;
  private static SimpleDepartmentService departmentService;
  private static boolean testOk = false;

  public static boolean getTestOk() {
    return testOk;
  }

  public static void setServices(SimpleUserService userService, SimpleDepartmentService departmentService) {
    SimpleTest.userService = userService;
    SimpleTest.departmentService = departmentService;
  }

  private List<SimpleUser> listAllSimpleUsers() {
    List<SimpleUser> userList = userService.findAll();
    TestHelper.sortSimple(userList);
    log.info("findAll: " + userList.size());
    LogHelper.listSimpleUsers(userList);
    return userList;
  }

  private void checkSimpleUser(SimpleUser user, String firstname, String surname, String username,
      String department) {
    assertNotNull(user);
    assertEquals(username, user.getUsername());
    assertEquals(firstname, user.getFirstname());
    assertEquals(surname, user.getSurname());
    assertEquals(department, user.getDepartment().getName());
  }

  public void simpleTest() {
    log.info("--- simpleUserTest ---");

    SimpleDepartment department1 = new SimpleDepartment("department1");
    departmentService.insert(department1);
    userService.insert(new SimpleUser("T", "Koch", "t.koch", department1));
    userService.insert(new SimpleUser("A", "Koch", "a.koch", department1));

    List<SimpleUser> listAll = listAllSimpleUsers();
    assertEquals(2, listAll.size());
    checkSimpleUser(listAll.get(0), "T", "Koch", "t.koch", "department1");
    checkSimpleUser(listAll.get(1), "A", "Koch", "a.koch", "department1");

    SimpleUser userT = userService.findByUsername("t.koch");
    checkSimpleUser(userT, "T", "Koch", "t.koch", "department1");
    userT.setFirstname("T1");
    userService.update(userT);

    SimpleUser userA = userService.findByUsername("a.koch");
    checkSimpleUser(userA, "A", "Koch", "a.koch", "department1");
    userService.remove(userA);

    listAll = listAllSimpleUsers();
    assertEquals(1, listAll.size());
    checkSimpleUser(listAll.get(0), "T1", "Koch", "t.koch", "department1");
  }

  public void testAll() {
    log.info("userService: " + userService);
    log.info("departmentService: " + departmentService);
    simpleTest();
    testOk = true;
  }
}
