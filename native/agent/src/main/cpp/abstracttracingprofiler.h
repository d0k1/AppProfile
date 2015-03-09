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

#ifndef ABSTRACTTRACINGPROFILER_H
#define ABSTRACTTRACINGPROFILER_H

#include "javaclassesinfo.h"
#include "javathreadsinfo.h"
#include "agentruntime.h"

class AbstractTracingProfiler
{
public:
  
  virtual void setData(AgentRuntime *runtime, JavaClassesInfo *classes, JavaThreadsInfo *threads) final;
  
  virtual void methodEntry(int cnum, int mnum)=0;
  virtual void methodExit(int cnum, int mnum)=0;
  virtual void printOnExit()=0;

  virtual void methodInstrumented(JavaMethodInfo *info)=0;
  virtual void threadStarted(JavaThreadInfo *)=0;
protected:
  
  virtual AgentRuntime *getRuntime() final;
  virtual JavaClassesInfo *getClasses() final;
  virtual JavaThreadsInfo *getThreads() final;
  
private:
  AgentRuntime *runtime;
  JavaClassesInfo *classes;
  JavaThreadsInfo *threads;
};

#endif // ABSTRACTTRACINGPROFILER_H
