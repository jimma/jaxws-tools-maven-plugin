/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
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
package org.jboss.test.ws.plugins.tools;

import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItems;

import org.jboss.test.ws.plugins.tools.utils.AbstractToolsMojoTestCase;
import org.jboss.ws.plugins.tools.WsConsumeMojo;
import org.jboss.ws.plugins.tools.WsProvideMojo;
import org.junit.Test;

/**
 * A test for checking tools mojo arguments are correctly understood
 * 
 * @author alessio.soldano@jboss.com
 * @since 26-Feb-2010
 * 
 */
public class ArgumentTest extends AbstractToolsMojoTestCase
{

   @Test
   public void testWsConsumePluginArguments() throws Exception
   {
      final String pluginConfig = "target/test-classes/test-argument/wsconsume-plugin-config.xml";

      WsConsumeMojo mojo = getMojo(WsConsumeMojo.class, "wsconsume", pluginConfig);
      assertEquals("t", mojo.getTarget());
      assertEquals("output", mojo.getOutputDirectory().getName());
      assertEquals("tp", mojo.getTargetPackage());
      assertEquals("catalog", mojo.getCatalog().getName());
      assertTrue(mojo.getExtension());
      assertEquals("wl", mojo.getWsdlLocation());
      assertEquals("source", mojo.getSourceDirectory().getName());
      assertTrue(mojo.isVerbose());
      assertThat(mojo.getBindingFiles(), hasItems(new String[] { "b1" }));
      assertThat(mojo.getClasspathElements(), hasItems(new String[] { "cp1", "cp2" }));
      assertThat(mojo.getWsdls(), hasItems(new String[] { "w1", "w2" }));
   }

   @Test
   public void testWsProvidePluginArguments() throws Exception
   {
      final String pluginConfig = "target/test-classes/test-argument/wsprovide-plugin-config.xml";

      WsProvideMojo mojo = getMojo(WsProvideMojo.class, "wsprovide", pluginConfig);
      assertEquals("output", mojo.getOutputDirectory().getName());
      assertEquals("endpoint", mojo.getEndpointClass());
      assertEquals("resource", mojo.getResourceDirectory().getName());
      assertEquals(true, mojo.isGenerateWsdl());
      assertTrue(mojo.getExtension());
      assertEquals("source", mojo.getSourceDirectory().getName());
      assertTrue(mojo.isVerbose());
      assertThat(mojo.getClasspathElements(), hasItems(new String[] { "cp1", "cp2" }));
   }

}