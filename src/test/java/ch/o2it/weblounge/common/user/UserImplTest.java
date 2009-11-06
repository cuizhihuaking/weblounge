/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2009 The Weblounge Team
 *  http://weblounge.o2it.ch
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program; if not, write to the Free Software Foundation
 *  Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package ch.o2it.weblounge.common.user;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import ch.o2it.weblounge.common.impl.user.UserImpl;

import org.junit.Before;
import org.junit.Test;

/**
 * TODO: Comment UserImplTest
 */
public class UserImplTest {
  
  /** Test user */
  protected UserImpl user = null;
  
  /** Login */
  protected String login = "john";
  
  /** Realm */
  protected String realm = "testland";
  
  /** User name */
  protected String name = "John Doe";

  /**
   * @throws java.lang.Exception
   */
  @Before
  @SuppressWarnings("unused")
  public void setUp() throws Exception {
    user = new UserImpl(login, realm, name);
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.user.UserImpl#hashCode()}.
   */
  @Test
  public void testHashCode() {
    assertEquals(login.hashCode(), user.hashCode());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.user.UserImpl#getLogin()}.
   */
  @Test
  public void testGetLogin() {
    assertEquals(login, user.getLogin());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.user.UserImpl#setName(java.lang.String)}.
   */
  @Test
  public void testSetName() {
    String name = "James Joyce";
    user.setName(name);
    assertEquals(name, user.getName());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.user.UserImpl#getName()}.
   */
  @Test
  public void testGetName() {
    assertEquals(name, user.getName());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.user.UserImpl#setRealm(java.lang.String)}.
   */
  @Test
  public void testSetRealm() {
    String realm = "wonderland";
    user.setRealm(realm);
    assertEquals(realm, user.getRealm());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.user.UserImpl#getRealm()}.
   */
  @Test
  public void testGetRealm() {
    assertEquals(realm, user.getRealm());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.user.UserImpl#equals(java.lang.Object)}.
   */
  @Test
  public void testEqualsObject() {
    assertTrue(user.equals(user));
    assertTrue(user.equals(new UserImpl(login, realm, name)));
    assertTrue(user.equals(new UserImpl(login, realm)));
    assertFalse(user.equals(new UserImpl(login, "wonderland")));
    assertFalse(user.equals(new UserImpl("james", realm)));
  }

}
