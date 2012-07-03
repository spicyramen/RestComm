/*
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.mobicents.servlet.sip.restcomm.http;

import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static javax.ws.rs.core.Response.ok;
import static javax.ws.rs.core.Response.status;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.apache.shiro.authz.AuthorizationException;
import org.mobicents.servlet.sip.restcomm.ServiceLocator;
import org.mobicents.servlet.sip.restcomm.Sid;
import org.mobicents.servlet.sip.restcomm.annotations.concurrency.ThreadSafe;
import org.mobicents.servlet.sip.restcomm.dao.DaoManager;
import org.mobicents.servlet.sip.restcomm.dao.NotificationsDao;
import org.mobicents.servlet.sip.restcomm.entities.Notification;
import org.mobicents.servlet.sip.restcomm.entities.NotificationList;
import org.mobicents.servlet.sip.restcomm.entities.RestCommResponse;
import org.mobicents.servlet.sip.restcomm.http.converter.NotificationConverter;
import org.mobicents.servlet.sip.restcomm.http.converter.NotificationListConverter;
import org.mobicents.servlet.sip.restcomm.http.converter.RestCommResponseConverter;

import com.thoughtworks.xstream.XStream;

/**
 * @author quintana.thomas@gmail.com (Thomas Quintana)
 */
@Path("/Accounts/{accountSid}/Notifications")
@ThreadSafe public final class NotificationsEndpoint extends AbstractEndpoint {
  private final NotificationsDao dao;
  private final XStream xstream;
  
  public NotificationsEndpoint() {
    super();
    final ServiceLocator services = ServiceLocator.getInstance();
    dao = services.get(DaoManager.class).getNotificationsDao();
    final NotificationConverter converter = new NotificationConverter();
    xstream = new XStream();
    xstream.alias("RestcommResponse", RestCommResponse.class);
    xstream.registerConverter(converter);
    xstream.registerConverter(new NotificationListConverter());
    xstream.registerConverter(new RestCommResponseConverter());
  }
  
  @Path("/{sid}")
  @GET public Response getNotification(@PathParam("accountSid") String accountSid, @PathParam("sid") String sid) {
    try { secure(new Sid(accountSid), "RestComm:Read:Notifications"); }
    catch(final AuthorizationException exception) { return status(UNAUTHORIZED).build(); }
    final Notification notification = dao.getNotification(new Sid(sid));
    if(notification == null) {
      return status(NOT_FOUND).build();
    } else {
      final RestCommResponse response = new RestCommResponse(notification);
      return ok(xstream.toXML(response), APPLICATION_XML).build();
    }
  }
  
  @GET public Response getNotifications(@PathParam("accountSid") String accountSid) {
    try { secure(new Sid(accountSid), "RestComm:Read:Notifications"); }
    catch(final AuthorizationException exception) { return status(UNAUTHORIZED).build(); }
    final List<Notification> notifications = dao.getNotifications(new Sid(accountSid));
    final RestCommResponse response = new RestCommResponse(new NotificationList(notifications));
    return ok(xstream.toXML(response), APPLICATION_XML).build();
  }
}
