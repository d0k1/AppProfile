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

#include "simplecallcounterprofiler.h"
#include <iostream>

using namespace std;

SimpleCallCounterProfiler::SimpleCallCounterProfiler(){
}

void SimpleCallCounterProfiler::methodInstrumented(JavaMethodInfo *info){
}

void SimpleCallCounterProfiler::methodEntry(int cnum, int mnum, jobject thread){  
  auto method = getClasses()->getMethodInfo(cnum, mnum);
  auto info = getRuntime()->getCurrentThreadInfo();
  unsigned long threadKey = info.getProcessTid();
  //unsigned long threadKey = (unsigned long)(*(long *)thread);
  auto it = statByThread.find(threadKey);
  map<unsigned long, CallStatistics*> *stat = nullptr;
  
  if(it==statByThread.end()){
    stat = new map<unsigned long, CallStatistics*>();
    statByThread.emplace(threadKey, stat);
  } else {
    stat = (it->second);
  }
  
  CallStatistics *call = nullptr;
  
  auto stat_it = stat->find(method->getMethodId()); 
  
  if(stat_it == stat->end()){
    call = new CallStatistics();
    stat->emplace(method->getMethodId(), call);
  } else {
    call = (stat_it->second);
  }
  
  call->callCount++;
}

void SimpleCallCounterProfiler::methodExit(int cnum, int mnum, jobject thread){
  auto info = getRuntime()->getCurrentThreadInfo();
  unsigned long threadKey = info.getProcessTid();
  //unsigned long threadKey = (unsigned long)(*(long *)thread);
  auto it = statByThread.find(threadKey);
  
  map<unsigned long, CallStatistics*> *stat = nullptr;
  
  if(it!=statByThread.end()){
    stat = (it->second);
  }
  
  if(stat!=nullptr){
    CallStatistics *call = nullptr;
    
    auto method = getClasses()->getMethodInfo(cnum, mnum);
    auto stat_it = stat->find(method->getMethodId()); 
    
    if(stat_it != stat->end()){
      call = (stat_it->second);
    }
    
    call->returnCount++;
  }
}

void SimpleCallCounterProfiler::printOnExit(){
  getRuntime()->agentGlobalLock();

  unsigned int calls1 = 0;
  unsigned long total = 0;

  cout << "Threads " << statByThread.size() <<endl;
  
  for(auto it=statByThread.begin();it!=statByThread.end();it++){
      cout << "Thread " << it->first << " " << endl;
      map<unsigned long, CallStatistics*> *calls = it->second;
      cout << "\t" << "calls: " << calls->size() <<endl;
      
      for(auto call_it=calls->begin();call_it!=calls->end();call_it++){
	CallStatistics stat = *call_it->second;
	calls1++;
	total += stat.callCount;
	
	auto method = getClasses()->getMethodById(call_it->first);
	cout << "\t" << method->getClass()->getName();
	
	cout << "#" <<  method->getName()<<method->getSignature() << " calls " << stat.callCount << " returns "<<stat.returnCount <<endl;
      }
  }

  cout << "Threads runned: " << getThreads()->getThreadCount() << endl;
  cout << "Classes loaded: " << getClasses()->getClassesCount() << endl;
  cout << "Methods instrumented: " << getClasses()->getMethodsCount() << endl;
  cout << "Total method used " << calls1 <<endl;
  cout << "Total calls processed " << total<<endl;
  
  getRuntime()->agentGlobalUnlock();
}

void SimpleCallCounterProfiler::threadStarted(jobject thread){
//  auto stat = new map<unsigned long, CallStatistics*>();
//  statByThread.emplace((unsigned long)*(long *)thread, stat);
}

void SimpleCallCounterProfiler::threadStopped(jobject thread){
}

void SimpleCallCounterProfiler::reset() {
}

string SimpleCallCounterProfiler::printCsv(){
}
