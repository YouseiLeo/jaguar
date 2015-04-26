/*******************************************************************************
 * Copyright (c) 2006, 2014 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 ******************************************************************************/
package br.usp.each.saeg.jaguar.plugin.launching;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.jacoco.agent.AgentJar;
import org.jacoco.core.runtime.AgentOptions;

/**
 * Internal utility to calculate the agent VM parameter.
 */
public class AgentArgumentSupport {

  public AgentArgumentSupport() {

  }

  /**
   * Returns a wrapper for the given launch configuration that adds the required
   * VM argument.
   * 
   * @param serverPort
   *          port of the local agent server
   * @param config
   *          launch configuration to wrap
   * @return wrapped launch configuration
   */
  public ILaunchConfiguration addArgument(int serverPort,
      ILaunchConfiguration config) throws CoreException {
    return new AdjustedLaunchConfiguration(getArgument(serverPort), config);
  }

  protected String getArgument(int serverPort) throws CoreException {
    final AgentOptions options = new AgentOptions();
    // options.setIncludes(preferences.getAgentIncludes());
    // options.setExcludes(preferences.getAgentExcludes());
    // options.setExclClassloader(preferences.getAgentExclClassloader());
    options.setOutput(AgentOptions.OutputMode.tcpclient);
    options.setPort(serverPort);
    return quote(options.getVMArgument(getAgentFile()));
  }

  protected File getAgentFile() throws CoreException {
    try {
      final URL agentfileurl = FileLocator.toFileURL(AgentJar.getResource());
      return new Path(agentfileurl.getPath()).toFile();
    } catch (IOException e) {
    	// TODO  throw new CoreException(EclEmmaStatus.NO_LOCAL_AGENTJAR_ERROR.getStatus(e));
    	e.printStackTrace();
    	return null;
    }
  }

  protected String quote(String arg) {
    if (arg.indexOf(' ') == -1) {
      return arg;
    } else {
      return '"' + arg + '"';
    }
  }

}
