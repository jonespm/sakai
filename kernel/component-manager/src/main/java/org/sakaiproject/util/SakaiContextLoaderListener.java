/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.util;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.impl.SakaiContextLoader;
import org.sakaiproject.component.impl.SpringCompMgr;

import org.springframework.web.context.ContextCleanupListener;

/**
 * <p>
 * Sakai's extension to the Spring ContextLoaderListener - use our ContextLoader, and increment / decrement the child count of the ComponentManager on init / destroy.
 * </p>
 */
public class SakaiContextLoaderListener extends SakaiContextLoader implements ServletContextListener
{
	private static final Logger log = LoggerFactory.getLogger(SakaiContextLoaderListener.class);

	/**
	 * Initialize the root web application context.
	 */
	@Override
	public void contextInitialized(ServletContextEvent event)
	{
		initWebApplicationContext(event.getServletContext());

		// increment the count of children for the component manager
		((SpringCompMgr) ComponentManager.getInstance()).addChildAc();
	}

	/**
	 * Close the root web application context.
	 */
	@Override
	public void contextDestroyed(ServletContextEvent event)
	{
		closeWebApplicationContext(event.getServletContext());
		new ContextCleanupListener().contextDestroyed(event);
		
		log.info("Destroying Components in "+event.getServletContext().getServletContextName());

		// decrement the count of children for the component manager
		((SpringCompMgr) ComponentManager.getInstance()).removeChildAc();
	}
}
