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
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.project.MavenProject;

/**
 * Abstract mojo all the other need to extend
 * 
 * @author alessio.soldano@jboss.com
 * @since 24-Feb-2010
 *
 */
abstract class AbstractToolsMojo extends AbstractMojo
{
   /**
   * @parameter property="project"
   * @readonly
   */
   protected MavenProject project;

   protected Boolean generateSource = true; //always generate sources, as we're having them compiled by Maven
   
   /**
    * Enables/Disables SOAP 1.2 binding extension
    * 
    * @parameter default-value="false"
    */
   protected Boolean extension;

   /**
    * 
    * @parameter default-value="false"
    */
   protected Boolean verbose;
   
   /**
    * An optional additional argLine to be used when running in fork mode; can be used to set endorse dir, enable debugging, etc. 
    * @parameter
    */
   protected String argLine;
   
   /**
    * @parameter default-value="false"
    */
   protected Boolean fork;

   /**
    * Either ${build.outputDirectory} or ${build.testOutputDirectory}.
    */
   public abstract File getOutputDirectory();
   
   /**
    * Either ${project.compileClasspathElements} or ${project.testClasspathElements}
    */
   public abstract List<String> getClasspathElements();
   
   public abstract List<Artifact> getPluginArtifacts();
   
   /**
    * Update the current Maven project source roots with the generated classes / resources
    */
   protected abstract void updateProjectSourceRoots();
   
   protected URLClassLoader getMavenClasspathAwareClassLoader()
   {
      List<String> classpath = getClasspathElements();
      final int size = classpath.size();
      URL[] urls = new URL[size];
      for (int i=0; i < size; i++)
      {
         try
         {
            urls[i] = new File(classpath.get(i)).toURI().toURL();
         }
         catch (MalformedURLException mue)
         {
            getLog().warn("Skipping invalid classpath element: " + classpath.get(i));
         }
      }
      return new URLClassLoader(urls, null);
   }
   
   /**
    * Return the plugin dependencies that are required to actually call the tools in fork mode
    * (jbossws-common-tools and his transitive dependencies getopt and log4j)
    * Those dependencies would usually be added at the end of the classpath as they do not come
    * in from the stack dependency tree because:
    * 1) jbossws-common-tools is not a required dep for stack clients
    * 2) log4j and getopt need to be provided scope deps in jbossws-commons-tools, hence they
    *    are not transitive
    *  
    * @return a list with the required plugin dependencies
    */
   protected List<File> getRequiredPluginDependencyPaths()
   {
      //TODO!! retrieve the actual version to be used from the included stack dependency instead?
      List<File> result = new ArrayList<File>(3);
      for (Artifact s : getPluginArtifacts()) {
         if ("org.jboss.ws".equals(s.getGroupId()) && "jbossws-common-tools".equals(s.getArtifactId()))
         {
            result.add(s.getFile());
         }
         else if ("gnu-getopt".equals(s.getGroupId()) && "getopt".equals(s.getArtifactId()))
         {
            result.add(s.getFile());
         }
         else if ("gnu.getopt".equals(s.getGroupId()) && "java-getopt".equals(s.getArtifactId()))
         {
            result.add(s.getFile());
         }
         else if ("log4j".equals(s.getGroupId()) && "log4j".equals(s.getArtifactId()))
         {
            result.add(s.getFile());
         }
      }
      return result;
   }

   public Boolean getExtension()
   {
      return extension;
   }

   public Boolean isVerbose()
   {
      return verbose;
   }
   
   public String getArgLine()
   {
      return argLine;
   }
   
   public Boolean isFork()
   {
      return fork;
   }

   /**
    * Create a jar with just a manifest containing a Main-Class entry and a Class-Path entry
    * for all classpath elements.
    *
    * @param classPath      List&lt;String> of all classpath elements.
    * @param startClassName  The classname to start (main-class)
    * @return The file pointint to the jar
    * @throws java.io.IOException When a file operation fails.
    */
   public File createJar(List<String> classPath, String startClassName) throws IOException
   {
      File tempDirectory = new File(getOutputDirectory().getParentFile(), "jaxws-tools");
      tempDirectory.mkdirs();
      File file = File.createTempFile("jaxws-tools-maven-plugin-classpath-", ".jar", tempDirectory);

      FileOutputStream fos = new FileOutputStream(file);
      JarOutputStream jos = new JarOutputStream(fos);
      jos.setLevel(JarOutputStream.STORED);
      JarEntry je = new JarEntry("META-INF/MANIFEST.MF");
      jos.putNextEntry(je);

      Manifest man = new Manifest();

      StringBuilder cp = new StringBuilder();
      for ( String el : classPath )
      {
         cp.append(new File(el).toURI().toURL().toExternalForm());
         cp.append(" ");
      }
      List<File> pluginDeps = getRequiredPluginDependencyPaths();
      for (File f : pluginDeps) {
          cp.append(f.toURI().toURL().toExternalForm());
          cp.append(" ");
      }

      man.getMainAttributes().putValue("Manifest-Version", "1.0");
      man.getMainAttributes().putValue("Class-Path", cp.toString().trim());
      man.getMainAttributes().putValue("Main-Class", startClassName);

      man.write(jos);
      jos.close();

      return file;
   }
}
