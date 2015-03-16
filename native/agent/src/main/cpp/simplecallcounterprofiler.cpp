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
#include <boost/format.hpp>
#include "utils.h"

using namespace std;
using boost::format;

SimpleCallCounterProfiler::SimpleCallCounterProfiler(){
}

void SimpleCallCounterProfiler::methodInstrumented(JavaMethodInfo *info){
}

void SimpleCallCounterProfiler::methodEntry(int cnum, int mnum, jobject thread){  
  auto methodId = Utils::getMethodId(cnum, mnum);
  auto info = getRuntime()->getCurrentThreadInfo();
  pthread_t threadKey = info.getProcessTid();
  //unsigned long threadKey = (unsigned long)(*(long *)thread);
  auto it = statByThread.find(threadKey);
  unordered_map<unsigned long, CallStatistics*> *stat = nullptr;
  
  if(it==statByThread.end()){
    stat = new unordered_map<unsigned long, CallStatistics*>();
    statByThread.emplace(threadKey, stat);
  } else {
    stat = (it->second);
  }
  
  CallStatistics *call = nullptr;
  
  auto stat_it = stat->find(methodId); 
  
  if(stat_it == stat->end()){
    call = new CallStatistics();
    stat->emplace(methodId, call);
  } else {
    call = (stat_it->second);
  }
  
  call->callCount++;
}

void SimpleCallCounterProfiler::methodExit(int cnum, int mnum, jobject thread){
  auto info = getRuntime()->getCurrentThreadInfo();
  pthread_t threadKey = info.getProcessTid();
  //unsigned long threadKey = (unsigned long)(*(long *)thread);
  auto it = statByThread.find(threadKey);
  
  unordered_map<unsigned long, CallStatistics*> *stat = nullptr;
  
  if(it!=statByThread.end()){
    stat = (it->second);
  }
  
  if(stat!=nullptr){
    CallStatistics *call = nullptr;
    
    auto methodId = Utils::getMethodId(cnum, mnum);
    auto stat_it = stat->find(methodId); 
    
    if(stat_it != stat->end()){
      call = (stat_it->second);
    }

    if(call!=nullptr){
	call->returnCount++;
    }
  }
}

void SimpleCallCounterProfiler::printOnExit(){
  getRuntime()->agentGlobalLock();
  
  unsigned int calls1 = 0;
  unsigned long total = 0;

  cout << "Threads " << statByThread.size() <<endl;
  
  for(auto it=statByThread.begin();it!=statByThread.end();it++){
      cout << "Thread " << it->first << " " << endl;
      unordered_map<unsigned long, CallStatistics*> *calls = it->second;
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
}

void SimpleCallCounterProfiler::threadStopped(jobject thread){
}

void SimpleCallCounterProfiler::reset() {
  for(auto it=statByThread.begin();it!=statByThread.end();it++){
    unordered_map<unsigned long, CallStatistics*> *calls = it->second;
    
    for(auto call_it=calls->begin();call_it!=calls->end();call_it++){
      CallStatistics *stat = call_it->second;

      stat->callCount=0;
      stat->returnCount=0;
      stat->prevCall=nullptr;
    }
  }
}

string SimpleCallCounterProfiler::printCsv(){
  string result = "threadId;methodName;callCount;returnCount\r\n";
  
  for(auto it=statByThread.begin();it!=statByThread.end();it++){
    unordered_map<unsigned long, CallStatistics*> *calls = it->second;
    pthread_t threadId = it->first;
    
    for(auto call_it=calls->begin();call_it!=calls->end();call_it++){
      auto method = getClasses()->getMethodById(call_it->first);
      string methodName = method->getFQN();
      
      format line("%d;%s;%d;%d\r\n");
      CallStatistics *stat = call_it->second;
      line % threadId % methodName % stat->callCount % stat->returnCount;
      result.append(line.str());
    }
  }
  
  return result;
}
