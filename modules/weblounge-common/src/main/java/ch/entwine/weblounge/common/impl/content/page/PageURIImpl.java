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

package ch.entwine.weblounge.common.impl.content.page;

import ch.entwine.weblounge.common.content.MalformedResourceURIException;
import ch.entwine.weblounge.common.content.ResourceURI;
import ch.entwine.weblounge.common.impl.content.ResourceURIImpl;
import ch.entwine.weblounge.common.request.WebloungeRequest;
import ch.entwine.weblounge.common.site.Site;


/**
 * This a <code>ResourceURI</code> intended to represent pages of type
 * <code>ch.entwine.weblounge.common.content.page.Page</code>.
 */
public class PageURIImpl extends ResourceURIImpl {

  /** The serial version uid */
  private static final long serialVersionUID = -4786684576702578116L;

  /** The resource type */
  public static final String TYPE = "page";

  /**
   * Creates a new {@link PageURI} from the given request, which is used to
   * determine <code>site</code>, <code>path</code> and <code>version</code>.
   * 
   * @param request
   *          the request
   */
  public PageURIImpl(WebloungeRequest request) {
    super(TYPE, request.getSite(), request.getUrl().getPath(), null, request.getVersion());
  }

  /**
   * Creates a new {@link ResourceURI} that is equal to <code>uri</code> except
   * for the version which is switched to <code>version</code>.
   * 
   * @param uri
   *          the uri
   * @param version
   *          the version
   */
  public PageURIImpl(ResourceURI uri, long version) {
    super(TYPE, uri.getSite(), uri.getPath(), uri.getIdentifier(), version);
  }

  /**
   * Creates a new {@link ResourceURI} pointing to the live version of the page
   * identified by <code>site</code>.
   * <p>
   * <b>Note:</b> Make sure to set <code>id</code> or <code>path</code> prior to
   * the first use of this uri.
   * 
   * @param site
   *          the site
   * @throws MalformedResourceURIException
   *           if the uri cannot be created. Usually, this is due to a malformed
   *           <code>path</code> parameter
   */
  public PageURIImpl(Site site) throws MalformedResourceURIException {
    super(TYPE, site, null);
  }

  /**
   * Creates a new {@link ResourceURI} pointing to the live version of the page
   * identified by <code>site</code> and <code>path</code>.
   * 
   * @param site
   *          the site
   * @param path
   *          the path
   * @throws MalformedResourceURIException
   *           if the uri cannot be created. Usually, this is due to a malformed
   *           <code>path</code> parameter
   */
  public PageURIImpl(Site site, String path)
      throws MalformedResourceURIException {
    super(TYPE, site, path);
  }

  /**
   * Creates a new {@link ResourceURI} pointing to a specific version of the
   * page identified by <code>site</code>, <code>path</code> and
   * <code>version</code> .
   * 
   * @param site
   *          the site
   * @param path
   *          the path
   * @param version
   *          the version
   * @throws MalformedResourceURIException
   *           if the uri cannot be created. Usually, this is due to a malformed
   *           <code>path</code> parameter
   */
  public PageURIImpl(Site site, String path, long version)
      throws MalformedResourceURIException {
    super(TYPE, site, path, version);
  }

  /**
   * Creates a new {@link ResourceURI} pointing to a specific version of the
   * page identified by <code>id<code>, <code>site</code>, <code>path</code> and
   * <code>version</code>.
   * 
   * @param site
   *          the site
   * @param path
   *          the path
   * @param id
   *          the page identifier
   * @throws MalformedResourceURIException
   *           if the uri cannot be created. Usually, this is due to a malformed
   *           <code>path</code> parameter
   */
  public PageURIImpl(Site site, String path, String id)
      throws MalformedResourceURIException {
    super(TYPE, site, path, id);
  }

  /**
   * Creates a new {@link ResourceURI} pointing to a specific version of the
   * page identified by <code>id<code>, <code>site</code>, <code>path</code> and
   * <code>version</code>.
   * 
   * @param site
   *          the site
   * @param path
   *          the path
   * @param id
   *          the page identifier
   * @param version
   *          the version
   * @throws MalformedResourceURIException
   *           if the uri cannot be created. Usually, this is due to a malformed
   *           <code>path</code> parameter
   */
  public PageURIImpl(Site site, String path, String id, long version)
      throws MalformedResourceURIException {
    super(TYPE, site, path, id, version);
  }

}
