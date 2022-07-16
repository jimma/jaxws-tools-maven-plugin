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

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItems;

import org.apache.maven.plugin.testing.MojoRule;
import org.codehaus.plexus.component.configurator.ComponentConfigurator;
import org.jboss.test.ws.plugins.tools.utils.AbstractToolsMojoTestCase;
import org.jboss.ws.plugins.tools.AbstractWsConsumeMojo;
import org.jboss.ws.plugins.tools.AbstractWsProvideMojo;
import org.jboss.ws.plugins.tools.TestWsConsumeMojo;
import org.jboss.ws.plugins.tools.TestWsProvideMojo;
import org.jboss.ws.plugins.tools.WsConsumeMojo;
import org.jboss.ws.plugins.tools.WsProvideMojo;
import org.junit.Rule;
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
      internalWsConsumeTest((WsConsumeMojo)getMojo("wsconsume", pluginConfig));
      internalWsConsumeTest((TestWsConsumeMojo)getMojo("wsconsume-test", pluginConfig));
   }

   private static void internalWsConsumeTest(AbstractWsConsumeMojo mojo)
   {
      assertEquals("t", mojo.getTarget());
      assertEquals("output", mojo.getOutputDirectory().getName());
      assertEquals("tp", mojo.getTargetPackage());
      assertEquals("catalog", mojo.getCatalog().getName());
      assertTrue(mojo.getExtension());
      assertEquals("wl", mojo.getWsdlLocation());
      assertEquals("UTF-8", mojo.getEncoding());
      assertEquals("source", mojo.getSourceDirectory().getName());
      assertTrue(mojo.isVerbose());
      assertTrue(mojo.isAdditionalHeaders());
      assertThat(mojo.getBindingFiles(), hasItems(new String[] { "b1" }));
      assertThat(mojo.getClasspathElements(), hasItems(new String[] { "cp1", "cp2" }));
      assertThat(mojo.getWsdls(), hasItems(new String[] { "w1", "w2" }));
      assertTrue(mojo.isFork());
      assertEquals("-Dfoo=bar", mojo.getArgLine());
   }

   @Test
   public void testWsProvidePluginArguments() throws Exception
   {
      final String pluginConfig = "target/test-classes/test-argument/wsprovide-plugin-config.xml";

      internalWsProvideTest((WsProvideMojo)getMojo("wsprovide", pluginConfig));
      internalWsProvideTest((TestWsProvideMojo)getMojo("wsprovide-test", pluginConfig));
   }
   private static void internalWsProvideTest(AbstractWsProvideMojo mojo)
   {
      assertEquals("output", mojo.getOutputDirectory().getName());
      assertEquals("endpoint", mojo.getEndpointClass());
      assertEquals("resource", mojo.getResourceDirectory().getName());
      assertEquals(true, mojo.isGenerateWsdl());
      assertTrue(mojo.getExtension());
      assertEquals("source", mojo.getSourceDirectory().getName());
      assertTrue(mojo.isVerbose());
      assertThat(mojo.getClasspathElements(), hasItems(new String[] { "cp1", "cp2" }));
      assertTrue(mojo.isFork());
      assertEquals("-Dfoo=bar", mojo.getArgLine());
      assertEquals("http://someHost:somePort", mojo.getPortSoapAddress());
   }


}
