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
package org.jboss.test.ws.plugins.tools.utils;

import java.io.File;

import java.nio.file.Path;
import org.apache.maven.execution.DefaultMavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.maven.plugin.testing.MojoRule;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingRequest;
import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.junit.Assert;
import org.junit.Rule;

import static org.junit.Assert.assertNotNull;

/**
 * Utility class for providing additional means of looking up mojo classes.
 * 
 * @author alessio.soldano@jboss.com
 * @since 26-Feb-2010
 *
 */
public abstract class AbstractToolsMojoTestCase
{

   @Rule
   public MojoRule rule = new MojoRule() {
      @Override
      protected void before() throws Throwable {
      }

      @Override
      protected void after() {
      }
   };

   @SuppressWarnings("unchecked")
   protected <T extends Mojo> T getMojo(String goal, String pomXml) throws Exception
   {
      File testPom = new File(pomXml);

      MavenProject project = readMavenProject(testPom);
      T mojo = (T) rule.lookupConfiguredMojo(project, goal);
      assertNotNull(mojo);
      return mojo;
   }
   public MavenProject readMavenProject(File pom)
           throws Exception {
      MavenExecutionRequest request = new DefaultMavenExecutionRequest();
      request.setBaseDirectory(pom.getParentFile());
      ProjectBuildingRequest configuration = request.getProjectBuildingRequest();
      configuration.setRepositorySession(new DefaultRepositorySystemSession());
      MavenProject project = rule.lookup(ProjectBuilder.class).build(pom, configuration).getProject();
      Assert.assertNotNull(project);
      return project;
   }


   protected class DebugEnabledLog extends SystemStreamLog
   {
      public DebugEnabledLog()
      {
         super();
      }

      @Override
      public boolean isDebugEnabled()
      {
         return true;
      }
   }

}
