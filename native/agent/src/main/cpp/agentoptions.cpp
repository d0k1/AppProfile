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

#include "agentoptions.h"
#include "utils.h"
#include <iostream>
#include <regex>
#include <boost/algorithm/string.hpp>

#include "simplecallcounterprofiler.h"
#include "threadcallstackprofiler.h"

bool AgentOptions::isCsvOnExit(){
  return csvOnExit;
}

int AgentOptions::getTracingProfilerDepth(){
  return tracingMaxDepth;
}

int AgentOptions::getTimerFrequency()
{
    return ticksFrequency;
}

bool AgentOptions::isMemoryTracking()
{
    return memoryTracking;
}

bool AgentOptions::isMemoryTrackingEvents()
{
    return memoryTrackingEvents;
}

AgentOptions::AgentOptions(string filename){
  options_description desc("Options");
  variables_map vm;

  string tracingProfilerType;
  string printOnExitValue;
  string printVMEventsValue;
  string printInstrumentedClassnamesValue;
  string csvOnExitValue;
  string maxDepthValue;
  string timerFreq;
  string memtrack;
  string memtrackevents;

  desc.add_options()("agent.exclude", boost::program_options::value<std::string>(&agentExclude));
  desc.add_options()("agent.exclude.ingore", boost::program_options::value<std::string>(&agentExcludeIgnore));
  desc.add_options()("agent.include", boost::program_options::value<std::string>(&agentInclude));
  desc.add_options()("agent.include.ingore", boost::program_options::value<std::string>(&agentIncludeIgnore));
  desc.add_options()("agent.appId", boost::program_options::value<std::string>(&appId));
  desc.add_options()("helper.jar", boost::program_options::value<std::string>(&helperJar));
  desc.add_options()("tracing.profiler", boost::program_options::value<std::string>(&tracingProfilerType));
  desc.add_options()("tracing.profiler.depth", boost::program_options::value<std::string>(&maxDepthValue));
  desc.add_options()("tracing.profiler.print.on.exit", boost::program_options::value<std::string>(&printOnExitValue));
  desc.add_options()("tracing.profiler.print.on.exit.csv", boost::program_options::value<std::string>(&csvOnExitValue));
  desc.add_options()("timer.freq", boost::program_options::value<std::string>(&timerFreq));

  desc.add_options()("memory.tracking", boost::program_options::value<std::string>(&memtrack));
  desc.add_options()("memory.tracking.events", boost::program_options::value<std::string>(&memtrackevents));

  desc.add_options()("print.vm.events", boost::program_options::value<std::string>(&printVMEventsValue));
  desc.add_options()("print.instrumented.classes", boost::program_options::value<std::string>(&printInstrumentedClassnamesValue));

  ifstream settings_file( filename , std::ifstream::in );
  store( parse_config_file( settings_file , desc, true ), vm );
  notify( vm );
  settings_file.close();

  excludes = Utils::splitString(agentExclude, ",");
  excludesIgnore = Utils::splitString(agentExcludeIgnore, ",");
  includes = Utils::splitString(agentInclude, ",");
  includesIgnore = Utils::splitString(agentIncludeIgnore, ",");

  int defaultDepth = 5;
  tracingMaxDepth = defaultDepth;
  if(maxDepthValue.length()==0 || maxDepthValue.length()>2){
    tracingMaxDepth = defaultDepth;
  }
  try{
    tracingMaxDepth = stoi(maxDepthValue);
  }catch(...){
    tracingMaxDepth=defaultDepth;
  }

  try{
    ticksFrequency = stoi(timerFreq);
  }catch(...){
    ticksFrequency=0;
  }

  if(memtrack=="false"){
    memoryTracking = false;
  } else {
    memoryTracking = true;
  }

  if(memtrackevents=="false"){
    memoryTrackingEvents = false;
  } else {
    memoryTrackingEvents = true;
  }

  if(csvOnExitValue=="false"){
    csvOnExit = false;
  } else {
    csvOnExit = true;
  }
  if(printOnExitValue=="true"){
    profilerPrintOnExit = true;
  } else {
    profilerPrintOnExit = false;
  }

  if(printVMEventsValue=="true"){
    printVMEvents = true;
  } else {
    printVMEvents = false;
  }

  if(printInstrumentedClassnamesValue=="true"){
    printInstrumentedClassnames = true;
  } else {
    printInstrumentedClassnames = false;
  }

  if(tracingProfilerType.length()==0 || tracingProfilerType=="simple"){
    tracingProfiler = new SimpleCallCounterProfiler();
  }else if(tracingProfilerType=="threadcallstack"){
    tracingProfiler = new ThreadCallStackProfiler();
  }
}

bool matchMask(string value, string regexp){
  if(regexp.length()<3 || !boost::algorithm::starts_with(regexp, ".*")){
    return false;
  }
  regex e(regexp);

  return regex_match(value, e);
}

bool AgentOptions::isClassExcluded(const char *klass){
  string className(klass);
  boost::algorithm::replace_all<string>(className, "/", ".");
  bool skip = false;

  for(auto it=includes.begin();it!=includes.end();++it){
    if (boost::algorithm::starts_with(className, *it) || *it == "*" || matchMask(className, *it)){
      skip = false;
      break;
    } else {
      if(!skip){
	skip = true;
      }
    }
  }

  for(auto it=includesIgnore.begin();it!=includesIgnore.end();++it){
    if(boost::algorithm::starts_with(className, *it)){
      skip = false;
      break;
    }
  }

  for(auto it=excludes.begin();it!=excludes.end();++it){
    if (boost::algorithm::starts_with(className, *it) || *it == "*" || matchMask(className, *it)){
      skip = true;
      break;
    }
  }

  for(auto it=excludesIgnore.begin();it!=excludesIgnore.end();++it){
    if(boost::algorithm::starts_with(className, *it)){
      skip = false;
      break;
    }
  }

  return skip;
}

string AgentOptions::getHelperJar(){
  return helperJar;
}

AbstractTracingProfiler *AgentOptions::getTracingProfiler(){
  return tracingProfiler;
}

bool AgentOptions::isTracingProfilerPrintOnExit(){
  return profilerPrintOnExit;
}

bool AgentOptions::isPrintVMEvents(){
  return printVMEvents;
}

bool AgentOptions::isPrintInstrumentedClasses(){
  return printInstrumentedClassnames;
}
