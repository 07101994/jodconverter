// JODConverter - Java OpenDocument Converter
// Copyright 2004-2012 Mirko Nasato and contributors
//
// JODConverter is Open Source software, you can redistribute it and/or
// modify it under either (at your option) of the following licenses
//
// 1. The GNU Lesser General Public License v3 (or later)
//    -> http://www.gnu.org/licenses/lgpl-3.0.txt
// 2. The Apache License, Version 2.0
//    -> http://www.apache.org/licenses/LICENSE-2.0.txt
//
//
package org.artofsolving.jodconverter.office;


import java.io.File;

import org.artofsolving.jodconverter.process.LinuxProcessManager;
import org.artofsolving.jodconverter.process.ProcessManager;
import org.artofsolving.jodconverter.process.PureJavaProcessManager;
import org.artofsolving.jodconverter.process.SigarProcessManager;
import org.artofsolving.jodconverter.util.PlatformUtils;


public class DefaultOfficeManagerConfiguration {
  public static final long         DEFAULT_RETRY_TIMEOUT  = 120000L;
  public static final long         DEFAULT_RETRY_INTERVAL = 250L;
  private File                     officeHome             = OfficeUtils.getDefaultOfficeHome();
  private OfficeConnectionProtocol connectionProtocol     = OfficeConnectionProtocol.SOCKET;
  private int[]                    portNumbers            = new int[] { 2002 };
  private String[]                 pipeNames              = new String[] { "office" };
  private String[]                 runAsArgs              = null;
  private File                     templateProfileDir     = null;
  private File                     workDir                = new File(System.getProperty("java.io.tmpdir"));
  private long                     taskQueueTimeout       = 30000L;                                        // 30 seconds
  private long                     taskExecutionTimeout   = 120000L;                                       // 2 minutes
  private int                      maxTasksPerProcess     = 200;
  private long                     retryTimeout           = DEFAULT_RETRY_TIMEOUT;
  private long                     retryInterval          = DEFAULT_RETRY_INTERVAL;
  private ProcessManager           processManager         = null;                                          // lazily initialised


  public DefaultOfficeManagerConfiguration setOfficeHome(String officeHome) throws NullPointerException, IllegalArgumentException {
    requireNonNull(officeHome, "officeHome must not be null");
    return setOfficeHome(new File(officeHome));
  }

  public DefaultOfficeManagerConfiguration setOfficeHome(File officeHome) throws NullPointerException, IllegalArgumentException {
    requireNonNull(officeHome, "officeHome must not be null");
    checkArgument("officeHome", officeHome.isDirectory(), "must exist and be a directory");
    this.officeHome = officeHome;
    return this;
  }

  public DefaultOfficeManagerConfiguration setConnectionProtocol(OfficeConnectionProtocol connectionProtocol) throws NullPointerException {
    requireNonNull(connectionProtocol, "connectionProtocol must not be null");
    this.connectionProtocol = connectionProtocol;
    return this;
  }

  public DefaultOfficeManagerConfiguration setPortNumber(int portNumber) {
    this.portNumbers = new int[] { portNumber };
    return this;
  }

  public DefaultOfficeManagerConfiguration setPortNumbers(int... portNumbers) throws NullPointerException, IllegalArgumentException {
    requireNonNull(portNumbers, "portNumbers must not be null");
    checkArgument("portNumbers", portNumbers.length > 0, "must not be empty");
    this.portNumbers = portNumbers;
    return this;
  }

  public DefaultOfficeManagerConfiguration setPipeName(String pipeName) throws NullPointerException {
    requireNonNull(pipeName, "pipeName must not be null");
    this.pipeNames = new String[] { pipeName };
    return this;
  }

  public DefaultOfficeManagerConfiguration setPipeNames(String... pipeNames) throws NullPointerException, IllegalArgumentException {
    requireNonNull(pipeNames, "pipeNames must not be null");
    checkArgument("pipeNames", pipeNames.length > 0, "must not be empty");
    this.pipeNames = pipeNames;
    return this;
  }

  public DefaultOfficeManagerConfiguration setRunAsArgs(String... runAsArgs) {
    this.runAsArgs = runAsArgs;
    return this;
  }

  public DefaultOfficeManagerConfiguration setTemplateProfileDir(File templateProfileDir) throws IllegalArgumentException {
    if (templateProfileDir != null) {
      checkArgument("templateProfileDir", templateProfileDir.isDirectory(), "must exist and be a directory");
    }
    this.templateProfileDir = templateProfileDir;
    return this;
  }

  /**
   * Sets the directory where temporary office profiles will be created.
   * <p>
   * Defaults to the system temporary directory as specified by the
   * <code>java.io.tmpdir</code> system property.
   * 
   * @param workDir
   * @return
   */
  public DefaultOfficeManagerConfiguration setWorkDir(File workDir) {
    requireNonNull(workDir, "workDir must not be null");
    this.workDir = workDir;
    return this;
  }

  public DefaultOfficeManagerConfiguration setTaskQueueTimeout(long taskQueueTimeout) {
    this.taskQueueTimeout = taskQueueTimeout;
    return this;
  }

  public DefaultOfficeManagerConfiguration setTaskExecutionTimeout(long taskExecutionTimeout) {
    this.taskExecutionTimeout = taskExecutionTimeout;
    return this;
  }

  public DefaultOfficeManagerConfiguration setMaxTasksPerProcess(int maxTasksPerProcess) {
    this.maxTasksPerProcess = maxTasksPerProcess;
    return this;
  }

  /**
   * Provide a specific {@link ProcessManager} implementation
   * <p>
   * The default is to use {@link SigarProcessManager} if sigar.jar is available
   * in the classpath, otherwise {@link LinuxProcessManager} on Linux and
   * {@link PureJavaProcessManager} on other platforms.
   * 
   * @param processManager
   * @return
   * @throws NullPointerException
   */
  public DefaultOfficeManagerConfiguration setProcessManager(ProcessManager processManager) throws NullPointerException {
    requireNonNull(processManager, "processManager must not be null");
    this.processManager = processManager;
    return this;
  }

  /**
   * Retry timeout set in milliseconds. Used for retrying office process calls.
   * If not set, it defaults to 2 minutes
   * 
   * @param retryTimeout
   *          in milliseconds
   * @return
   */
  public DefaultOfficeManagerConfiguration setRetryTimeout(long retryTimeout) {
    this.retryTimeout = retryTimeout;
    return this;
  }

  /**
   * Retry interval set in milliseconds. Used for retrying office process calls.
   * If not set, it defaults to 250 milliseconds
   * 
   * @param retryTimeout
   *          in milliseconds
   * @return
   */
  public DefaultOfficeManagerConfiguration setRetryInterval(long retryInterval) {
    this.retryInterval = retryInterval;
    return this;
  }

  public OfficeManager buildOfficeManager() throws IllegalStateException {
    if (this.officeHome == null) {
      throw new IllegalStateException("officeHome not set and could not be auto-detected");
    } else if (!this.officeHome.isDirectory()) {
      throw new IllegalStateException("officeHome doesn't exist or is not a directory: " + this.officeHome);
    } else if (!OfficeUtils.getOfficeExecutable(this.officeHome).isFile()) {
      throw new IllegalStateException("invalid officeHome: it doesn't contain soffice.bin: " + this.officeHome);
    }
    if (this.templateProfileDir != null && !isValidProfileDir(this.templateProfileDir)) {
      throw new IllegalStateException("templateProfileDir doesn't appear to contain a user profile: " + this.templateProfileDir);
    }
    if (!this.workDir.isDirectory()) {
      throw new IllegalStateException("workDir doesn't exist or is not a directory: " + this.workDir);
    }

    if (this.processManager == null) {
      this.processManager = findBestProcessManager();
    }

    int numInstances = this.connectionProtocol == OfficeConnectionProtocol.PIPE ? this.pipeNames.length : this.portNumbers.length;
    UnoUrl[] unoUrls = new UnoUrl[numInstances];
    for (int i = 0; i < numInstances; i++) {
      unoUrls[i] = (this.connectionProtocol == OfficeConnectionProtocol.PIPE) ? UnoUrl.pipe(this.pipeNames[i]) : UnoUrl.socket(this.portNumbers[i]);
    }
    return new ProcessPoolOfficeManager(
        this.officeHome, unoUrls, this.runAsArgs, this.templateProfileDir, this.workDir,
        this.retryTimeout, this.retryInterval, this.taskQueueTimeout, this.taskExecutionTimeout, this.maxTasksPerProcess,
        this.processManager);
  }

  private ProcessManager findBestProcessManager() {
    if (isSigarAvailable()) {
      return new SigarProcessManager();
    } else if (PlatformUtils.isLinux()) {
      LinuxProcessManager processManager = new LinuxProcessManager();
      if (this.runAsArgs != null) {
        processManager.setRunAsArgs(this.runAsArgs);
      }
      return processManager;
    } else {
      // NOTE: UnixProcessManager can't be trusted to work on Solaris
      // because of the 80-char limit on ps output there
      return new PureJavaProcessManager();
    }
  }

  private boolean isSigarAvailable() {
    try {
      Class.forName("org.hyperic.sigar.Sigar", false, getClass().getClassLoader());
      return true;
    } catch (ClassNotFoundException classNotFoundException) {
      return false;
    }
  }

  // private void checkArgumentNotNull(String argName, Object argValue) throws
  // NullPointerException
  // {
  // if (argValue == null) {
  // throw new NullPointerException(argName + " must not be null");
  // }
  // }

  private void checkArgument(String argName, boolean condition, String message) throws IllegalArgumentException {
    if (!condition) {
      throw new IllegalArgumentException(argName + " " + message);
    }
  }

  private boolean isValidProfileDir(File profileDir) {
    return new File(profileDir, "user").isDirectory();
  }

  // Objects.requireNonNull(T, String) fallback for JDK < 1.7
  private static <T> T requireNonNull( T obj, String message) {
    if ( obj == null)
      throw new NullPointerException( message);
      return obj;
  }

}
