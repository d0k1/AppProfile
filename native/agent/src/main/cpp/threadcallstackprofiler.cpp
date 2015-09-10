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
#include "utils.h"
using boost::format;

static pthread_key_t key;
static pthread_once_t key_once = PTHREAD_ONCE_INIT;


static void make_key()
{
    (void) pthread_key_create(&key, nullptr);
}

ThreadCallStackProfiler::ThreadCallStackProfiler(){
}

ThreadControl *ThreadCallStackProfiler::getCurrentThreadControl(){
    return (ThreadControl *)pthread_getspecific(key);
}

void ThreadCallStackProfiler::methodEntry(int cnum, int mnum, jobject thread) {
  auto methodId = Utils::getMethodId(cnum, mnum);

  ThreadControl *ctrl = getCurrentThreadControl();
  if(ctrl==nullptr){
    return;
  }

  // Вход в метод
  CallStatistics *stat = ctrl->current;

  // если еще не были внтури ниодного метода
  if(stat==nullptr){
    // ищем такой же, для статистики
    auto root_it = ctrl->roots.find(methodId);

    // если не бывали в таком методе раньше
    if(root_it==ctrl->roots.end()){
      stat = new CallStatistics();
      stat->methodId = methodId;
      stat->level=1;
      ctrl->roots.emplace(methodId, stat);
      ctrl->current = stat;
    } else {
      stat = root_it->second;
      stat->level=1;
      ctrl->current = stat;
    }
  } else {

    if(ctrl->current->level>=maxDepth){
        return;
    }

    // если уже внутри какого-то метода
    // пробуем найти метод внутри текущего, в который уже погружались
    auto child_it = ctrl->current->childs.find(methodId);

    // не найден - создаем новый
    if(child_it == ctrl->current->childs.end()){
        stat = new CallStatistics();
        stat->methodId = methodId;
        stat->level = ctrl->current->level+1;
        ctrl->current->childs.emplace(methodId, stat);
    } else {
      stat = child_it->second;
    }
    // кладем в стек потока текущий метод
    stat->prevCall = ctrl->current;
    // текущим становиться stat метод
    ctrl->current = stat;
  }

  stat->callCount++;
  stat->ticks_last = getRuntime()->getTicks();
}

void ThreadCallStackProfiler::methodExit(int cnum, int mnum, jobject thread){
  auto methodId = Utils::getMethodId(cnum, mnum);

  ThreadControl *ctrl = getCurrentThreadControl();
  if(ctrl==nullptr){
    return;
  }

  CallStatistics *stat = ctrl->current;

  if(stat==nullptr) {
    return;
  }

  if(stat->methodId!=methodId){
    return;
  }

  stat->ticks_spent += (getRuntime()->getTicks() - stat->ticks_last);
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

void printCalls(JavaClassesInfo *classes, unordered_map<unsigned long long, CallStatistics *> &stats, int level){
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

  cout << "Threads " << statByThread.size() << "Buckets " << statByThread.bucket_count() << endl;

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
  pthread_once(&key_once, make_key);
  ThreadControl *ctrl = new ThreadControl();
  statByThread.emplace(getRuntime()->getCurrentThreadInfo().getProcessTid(), ctrl);

  pthread_setspecific(key, ctrl);
}

void ThreadCallStackProfiler::threadStopped(jobject thread){
//    ThreadControl *ctrl = getCurrentThreadControl();

//    if(ctrl!=nullptr){
//        delete ctrl;
//    }
}

void resetCalls(unordered_map<unsigned long long, CallStatistics *> &stats){
  for(auto it=stats.begin();it!=stats.end();it++){
    CallStatistics *stat = it->second;
    stat->callCount=0;
    stat->returnCount=0;
    stat->prevCall=nullptr;
    resetCalls(stat->childs);
  }
}

void ThreadCallStackProfiler::setData(AgentRuntime *runtime, JavaClassesInfo *classes, JavaThreadsInfo *threads){
  AbstractTracingProfiler::setData(runtime, classes, threads);
  maxDepth = getRuntime()->getOptions()->getTracingProfilerDepth();
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

string printCall(JavaClassesInfo *classes, pthread_t threadId, unordered_map<unsigned long long, CallStatistics *> &stats, unsigned long long parentId){

  string result("");

  for(auto it=stats.begin();it!=stats.end();it++){
    auto method = classes->getMethodById(it->first);
    string methodName = method->getFQN();

    format line("\"%d\";\"%d\";\"%d\";\"%d\";\"%s\";\"%d\";\"%d\";\"%d\"\r\n");
    CallStatistics *stat = it->second;

    line % threadId % stat->level % parentId % (unsigned long long)stat->methodId % methodName % stat->callCount % stat->returnCount % stat->ticks_spent;
    result.append(line.str());
    result.append(printCall(classes, threadId, stat->childs, (unsigned long long)stat->methodId));
  }

  return result;
}

void ThreadCallStackProfiler::new_object(jobject obj)
{
}

void ThreadCallStackProfiler::new_array(jobject obj)
{
}

string ThreadCallStackProfiler::printCsv(){
  getRuntime()->agentGlobalLock();

  string result = "threadId;level;parentId;methodId;methodName;callCount;returnCount;ticks\r\n";

  for(auto it=statByThread.begin();it!=statByThread.end();it++){
    ThreadControl *ctrl = it->second;
    result += printCall(getClasses(), it->first, ctrl->roots, 0);
  }

  getRuntime()->agentGlobalUnlock();

  return result;
}
