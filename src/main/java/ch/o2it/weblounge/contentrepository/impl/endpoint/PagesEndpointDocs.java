/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2010 The Weblounge Team
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

package ch.o2it.weblounge.contentrepository.impl.endpoint;

import static ch.o2it.weblounge.common.impl.util.doc.Status.BAD_REQUEST;
import static ch.o2it.weblounge.common.impl.util.doc.Status.CONFLICT;
import static ch.o2it.weblounge.common.impl.util.doc.Status.METHOD_NOT_ALLOWED;
import static ch.o2it.weblounge.common.impl.util.doc.Status.NOT_FOUND;
import static ch.o2it.weblounge.common.impl.util.doc.Status.OK;
import static ch.o2it.weblounge.common.impl.util.doc.Status.SERVICE_UNAVAILABLE;
import static ch.o2it.weblounge.common.impl.util.doc.Status.PRECONDITION_FAILED;

import ch.o2it.weblounge.common.impl.util.doc.Endpoint;
import ch.o2it.weblounge.common.impl.util.doc.EndpointDocumentation;
import ch.o2it.weblounge.common.impl.util.doc.EndpointDocumentationGenerator;
import ch.o2it.weblounge.common.impl.util.doc.Format;
import ch.o2it.weblounge.common.impl.util.doc.Parameter;
import ch.o2it.weblounge.common.impl.util.doc.TestForm;
import ch.o2it.weblounge.common.impl.util.doc.Endpoint.Method;

/**
 * Page endpoint documentation generator.
 */
public final class PagesEndpointDocs {

  /**
   * Creates the documentation.
   * 
   * @param endpointUrl
   *          the endpoint address
   * @return the endpoint documentation
   */
  public static String createDocumentation(String endpointUrl) {
    EndpointDocumentation docs = new EndpointDocumentation(endpointUrl, "pages");
    docs.setTitle("Weblounge Pages");

    // GET /{page}
    Endpoint pageEndpoint = new Endpoint("/{page}", Method.GET, "getpage");
    pageEndpoint.setDescription("Returns the page with the given id");
    pageEndpoint.addFormat(Format.xml());
    pageEndpoint.addStatus(OK("the page was found and is returned as part of the response"));
    pageEndpoint.addStatus(NOT_FOUND("the page was not found or could not be loaded"));
    pageEndpoint.addStatus(BAD_REQUEST("an invalid page identifier was received"));
    pageEndpoint.addStatus(SERVICE_UNAVAILABLE("the site or its content repository is temporarily offline"));
    pageEndpoint.addPathParameter(new Parameter("page", Parameter.Type.String, "The page identifier"));
    pageEndpoint.setTestForm(new TestForm());
    docs.addEndpoint(Endpoint.Type.READ, pageEndpoint);

    // POST /{page}
    Endpoint createPageEndpoint = new Endpoint("/", Method.POST, "createpage");
    createPageEndpoint.setDescription("Creates a new page, either at the given path or at a random location and returns the REST url of the created resource.");
    createPageEndpoint.addFormat(Format.xml());
    createPageEndpoint.addStatus(OK("the page was created and the response body contains it's resource url"));
    createPageEndpoint.addStatus(BAD_REQUEST("the path was not specified"));
    createPageEndpoint.addStatus(BAD_REQUEST("the page content is malformed"));
    createPageEndpoint.addStatus(CONFLICT("a page already exists at the specified path"));
    createPageEndpoint.addStatus(METHOD_NOT_ALLOWED("the site or its content repository is read-only"));
    createPageEndpoint.addStatus(SERVICE_UNAVAILABLE("the site or its content repository is temporarily offline"));
    createPageEndpoint.addOptionalParameter(new Parameter("path", Parameter.Type.String, "The target path"));
    createPageEndpoint.addOptionalParameter(new Parameter("page", Parameter.Type.String, "The page data"));
    createPageEndpoint.setTestForm(new TestForm());
    docs.addEndpoint(Endpoint.Type.WRITE, createPageEndpoint);

    // PUT /{page}
    Endpoint updatePageEndpoint = new Endpoint("/{page}", Method.PUT, "updatepage");
    updatePageEndpoint.setDescription("Updates the specified page. If the client supplies an If-Match header, the update is processed only if the header value matches the page's ETag");
    updatePageEndpoint.addFormat(Format.xml());
    updatePageEndpoint.addStatus(OK("the page was updated"));
    updatePageEndpoint.addStatus(BAD_REQUEST("the page content was not specified"));
    createPageEndpoint.addStatus(BAD_REQUEST("the page content is malformed"));
    updatePageEndpoint.addStatus(NOT_FOUND("the site or the page to update were not found"));
    updatePageEndpoint.addStatus(PRECONDITION_FAILED("the page's etag does not match the value specified in the If-Match header"));
    updatePageEndpoint.addStatus(METHOD_NOT_ALLOWED("the site or its content repository is read-only"));
    updatePageEndpoint.addStatus(SERVICE_UNAVAILABLE("the site or its content repository is temporarily offline"));
    updatePageEndpoint.addPathParameter(new Parameter("page", Parameter.Type.String, "The page identifier"));
    updatePageEndpoint.addRequiredParameter(new Parameter("content", Parameter.Type.Text, "The page content"));
    updatePageEndpoint.setTestForm(new TestForm());
    docs.addEndpoint(Endpoint.Type.WRITE, updatePageEndpoint);

    // DELETE /{page}
    Endpoint deletePageEndpoint = new Endpoint("/{page}", Method.DELETE, "deletepage");
    deletePageEndpoint.setDescription("Deletes the specified page.");
    deletePageEndpoint.addFormat(Format.xml());
    deletePageEndpoint.addStatus(OK("the page was deleted"));
    deletePageEndpoint.addStatus(BAD_REQUEST("the page was not specified"));
    deletePageEndpoint.addStatus(NOT_FOUND("the page was not found"));
    deletePageEndpoint.addStatus(METHOD_NOT_ALLOWED("the site or its content repository is read-only"));
    deletePageEndpoint.addStatus(SERVICE_UNAVAILABLE("the site or its content repository is temporarily offline"));
    deletePageEndpoint.addPathParameter(new Parameter("page", Parameter.Type.String, "The page identifier"));
    deletePageEndpoint.setTestForm(new TestForm());
    docs.addEndpoint(Endpoint.Type.WRITE, deletePageEndpoint);

    // GET /{page}/composers/{composerId}
    Endpoint composerEndpoint = new Endpoint("/{page}/composers/{composer}", Method.GET, "getcomposer");
    composerEndpoint.setDescription("Returns the composer with the given id from the indicated page");
    composerEndpoint.addFormat(Format.xml());
    composerEndpoint.addStatus(OK("the composer was found and is returned as part of the response"));
    composerEndpoint.addStatus(NOT_FOUND("the composer was not found or could not be loaded"));
    composerEndpoint.addStatus(BAD_REQUEST("an invalid page or composer identifier was received"));
    composerEndpoint.addStatus(SERVICE_UNAVAILABLE("the site or its content repository is temporarily offline"));
    composerEndpoint.addPathParameter(new Parameter("page", Parameter.Type.String, "The page identifier"));
    composerEndpoint.addPathParameter(new Parameter("composer", Parameter.Type.String, "The composer identifier"));
    composerEndpoint.setTestForm(new TestForm());
    docs.addEndpoint(Endpoint.Type.READ, composerEndpoint);

    // GET /{page}/composers/{composerId}/pagelets/{pageletIndex}
    Endpoint pageletEndpoint = new Endpoint("/{page}/composers/{composer}/pagelets/{pageletindex}", Method.GET, "getpagelet");
    pageletEndpoint.setDescription("Returns the pagelet at the given index from the indicated composer on the page");
    pageletEndpoint.addFormat(Format.xml());
    pageletEndpoint.addStatus(OK("the pagelet was found and is returned as part of the response"));
    pageletEndpoint.addStatus(NOT_FOUND("the pagelet was not found or could not be loaded"));
    pageletEndpoint.addStatus(BAD_REQUEST("an invalid page, composer identifier or pagelet index was received"));
    pageletEndpoint.addStatus(SERVICE_UNAVAILABLE("the site or its content repository is temporarily offline"));
    pageletEndpoint.addPathParameter(new Parameter("page", Parameter.Type.String, "The page identifier"));
    pageletEndpoint.addPathParameter(new Parameter("composer", Parameter.Type.String, "The composer identifier"));
    pageletEndpoint.addPathParameter(new Parameter("pageletindex", Parameter.Type.String, "The zero-based pagelet index"));
    pageletEndpoint.setTestForm(new TestForm());
    docs.addEndpoint(Endpoint.Type.READ, pageletEndpoint);

    return EndpointDocumentationGenerator.generate(docs);
  }

}
