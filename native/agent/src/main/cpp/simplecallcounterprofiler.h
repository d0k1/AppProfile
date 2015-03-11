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

#ifndef SIMPLECALLCOUNTERPROFILER_H
#define SIMPLECALLCOUNTERPROFILER_H

#include "abstracttracingprofiler.h"
#include <map>
#include "javamethodinfo.h"

using namespace std;

struct CallStatistics final {
  unsigned int callCount;
  unsigned int returnCount;
};

class SimpleCallCounterProfiler final : public AbstractTracingProfiler 
{
public:
  SimpleCallCounterProfiler();
  virtual void methodEntry(int cnum, int mnum, jobject thread) override;
  virtual void methodExit(int cnum, int mnum, jobject thread) override;
  virtual void printOnExit() override;
  
  virtual void methodInstrumented(JavaMethodInfo *info) override;
  virtual void threadStarted(jobject thread);
  virtual void threadStopped(jobject thread);
private:
  map<unsigned long, map<unsigned long, CallStatistics*>*> statByThread;
};

#endif // SIMPLECALLCOUNTERPROFILER_H
