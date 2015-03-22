/*
 * <one line to give the program's name and a brief idea of what it does.>
 * Copyright (C) 2015  <copyright holder> <email>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

#ifndef AGENTRUNTIME_H
#define AGENTRUNTIME_H

#include <stdlib.h>
#include <jvmti.h>
#include <string>

#include "javathreadinfo.h"
#include <sys/types.h>
#include <pthread.h>
#include "agentoptions.h"

#include <boost/log/core.hpp>
#include <boost/log/trivial.hpp>
#include <boost/log/expressions.hpp>
#include <boost/log/sinks/text_file_backend.hpp>
#include <boost/log/utility/setup/file.hpp>
#include <boost/log/utility/setup/common_attributes.hpp>
#include <boost/log/sources/severity_logger.hpp>
#include <boost/log/sources/record_ostream.hpp>

using namespace std;

class AgentOptions;

class AgentRuntime
{

public:
  AgentRuntime(jvmtiEnv *jvmti);

  void *JVMTIAllocate(int len);
  void JVMTIFree(void *ptr);
  void JVMTIAddJarToClasspath(const char *name);
  void JVMTIExitIfError(jvmtiError errnum, const char *str);

  void agentGlobalLock();
  void agentGlobalUnlock();

  void VmStarted();
  void VmDead();

  bool isVmStarted();
  bool isVmDead();

  JavaThreadInfo getThreadInfo(jthread thread);
  JavaThreadInfo getCurrentThreadInfo();

  string getEnvVariable(string name);

  AgentOptions *getOptions();

  void logTrace(string message);
  void logDebug(string message);
  void logInfo(string message);
  void logWarning(string message);
  void logError(string message);
  void logFatal(string message);

private:
  boost::log::sources::severity_logger<boost::log::trivial::severity_level> &getLogger();
  boost::log::sources::severity_logger<boost::log::trivial::severity_level> logger;

  AgentOptions *options;
  int loadedClasses;

  jvmtiEnv *jvmti;
  bool vm_dead;
  bool vm_started;

  jrawMonitorID lock;
};

void  fatal_error(const char * format, ...);

#endif // AGENTRUNTIME_H
