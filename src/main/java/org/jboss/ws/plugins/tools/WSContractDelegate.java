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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.maven.plugin.logging.Log;

public class WSContractDelegate
{
   private static final PrintStream PS = System.out;
   private static final String FS = System.getProperty("file.separator");
   private Log log;

   public WSContractDelegate(Log log)
   {
      this.log = log;
   }

   public void runProvider(WSContractProviderParams params) throws Exception
   {
      log.info("run provider");
      log.info("source=" + params.getSourceDirectory());
      log.info("dest=" + params.getOutputDirectory());
      if (params.isFork() || Util.getJVMMajorVersion() > 8)
      {
         log.info("run out of process");
         runProviderOutOfProcess(params);
      }
      else
      {
         log.info("run in process");
         runProviderInProcess(params);
      }
   }

   private void runProviderInProcess(WSContractProviderParams params) throws Exception
   {
      ClassLoader loader = params.getLoader();
      Class<?> providerClass = loader.loadClass("org.jboss.ws.api.tools.WSContractProvider");
      Object provider = providerClass.getMethod("newInstance").invoke(null);
      setupProvider(providerClass, provider, params);
      Method m = providerClass.getMethod("provide", new Class<?>[]{String.class});
      m.invoke(provider, new Object[]{params.getEndpointClass()});
   }

   private void runProviderOutOfProcess(WSContractProviderParams params) throws Exception
   {
      List<String> commandList = initCommandList(params.getArgLine(), params.getManifestOnlyJar(), "org.jboss.ws.tools.cmd.WSProvide");
      getProviderCommandLine(commandList, params);

      log.info("Running command line: " + commandList);

      ProcessBuilder pb = new ProcessBuilder(commandList);
      Process p = pb.start();
      new RT("provider output", p.getInputStream()).start();
      new RT("provider error", p.getErrorStream()).start();
      int result = p.waitFor();

      if (result != 0)
      {
         throw new Exception("Process terminated with code " + result);
      }
   }

   public void runConsumer(WSContractConsumerParams params, String wsdl) throws Exception
   {
      log.info("run consumer " + wsdl);
      log.info("source=" + params.getSourceDirectory());
      log.info("output=" + params.getOutputDirectory());
      if (params.isFork() || Util.getJVMMajorVersion() > 8)
      {
         log.info("run consumer out of process");
         runConsumerOutOfProcess(params, wsdl);
      }
      else
      {
         log.info("run consumer in process");
         runConsumerInProcess(params, wsdl);
      }
   }

   private void runConsumerInProcess(WSContractConsumerParams params, String wsdl) throws Exception
   {
      ClassLoader loader = params.getLoader();
      Class<?> consumerClass = loader.loadClass("org.jboss.ws.api.tools.WSContractConsumer");
      Object consumer = consumerClass.getMethod("newInstance").invoke(null);
      setupConsumer(consumerClass, consumer, params);
      Method m = consumerClass.getMethod("consume", new Class<?>[]{String.class});
      m.invoke(consumer, new Object[]{wsdl});
   }

   private void runConsumerOutOfProcess(WSContractConsumerParams params, String wsdl) throws Exception
   {
      List<String> commandList = initCommandList(params.getArgLine(), params.getManifestOnlyJar(), "org.jboss.ws.tools.cmd.WSConsume");
      getConsumerCommandLine(commandList, params, wsdl);

      log.info("Running command line: " + commandList);

      ProcessBuilder pb = new ProcessBuilder(commandList);
      Process p = pb.start();
      new RT("consumer output", p.getInputStream()).start();
      new RT("consumer error", p.getErrorStream()).start();
      int result = p.waitFor();

      if (result != 0)
      {
         throw new Exception("Process terminated with code " + result);
      }
   }

   /**
    * Write manifest-only jar to the command-line
    *
    * @param argLine
    * @param manifestOnlyJar
    * @param toolClass
    * @return
    * @throws Exception
    */
   private static List<String> initCommandList(String argLine, File manifestOnlyJar, String toolClass) throws Exception
   {
      List<String> commandList = new ArrayList<String>();
      final String fileName = System.getProperty("os.name", "").startsWith("Windows") ? "java.exe" : "java";
      File javaFile = new File (System.getProperty("java.home") + FS + "bin" + FS + fileName);
      commandList.add(javaFile.exists() ? javaFile.getCanonicalPath() : "java");
      if (argLine != null)
      {
         commandList.add(argLine);
      }
      if (Util.getJVMMajorVersion() > 8) {
         commandList.add("--add-modules=java.compiler");
      }
      commandList.add("-classpath");
      commandList.add(manifestOnlyJar.getCanonicalPath());
      commandList.add(toolClass);
      return commandList;
   }

   private static void getConsumerCommandLine(List<String> commandList, WSContractConsumerParams params, String wsdl)
   {
      List<String> bindingFiles = params.getBindingFiles();
      if (bindingFiles != null && !bindingFiles.isEmpty())
      {
         commandList.add("-b");
         commandList.addAll(bindingFiles);
      }
      if (params.isGenerateSource())
      {
         commandList.add("-k");
      }
      if (params.getCatalog() != null)
      {
         commandList.add("-c");
         commandList.add(params.getCatalog().getAbsolutePath());
      }
      if (params.getTargetPackage() != null)
      {
         commandList.add("-p");
         commandList.add(params.getTargetPackage());
      }
      if (params.getWsdlLocation() != null)
      {
         commandList.add("-w");
         commandList.add(params.getWsdlLocation());
      }
      if (params.getEncoding() != null)
      {
         commandList.add("-d");
         commandList.add(params.getEncoding());
      }
      if (params.getOutputDirectory() != null)
      {
         commandList.add("-o");
         commandList.add(params.getOutputDirectory().getAbsolutePath());
      }
      if (params.getSourceDirectory() != null)
      {
         commandList.add("-s");
         commandList.add(params.getSourceDirectory().getAbsolutePath());
      }
      if (params.getTarget() != null)
      {
         commandList.add("-t");
         commandList.add(params.getTarget());
      }
      if (params.isExtension())
      {
         commandList.add("-e");
      }
      if (params.isNoCompile())
      {
         commandList.add("-n");
      }
      if (params.isAdditionalHeaders())
      {
         commandList.add("-a");
      }
      commandList.add(wsdl);
   }

   private static void getProviderCommandLine(List<String> commandList, WSContractProviderParams params)
   {
      if (params.isGenerateSource())
      {
         commandList.add("-k");
      }
      if (params.isGenerateWsdl())
      {
         commandList.add("-w");
      }
      if (params.getOutputDirectory() != null)
      {
         commandList.add("-o");
         commandList.add(params.getOutputDirectory().getAbsolutePath());
      }
      if (params.getSourceDirectory() != null)
      {
         commandList.add("-s");
         commandList.add(params.getSourceDirectory().getAbsolutePath());
      }
      if (params.getResourceDirectory() != null)
      {
         commandList.add("-r");
         commandList.add(params.getResourceDirectory().getAbsolutePath());
      }
      if (params.isExtension())
      {
         commandList.add("-e");
      }
      if(params.getPortSoapAddress() != null){
         commandList.add("-a");
         commandList.add(params.getPortSoapAddress());
      }
      commandList.add(params.getEndpointClass());
   }

   private static void setupConsumer(Class<?> consumerClass, Object consumer, WSContractConsumerParams params) throws Exception
   {
      callMethod(consumerClass, consumer, "setAdditionalCompilerClassPath", params.getAdditionalCompilerClassPath());
      Method m = consumerClass.getMethod("setMessageStream", new Class<?>[]{PrintStream.class});
      m.invoke(consumer, new Object[]{PS});
      List<String> bindingFiles = params.getBindingFiles();
      if (bindingFiles != null && !bindingFiles.isEmpty())
      {
         List<File> files = new LinkedList<File>();
         for (String bf : bindingFiles)
         {
            files.add(new File(bf));
         }
         callMethod(consumerClass, consumer, "setBindingFiles", files);
      }
      if (params.getCatalog() != null)
      {
         callMethod(consumerClass, consumer, "setCatalog", params.getCatalog());
      }
      callMethod(consumerClass, consumer, "setAdditionalHeaders", params.isAdditionalHeaders());
      callMethod(consumerClass, consumer, "setExtension", params.isExtension());
      callMethod(consumerClass, consumer, "setGenerateSource", params.isGenerateSource());
      callMethod(consumerClass, consumer, "setNoCompile", params.isNoCompile());
      if (params.getOutputDirectory() != null)
      {
         callMethod(consumerClass, consumer, "setOutputDirectory", params.getOutputDirectory());
      }
      if (params.getSourceDirectory() != null)
      {
         callMethod(consumerClass, consumer, "setSourceDirectory", params.getSourceDirectory());
      }
      if (params.getTarget() != null)
      {
         callMethod(consumerClass, consumer, "setTarget", params.getTarget());
      }
      if (params.getTargetPackage() != null)
      {
         callMethod(consumerClass, consumer, "setTargetPackage", params.getTargetPackage());
      }
      if (params.getWsdlLocation() != null)
      {
         callMethod(consumerClass, consumer, "setWsdlLocation", params.getWsdlLocation());
      }
      if (params.getEncoding() != null)
      {
         callMethod(consumerClass, consumer, "setEncoding", params.getEncoding());
      }
   }

   private static Object callMethod(Class<?> clazz, Object obj, String name, boolean param) throws Exception
   {
      Method m = clazz.getMethod(name, new Class<?>[]{boolean.class});
      return m.invoke(obj, new Object[]{param});
   }

   private static <T> Object callMethod(Class<?> clazz, Object obj, String name, T param) throws Exception
   {
      Method m = clazz.getMethod(name, new Class<?>[]{param.getClass()});
      return m.invoke(obj, new Object[]{param});
   }

   @SuppressWarnings("rawtypes")
   private static Object callMethod(Class<?> clazz, Object obj, String name, List param) throws Exception
   {
      Method m = clazz.getMethod(name, new Class<?>[]{List.class});
      return m.invoke(obj, new Object[]{param});
   }

   private static void setupProvider(Class<?> providerClass, Object provider, WSContractProviderParams params) throws Exception
   {
      Method m = providerClass.getMethod("setClassLoader", new Class<?>[]{ClassLoader.class});
      m.invoke(provider, new Object[]{params.getLoader()});
      callMethod(providerClass, provider, "setExtension", params.isExtension());
      callMethod(providerClass, provider, "setGenerateSource", params.isGenerateSource());
      callMethod(providerClass, provider, "setGenerateWsdl", params.isGenerateWsdl());
      Method m2 = providerClass.getMethod("setMessageStream", new Class<?>[]{PrintStream.class});
      m2.invoke(provider, new Object[]{PS});
      if (params.getOutputDirectory() != null)
      {
         callMethod(providerClass, provider, "setOutputDirectory", params.getOutputDirectory());
      }
      if (params.getResourceDirectory() != null)
      {
         callMethod(providerClass, provider, "setResourceDirectory", params.getResourceDirectory());
      }
      if (params.getSourceDirectory() != null)
      {
         callMethod(providerClass, provider, "setSourceDirectory", params.getSourceDirectory());
      }
      if (params.getPortSoapAddress() != null)
      {
         callMethod(providerClass, provider, "setPortSoapAddress", params.getPortSoapAddress());
      }
   }

   private class RT extends Thread {
      private final InputStream is;
      private final String name;
      public RT(String name, InputStream is) {
         setDaemon(true);
         this.name = name;
         this.is = is;
      }
      @Override
      public void run () {
         try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            String l;
            while ((l = br.readLine()) != null) {
               log.info(name + ": " + l);
            }
         } catch (IOException e) {
            log.warn("could not read", e);
         }
      }
   }
}