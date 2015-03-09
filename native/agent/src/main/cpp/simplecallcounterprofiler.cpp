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
  CallStatistics stat;
  stat.callCount=0;
  stat.returnCount=0;
  stat.method = info;
  statistics.emplace(info->getMethodId(), stat);
}

void SimpleCallCounterProfiler::methodEntry(int cnum, int mnum){
  getRuntime()->agentGlobalLock();
  
  JavaMethodInfo *method = getClasses()->getMethodInfo(cnum, mnum);
  statistics.at(method->getMethodId()).callCount++;
  
  getRuntime()->agentGlobalUnlock();
}

void SimpleCallCounterProfiler::methodExit(int cnum, int mnum){
  getRuntime()->agentGlobalLock();

  JavaMethodInfo *method = getClasses()->getMethodInfo(cnum, mnum);
  statistics.at(method->getMethodId()).returnCount++;

  getRuntime()->agentGlobalUnlock();
}

void SimpleCallCounterProfiler::printOnExit(){
  getRuntime()->agentGlobalLock();

  unsigned int calls = 0;
  unsigned long total = 0;
  
  for(auto it=statistics.begin();it!=statistics.end();it++){
      CallStatistics stat = it->second;
      
      if(stat.callCount>0){
	calls++;
	total += stat.callCount;
	
	cout << stat.method->getClass()->getName();
	
	//stat.method->getClass()->getName() << "#" <<
	cout << "#" <<  stat.method->getName()<<stat.method->getSignature() << " calls " << stat.callCount << " returns "<<stat.returnCount <<endl;
      }
  }
  
  cout << "Threads runned: " << getThreads()->getThreadCount() << endl;
  cout << "Classes loaded: " << getClasses()->getClassesCount() << endl;
  cout << "Methods instrumented: " << getClasses()->getMethodsCount() << endl;
  cout << "Total method used " << calls <<endl;
  cout << "Total calls processed " << total<<endl;
  
  getRuntime()->agentGlobalUnlock();
}
