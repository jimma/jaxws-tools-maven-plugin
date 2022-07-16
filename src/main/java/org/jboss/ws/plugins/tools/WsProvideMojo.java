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
package org.jboss.ws.plugins.tools;

import java.io.File;
import java.util.List;

import org.apache.maven.artifact.Artifact;

/**
 * Runs wsprovide tool
 * 
 * @goal wsprovide
 * @phase process-classes
 * @requiresDependencyResolution compile+runtime
 * @description JBossWS WSProvide Maven Plugin.
 * 
 * @author alessio.soldano@jboss.com
 * @since 25-Feb-2010
 */
public class WsProvideMojo extends AbstractWsProvideMojo
{
    /**
    * Sets the main output directory. If the directory does not exist, it will be created.
    * 
    * @parameter default-value="${project.build.outputDirectory}"
    */
   protected File outputDirectory;

   /**
    * Project classpath.
    *
    * @parameter property="project.classpathElements"
    * @required
    * @readonly
    */
   protected List<String> classpathElements;
   
   /**
    * The plugin dependency artifacts
    * 
    * @parameter default-value="${plugin.artifacts}"
    * @required
    * @readonly
    */
   private List<Artifact> pluginArtifacts;

   @Override
   protected void updateProjectSourceRoots()
   {
      project.addCompileSourceRoot(sourceDirectory.getAbsolutePath());
   }

   @Override
   public File getOutputDirectory()
   {
      return outputDirectory;
   }

   @Override
   public List<String> getClasspathElements()
   {
      return classpathElements;
   }

   @Override
   public List<Artifact> getPluginArtifacts()
   {
      return pluginArtifacts;
   }
}
