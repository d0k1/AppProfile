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

#ifndef THREADCALLSTACKPROFILER_H
#define THREADCALLSTACKPROFILER_H

#include "abstracttracingprofiler.h"
#include <unordered_map>

#include <string>

using namespace std;

struct ThreadControl final {
  unordered_map<unsigned long long, CallStatistics *> roots;
  CallStatistics *current;

  ThreadControl():current(nullptr){};
};

struct Hash{
  size_t operator()(const pthread_t &x) const{
    return x % 200;//std::hash<long>()(x);
  }
};

struct Equal {
    // NOTE: Assumes lhs != nullptr && rhs != nullptr.
    bool operator()(const pthread_t & lhs, const pthread_t & rhs) const {
        return lhs == rhs;
    }
};

class ThreadCallStackProfiler : public AbstractTracingProfiler
{
public:
  ThreadCallStackProfiler();

  virtual void setData(AgentRuntime *runtime, JavaClassesInfo *classes, JavaThreadsInfo *threads) final;

  virtual void methodEntry(int cnum, int mnum, jobject thread) override;
  virtual void methodExit(int cnum, int mnum, jobject thread) override;
  virtual void printOnExit() override;

  virtual void new_object(jobject obj) override;
  virtual void new_array(jobject obj) override;

  virtual void methodInstrumented(JavaMethodInfo *info) override;
  virtual void threadStarted(jobject thread);
  virtual void threadStopped(jobject thread);

  virtual void reset() override final;
  virtual string printCsv() override final;

private:
  ThreadControl *getCurrentThreadControl();
  int maxDepth;
  unordered_map<pthread_t, ThreadControl*, Hash, Equal> statByThread;
};

#endif // THREADCALLSTACKPROFILER_H
