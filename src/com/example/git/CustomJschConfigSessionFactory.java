package com.example.git;

import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig;

import com.jcraft.jsch.Session;

public class CustomJschConfigSessionFactory extends JschConfigSessionFactory {
  @Override
  protected void configure(OpenSshConfig.Host host, Session session) {
      session.setConfig("StrictHostKeyChecking", "no");
  }
}

