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

#include "threadcallstackprofiler.h"
#include <boost/format.hpp>

using boost::format;

ThreadCallStackProfiler::ThreadCallStackProfiler(){
}

void ThreadCallStackProfiler::methodEntry(int cnum, int mnum, jobject thread) {
  auto method = getClasses()->getMethodInfo(cnum, mnum);
  auto info = getRuntime()->getCurrentThreadInfo();
  pthread_t threadKey = info.getProcessTid();  
  //unsigned long threadKey = (unsigned long)(*(long *)thread);
  auto it = statByThread.find(threadKey);
  
  ThreadControl *ctrl = nullptr;
  
  if(it==statByThread.end()){
    ctrl = new ThreadControl();
    statByThread.emplace(threadKey, ctrl);
  } else {
    ctrl = (it->second);
  }
  
  // Вход в метод
  CallStatistics *stat = ctrl->current;
  
  // если еще не были внтури ниодного метода
  if(stat==nullptr){
    // ищем такой же, для статистики
    auto root_it = ctrl->roots.find(method->getMethodId());
    
    // если не бывали в таком методе раньше
    if(root_it==ctrl->roots.end()){
      stat = new CallStatistics();
      ctrl->roots.emplace(method->getMethodId(), stat);
      ctrl->current = stat;
    } else {
      stat = root_it->second;
    }
  } else {
    // если уже внутри какого-то метода
    // пробуем найти метод внутри текущего, в который уже погружались
    auto child_it = ctrl->current->childs.find(method->getMethodId());
    
    // не найден - создаем новый
    if(child_it == ctrl->current->childs.end()){
      stat = new CallStatistics();
      ctrl->current->childs.emplace(method->getMethodId(), stat);      
    } else {
      stat = child_it->second;
    }
    // кладем в стек потока текущий метод
    stat->prevCall = ctrl->current;
    // текущим становиться stat метод
    ctrl->current = stat;
  }
  
  stat->callCount++;
}

void ThreadCallStackProfiler::methodExit(int cnum, int mnum, jobject thread){
  auto info = getRuntime()->getCurrentThreadInfo();
  pthread_t threadKey = info.getProcessTid();  
  //unsigned long threadKey = (unsigned long)(*(long *)thread);
  auto it = statByThread.find(threadKey);
  
  ThreadControl *ctrl = nullptr;
  if(it==statByThread.end()){
    return;
  }
  
  ctrl = it->second;
  CallStatistics *stat = ctrl->current;
  
  if(stat==nullptr) {
    return;
  }
  
  stat->returnCount++;
  
  if(stat->prevCall!=nullptr){
    ctrl->current = stat->prevCall;
  } else {
    ctrl->current = nullptr;
  }
}

void make_shift(int level){
  for(int i=0;i<level;i++)
    cout << "\t";
}

void printCalls(JavaClassesInfo *classes, map<unsigned long, CallStatistics *> &stats, int level){  
  for(auto it=stats.begin();it!=stats.end();it++){
    CallStatistics *stat = it->second;
    auto method = classes->getMethodById(it->first);    
    make_shift(level);
    cout << method->getClass()->getName();
    cout << "#" <<  method->getName()<<method->getSignature() << " calls " << stat->callCount << " returns "<<stat->returnCount <<endl;
    printCalls(classes, stat->childs, level+1);
  }
}

void ThreadCallStackProfiler::printOnExit(){
  getRuntime()->agentGlobalLock();
  
  cout << "Threads " << statByThread.size() <<endl;

  for(auto it=statByThread.begin();it!=statByThread.end();it++){
    cout << "Thread " << it->first << " " << endl;
    ThreadControl *ctrl = it->second;
    printCalls(getClasses(), ctrl->roots, 1);
  }

  getRuntime()->agentGlobalUnlock();
}

void ThreadCallStackProfiler::methodInstrumented(JavaMethodInfo *info){
}

void ThreadCallStackProfiler::threadStarted(jobject thread){
}

void ThreadCallStackProfiler::threadStopped(jobject thread){
}

void resetCalls(map<unsigned long, CallStatistics *> &stats){  
  for(auto it=stats.begin();it!=stats.end();it++){
    CallStatistics *stat = it->second;
    
    stat->callCount=0;
    stat->returnCount=0;
    stat->nanosMax=0;
    stat->nanosMin=0;
    stat->nanosMean=0;
    stat->nanosMediana=0;
    stat->prevCall=nullptr;
    resetCalls(stat->childs);
  }
}

void ThreadCallStackProfiler::reset() {
  getRuntime()->agentGlobalLock();
  
  for(auto it=statByThread.begin();it!=statByThread.end();it++){
    ThreadControl *ctrl = it->second;
    ctrl->current = nullptr;
    resetCalls(ctrl->roots);
  }
  getRuntime()->agentGlobalUnlock();
}

string printCall(JavaClassesInfo *classes, pthread_t threadId, map<unsigned long, CallStatistics *> &stats, unsigned long parentId){
  
  string result("");
  
  for(auto it=stats.begin();it!=stats.end();it++){
    auto method = classes->getMethodById(it->first);    
    string methodName = method->getFQN();
    
    format line("%d;%d;%d;%s;%d;%d\r\n");   
    CallStatistics *stat = it->second;
    
    line % threadId % parentId % (unsigned long)stat % methodName % stat->callCount % stat->returnCount;
    result.append(line.str());
    result.append(printCall(classes, threadId, stat->childs, (unsigned long)stat));
  }
  
  return result;
}


string ThreadCallStackProfiler::printCsv(){
  getRuntime()->agentGlobalLock();
  
  string result = "threadId;parentId;methodId;methodName;callCount;returnCount\r\n";
  
  for(auto it=statByThread.begin();it!=statByThread.end();it++){
    ThreadControl *ctrl = it->second;
    result += printCall(getClasses(), it->first, ctrl->roots, 0);
  }
  
  getRuntime()->agentGlobalUnlock();  
  
  return result;
}
