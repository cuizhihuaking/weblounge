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

package ch.entwine.weblounge.common.impl.request;

/**
 * Method and header constant definitions used when analyzing
 * <code>HTTP1.1</code> requests.
 */
public interface Http11Constants {

  /* the basic HTTP/1.1 request methods */
  String METHOD_OPTIONS = "OPTIONS";
  String METHOD_GET = "GET";
  String METHOD_HEAD = "HEAD";
  String METHOD_POST = "POST";
  String METHOD_PUT = "PUT";
  String METHOD_DELETE = "DELETE";
  String METHOD_TRACE = "TRACE";
  String METHOD_CONNECT = "CONNECT";

  /* the WebDAV request methods */
  String METHOD_DAV_PROPFIND = "PROPFIND";
  String METHOD_DAV_PROPPATCH = "PROPPATCH";
  String METHOD_DAV_MKCOL = "MKCOL";
  String METHOD_DAV_COPY = "COPY";
  String METHOD_DAV_MOVE = "MOVE";
  String METHOD_DAV_LOCK = "LOCK";
  String METHOD_DAV_UNLOCK = "UNLOCK";

  /* some basic response headers */
  String HEADER_ALLOW = "Allow";
  String HEADER_EXPIRES = "Expires";
  String HEADER_DATE = "Date";
  String HEADER_ETAG = "ETag";
  String HEADER_LAST_MODIFIED = "Last-Modified";
  String HEADER_CONTENT_RANGE = "Content-Range";
  String HEADER_CACHE_CONTROL = "Cache-Control";
  String HEADER_PRAGMA = "Pragma";

  /* some basic request headers */
  String HEADER_IF_MODIFIED_SINCE = "If-Modified-Since";
  String HEADER_IF_NONE_MATCH = "If-None-Match";
  String HEADER_IF_UNMODIFIED_SINCE = "If-Unmodified-Since";
  String HEADER_IF_MATCH = "If-Match";

}
