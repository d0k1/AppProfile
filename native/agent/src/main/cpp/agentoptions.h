/*
 * <one line to give the program's name and a brief idea of what it does.>
 * Copyright (C) 2015  Denis V. Kirpichenkov <email>
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

#ifndef AGENTOPTIONS_H
#define AGENTOPTIONS_H

#include <boost/program_options.hpp>
#include <iostream>
#include <fstream>
#include "abstracttracingprofiler.h"

using namespace boost::program_options;
using namespace std;

class AbstractTracingProfiler;

class AgentOptions
{
public:
  AgentOptions(string filename);

  bool isClassExcluded(const char *klass);
  string getHelperJar();

  AbstractTracingProfiler *getTracingProfiler();
  bool isTracingProfilerPrintOnExit();
  bool isPrintVMEvents();
  bool isPrintInstrumentedClasses();
  bool isCsvOnExit();
  int getTracingProfilerDepth();
  int getTimerFrequency();
  bool isMemoryTracking();
  bool isMemoryTrackingEvents();
private:
  int tracingMaxDepth;
  bool printVMEvents;
  bool printInstrumentedClassnames;
  bool profilerPrintOnExit;
  bool csvOnExit;
  bool memoryTracking;
  bool memoryTrackingEvents;

  string agentExclude;
  string agentExcludeIgnore;
  string agentInclude;
  string agentIncludeIgnore;
  string appId;
  string helperJar;

  vector<string> excludes;
  vector<string> excludesIgnore;

  vector<string> includes;
  vector<string> includesIgnore;

  int ticksFrequency;

  AbstractTracingProfiler *tracingProfiler;
};

#endif // AGENTOPTIONS_H
