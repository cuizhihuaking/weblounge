/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2003 - 2011 The Weblounge Team
 *  http://entwinemedia.com/weblounge
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

package ch.entwine.weblounge.test.harness.content;

import static ch.entwine.weblounge.common.impl.request.Http11Constants.HEADER_ETAG;
import static ch.entwine.weblounge.common.impl.request.Http11Constants.HEADER_LAST_MODIFIED;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import ch.entwine.weblounge.common.editor.EditingState;
import ch.entwine.weblounge.common.impl.language.LanguageUtils;
import ch.entwine.weblounge.common.impl.testing.IntegrationTestBase;
import ch.entwine.weblounge.common.impl.util.TestUtils;
import ch.entwine.weblounge.common.impl.util.xml.XPathHelper;
import ch.entwine.weblounge.common.language.Language;
import ch.entwine.weblounge.common.url.UrlUtils;
import ch.entwine.weblounge.test.util.TestSiteUtils;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

/**
 * Integration test to test <code>HTML</code> page output.
 */
public class PageContentTest extends IntegrationTestBase {

  /** The logger */
  private static final Logger logger = LoggerFactory.getLogger(PageContentTest.class);

  /** The paths to test */
  private static final String requestPath = "/test/pagecontent";

  /** The paths to test */
  private static final String protectedPath = "/test/protected";

  /** The expected text */
  private static final Map<Language, String> texts = new HashMap<Language, String>();

  /** Modification date parser */
  private static final SimpleDateFormat lastModifiedDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);

  /**
   * Prepare the test data.
   */
  static {
    texts.put(LanguageUtils.getLanguage(Locale.GERMAN), "Ein amüsanter Titel");
    texts.put(LanguageUtils.getLanguage(Locale.FRENCH), "Un titre joyeux");
    texts.put(LanguageUtils.getLanguage(Locale.ENGLISH), "Ein amüsanter Titel");
  }

  /**
   * Creates a new instance of the <code>HTML</code> page test.
   */
  public PageContentTest() {
    super("Page Content Test", WEBLOUNGE_CONTENT_TEST_GROUP);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.testing.kernel.IntegrationTest#execute(java.lang.String)
   */
  @Override
  public void execute(String serverUrl)
      throws Exception {
    testPage(serverUrl);
    testNonExistingPage(serverUrl);
    //testWorkPage(serverUrl);
    //testPageAsEditor(serverUrl);
    testProtectedPage(serverUrl);
    testComposerInheritance(serverUrl);
  }

  /**
   * Tests for a non-existing page and makes sure that a 404 is returned.
   * 
   * @param serverUrl
   *          the server url
   */
  private void testNonExistingPage(String serverUrl) throws Exception {
    logger.info("Preparing test of non-existing page content");

    String requestUrl = UrlUtils.concat(serverUrl, requestPath, "does-not-exist", "index.html");

    logger.info("Sending request to {}", requestUrl);
    HttpGet request = new HttpGet(requestUrl);

    // Send and the request and examine the response
    logger.debug("Sending request to {}", request.getURI());
    HttpClient httpClient = new DefaultHttpClient();
    try {
      HttpResponse response = TestUtils.request(httpClient, request, null);
      assertEquals(HttpServletResponse.SC_NOT_FOUND, response.getStatusLine().getStatusCode());
      TestUtils.parseTextResponse(response);
    } finally {
      httpClient.getConnectionManager().shutdown();
    }
  }

  /**
   * Tests the regular delivery of page content.
   * 
   * @param serverUrl
   *          the base server url
   * @throws Exception
   *           if the test fails
   */
  private void testPage(String serverUrl) throws Exception {
    logger.info("Preparing test of regular page content");

    // Prepare the request
    logger.info("Testing regular page output");

    for (Language language : texts.keySet()) {

      String requestUrl = UrlUtils.concat(serverUrl, requestPath, "index_" + language.getIdentifier() + ".html");

      logger.info("Sending request to the {} version of {}", language.getLocale().getDisplayName(), requestUrl);
      HttpGet request = new HttpGet(requestUrl);
      request.addHeader("X-Cache-Debug", "yes");
      String[][] params = new String[][] { {
        "language",
        language.getIdentifier() } };

      String eTagValue;
      Date modificationDate = null;

      // Send and the request and examine the response
      logger.debug("Sending request to {}", request.getURI());
      HttpClient httpClient = new DefaultHttpClient();
      try {
        HttpResponse response = TestUtils.request(httpClient, request, params);
        assertEquals(HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());

        // Get the document contents
        Document xml = TestUtils.parseXMLResponse(response);

        // Test template output
        String templateOutput = XPathHelper.valueOf(xml, "/html/head/title");
        assertNotNull("General template output does not work", templateOutput);
        assertEquals("Template title is not as expected", "Weblounge Test Site", templateOutput);

        // Look for included pagelet's elements
        String text = XPathHelper.valueOf(xml, "/html/body/div[@id='main']//span[@id='element']");
        assertNotNull("Content of pagelet element 'title' not found", text);
        assertEquals("Element text does not match", texts.get(language), text);
        logger.debug("Found {} pagelet content", language.getLocale().getDisplayName());

        // Look for included pagelet's properties
        String property = XPathHelper.valueOf(xml, "/html/body/div[@id='main']//span[@id='property']");
        assertNotNull("Content of pagelet property 'headline' not found", property);
        assertEquals("Element property does not match", "true", property);
        logger.debug("Found {} pagelet property", language.getLocale().getDisplayName());

        // Look for pagelet header includes
        assertEquals("Pagelet include failed", "1", XPathHelper.valueOf(xml, "count(/html/head/link[contains(@href, 'greeting.css')])"));
        logger.debug("Found pagelet stylesheet include");

        // Test for template replacement
        assertNull("Header tag templating failed", XPathHelper.valueOf(xml, "//@src[contains(., '${module.root}')]"));
        assertNull("Header tag templating failed", XPathHelper.valueOf(xml, "//@src[contains(., '${site.root}')]"));

        // Test for reflection of current environment
        String scriptUrl = XPathHelper.valueOf(xml, "/html/head/script[contains(@src, 'greeting.js')]/@src");
        assertNotNull("Script include failed", scriptUrl);
        String stylesheetUrl = XPathHelper.valueOf(xml, "/html/head/link[contains(@href, 'greeting.css')]/@href");
        assertNotNull("Stylesheet include failed", stylesheetUrl);
        switch (environment) {
          case Any:
            fail("Environment has not yet been initialized");
            break;
          case Development:
            assertTrue("Script url does not reflect environment", scriptUrl.startsWith("http://localhost:8080"));
            assertTrue("Stylesheet url does not reflect environment", stylesheetUrl.startsWith("http://localhost:8080"));
            break;
          case Production:
            assertTrue("Script url does not reflect environment", scriptUrl.startsWith("http://test:8080"));
            assertTrue("Stylesheet url does not reflect environment", stylesheetUrl.startsWith("http://test:8080"));
            break;
          case Staging:
            assertTrue("Script url does not reflect environment", scriptUrl.startsWith("http://127.0.0.1:8080"));
            assertTrue("Stylesheet url does not reflect environment", stylesheetUrl.startsWith("http://127.0.0.1:8080"));
            break;
          default:
            fail("Unknown environment " + environment + " encountered");
            break;
        }

        // See if the cache is online
        if (response.getHeaders("X-Cache-Key").length == 0) {
          logger.warn("Cache is turned off, ETags are not tested");
          return;
        }

        // Test ETag header
        Header eTagHeader = response.getFirstHeader(HEADER_ETAG);
        assertNotNull(eTagHeader);
        assertNotNull(eTagHeader.getValue());
        eTagValue = eTagHeader.getValue();

        // Test Last-Modified header
        Header modifiedHeader = response.getFirstHeader(HEADER_LAST_MODIFIED);
        assertNotNull(modifiedHeader);
        modificationDate = lastModifiedDateFormat.parse(modifiedHeader.getValue());

        TestSiteUtils.testETagHeader(request, eTagValue, logger, params);
        TestSiteUtils.testModifiedHeader(request, modificationDate, logger, params);

      } finally {
        httpClient.getConnectionManager().shutdown();
      }

    }
  }

  /**
   * Tests that the work version of a page is returned.
   * 
   * @param serverUrl
   *          the base server url
   * @throws Exception
   *           if the test fails
   */
  private void testWorkPage(String serverUrl) throws Exception {
    logger.info("Preparing test of regular page content");

    // Prepare the request
    logger.info("Testing regular page output as an editor");

    String requestUrl = UrlUtils.concat(serverUrl, requestPath, "work.html") + "&edit=true";

    logger.info("Sending request to the work version of {}", requestUrl);
    HttpGet request = new HttpGet(requestUrl);
    request.addHeader("Cookie", "weblounge.editor=true");

    // Send and the request and examine the response
    logger.debug("Sending request to {}", request.getURI());
    HttpClient httpClient = new DefaultHttpClient();
    try {
      HttpResponse response = TestUtils.request(httpClient, request, null);
      assertEquals(HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());

      // Get the document contents
      Document xml = TestUtils.parseXMLResponse(response);

      // Look for included pagelet's properties
      String property = XPathHelper.valueOf(xml, "/html/body/div[@id='main']//span[@id='property']");
      assertNotNull("Content of pagelet property 'headline' not found", property);
      assertEquals("Element property does not match", "false", property);

    } finally {
      httpClient.getConnectionManager().shutdown();
    }

  }

  /**
   * Tests that the work version of a page is returned if it exists when the
   * corresponding cookie is passed in.
   * 
   * @param serverUrl
   *          the base server url
   * @throws Exception
   *           if the test fails
   */
  private void testPageAsEditor(String serverUrl) throws Exception {
    logger.info("Preparing test of regular page content");

    // Prepare the request
    logger.info("Testing regular page output as an editor");

    String requestUrl = UrlUtils.concat(serverUrl, requestPath);

    logger.info("Sending request to the work version of {}", requestUrl);
    HttpGet request = new HttpGet(requestUrl);

    // Send and the request and examine the response
    logger.debug("Sending request to {}", request.getURI());
    HttpClient httpClient = new DefaultHttpClient();
    try {

      request.setHeader("Cookie", EditingState.STATE_COOKIE + "=true");

      HttpResponse response = TestUtils.request(httpClient, request, null);
      assertEquals(HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());

      // Get the document contents
      Document xml = TestUtils.parseXMLResponse(response);

      // Look for included pagelet's properties
      String property = XPathHelper.valueOf(xml, "/html/body/div[@id='main']//span[@id='property']");
      assertNotNull("Content of pagelet property 'headline' not found", property);
      assertEquals("Element property does not match", "false", property);

    } finally {
      httpClient.getConnectionManager().shutdown();
    }

  }

  /**
   * Tests that the work version of a page is returned.
   * 
   * @param serverUrl
   *          the base server url
   * @throws Exception
   *           if the test fails
   */
  private void testProtectedPage(String serverUrl) throws Exception {
    logger.info("Preparing test of protected page content");

    // Prepare the request
    logger.info("Testing access of protected page");

    String requestUrl = UrlUtils.concat(serverUrl, protectedPath);

    logger.info("Sending request to the protected page at {}", requestUrl);
    HttpGet request = new HttpGet(requestUrl);

    // Send and the request and examine the response
    logger.debug("Sending request to {}", request.getURI());
    HttpClient httpClient = new DefaultHttpClient();
    try {
      HttpResponse response = TestUtils.request(httpClient, request, null);
      assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatusLine().getStatusCode());
    } finally {
      httpClient.getConnectionManager().shutdown();
    }

  }

  /**
   * Tests the delivery of inherited page content.
   * 
   * @param serverUrl
   *          the base server url
   * @throws Exception
   *           if the test fails
   */
  private void testComposerInheritance(String serverUrl) throws Exception {
    logger.info("Preparing test of inherited page content");

    // Prepare the request
    logger.info("Testing inherited page output");

    String requestUrl = UrlUtils.concat(serverUrl, "/test/ghost/inheriting");

    logger.info("Sending request to {}", requestUrl);
    HttpGet request = new HttpGet(requestUrl);

    String eTagValue;
    Date modificationDate = null;

    // Send and the request and examine the response
    logger.debug("Sending request to {}", request.getURI());
    HttpClient httpClient = new DefaultHttpClient();
    try {
      HttpResponse response = TestUtils.request(httpClient, request, null);
      assertEquals(HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());

      // Get the document contents
      Document xml = TestUtils.parseXMLResponse(response);

      // Test template output
      String templateOutput = XPathHelper.valueOf(xml, "/html/head/title");
      assertNotNull("General template output does not work", templateOutput);
      assertEquals("Template title is not as expected", "Weblounge Test Site Ghost Template", templateOutput);

      // Look for inherited pagelets
      String text = XPathHelper.valueOf(xml, "/html/body/div[@id='main']//div[@class='greeting']");
      assertNotNull("Content of pagelet element 'include' not found", text);
      logger.debug("Found inherited pagelet content");

      // Look for inherited pagelet header includes
      assertEquals("Pagelet include failed", "1", XPathHelper.valueOf(xml, "count(/html/head/link[contains(@href, 'greeting.css')])"));
      logger.debug("Found pagelet stylesheet include");

      // Test for template replacement
      assertNull("Header tag templating failed", XPathHelper.valueOf(xml, "//@src[contains(., '${module.root}')]"));
      assertNull("Header tag templating failed", XPathHelper.valueOf(xml, "//@src[contains(., '${site.root}')]"));

      // See if the cache is online
      if (response.getHeaders("X-Cache-Key").length == 0) {
        logger.warn("Cache is turned off, ETags are not tested");
        return;
      }

      // Test ETag header
      Header eTagHeader = response.getFirstHeader("ETag");
      assertNotNull(eTagHeader);
      assertNotNull(eTagHeader.getValue());
      eTagValue = eTagHeader.getValue();

      // Test Last-Modified header
      Header modifiedHeader = response.getFirstHeader("Last-Modified");
      assertNotNull(modifiedHeader);
      modificationDate = lastModifiedDateFormat.parse(modifiedHeader.getValue());

      TestSiteUtils.testETagHeader(request, eTagValue, logger, null);
      TestSiteUtils.testModifiedHeader(request, modificationDate, logger, null);
      
    } finally {
      httpClient.getConnectionManager().shutdown();
    }

  }

}
