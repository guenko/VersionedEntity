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
package eu.koch.versent.web;

import javax.ejb.EJB;
import javax.servlet.*;
import javax.servlet.http.*;
import eu.koch.versent.appl.intf.ApplicationService;
import eu.koch.versent.ejb.intf.SimpleDepartmentService;
import eu.koch.versent.ejb.intf.SimpleUserService;
import eu.koch.versent.ejb.intf.VersionedDepartmentService;
import eu.koch.versent.ejb.intf.VersionedUserService;
import eu.koch.versent.web.test.SimpleTest;
import eu.koch.versent.web.test.VersionedTest;
import org.apache.log4j.Logger;

public class TestServlet extends HttpServlet {

  private static Logger log = Logger.getLogger(TestServlet.class);

  private static final long serialVersionUID = 1L;

  @EJB
  private SimpleUserService simpleUserService;

  @EJB
  private SimpleDepartmentService simpleDepartmentService;

  @EJB
  private VersionedUserService versionedUserService;

  @EJB
  private VersionedDepartmentService versionedDepartmentService;

  @EJB
  private ApplicationService applicationService;

  public TestServlet() {
    log.info("TestServlet");
  }

  public void init(ServletConfig config) throws ServletException {
    runTests();
  }

  public String getServletInfo() {
    return this.getClass().getSimpleName();
  }

  protected void runTests() {
    log.info("------------------ runTests -------------------------");
    // will use junit3 style, since junit4 runner does not output anything
    SimpleTest.setServices(simpleUserService, simpleDepartmentService);
    junit.textui.TestRunner.run(SimpleTest.class);

    VersionedTest.setServices(versionedUserService, versionedDepartmentService);
    junit.textui.TestRunner.run(VersionedTest.class);

    log.info((VersionedTest.getTestOk() && SimpleTest.getTestOk()) ? "TESTS OK" : "---- TESTS FAILED ----");
    
    applicationService.listSimple();
    applicationService.listVersioned();
  }
}
